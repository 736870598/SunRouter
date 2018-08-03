package com.sunxy.router.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.sunxy.router.annotation.Route;
import com.sunxy.router.annotation.model.RouteMeta;
import com.sunxy.router.compiler.utils.Consts;
import com.sunxy.router.compiler.utils.Log;
import com.sunxy.router.compiler.utils.Utils;

import org.omg.Dynamic.Parameter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/2 0002.
 */
@AutoService(Processor.class)
/**
 * 处理器接收的参数替 代 {@link AbstractProcessor#getSupportedOptions()} 函数
 */
@SupportedOptions(Consts.ARGUMENTS_NAME)
/**
 * 指定使用的Java版本 替代 {@link AbstractProcessor#getSupportedSourceVersion()} 函数
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 注册给哪些注解的  替代 {@link AbstractProcessor#getSupportedAnnotationTypes()} 函数
 */
@SupportedAnnotationTypes({Consts.ANN_TYPE_ROUTE})
public class RouteProcessor extends AbstractProcessor {

    /**
     * key:组名 value:类名
     */
    private Map<String, String> rootMap = new TreeMap<>();
    /**
     * 分组 key:组名 value:对应组的路由信息
     */
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();

    /**
     * 节点工具类 (类、函数、属性都是节点)
     */
    private Elements elementUtils;

    /**
     * type(类信息)工具类
     */
    private Types typeUtils;

    /**
     * 文件生成器 类/资源
     */
    private Filer filerUtils;
    /**
     * 参数
     */
    private String moduleName;

    private Log log;

    private TypeMirror type_Activity;
    private TypeMirror type_Service;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        log = Log.newLog(processingEnvironment.getMessager());
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filerUtils = processingEnvironment.getFiler();

        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)){
            moduleName = options.get(Consts.ARGUMENTS_NAME);
        }
        if (Utils.isEmpty(moduleName)){
            throw new RuntimeException("not set processor parmaters");
        }

        //节点描述
        type_Activity = elementUtils.getTypeElement(Consts.ACTIVITY).asType();
        type_Service = elementUtils.getTypeElement(Consts.SERVICE).asType();

    }

    /**
     * 正式处理注解
     *
     * @param set   使用了支持处理注解  的节点集合
     * @param roundEnvironment  表示当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return  true 表示后续处理器不会再处理(已经处理)
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!Utils.isEmpty(set)){
            Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            if (!Utils.isEmpty(routeElements)){
                try {
                    parseRoutes(routeElements);
                }catch (IOException e){
                    e.printStackTrace();
                    throw new RuntimeException("process " + e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) throws IOException{

        //声明 Route 注解的节点 (需要处理的节点 Activity/IService)
        for (Element element : routeElements) {
            RouteMeta routeMeta;
            // 使用Route注解的类信息
            TypeMirror typeMirror = element.asType();
            Route route = element.getAnnotation(Route.class);
            //是否是 Activity 使用了Route注解
            if (typeUtils.isSubtype(typeMirror, type_Activity)){
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, element);
            }else if (typeUtils.isSubtype(typeMirror, type_Service)){
                routeMeta = new RouteMeta(RouteMeta.Type.SERVICE, route, element);
            }else{
                throw new RuntimeException("[Just Support Activity/Service Route] :" + element);
            }
            //分组信息记录  groupMap <Group分组,RouteMeta路由信息> 集合
            categories(routeMeta);
        }

        //生成类需要实现的接口
        TypeElement iRouteGroup = elementUtils.getTypeElement(Consts.IROUTE_GROUP);
        TypeElement iRouteRoot = elementUtils.getTypeElement(Consts.IROUTE_ROOT);

        /**
         * 生成类需要实现的接口
         */
        GeneratedGroup(iRouteGroup);
        /**
         * 生成类需要实现的接口
         */
        GeneratedRoot(iRouteRoot, iRouteGroup);
    }

    /**
     * 生成group信息
     */
    private void GeneratedGroup(TypeElement iRouteGroup) throws IOException{

        //创建 Map<String, RouteMeta>
        ParameterizedTypeName atlas = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class)
        );

        //创建一个方法的参数 相当于： Map<String, RouteMeta> atlas
        ParameterSpec groupParamSpec = ParameterSpec.builder(atlas, "atlas").build();

        //遍历分组,每一个分组创建一个 $$Group$$ 类
        for (Map.Entry<String, List<RouteMeta>> entry : groupMap.entrySet()) {

            //创建函数： public void loadInfo(Map<String, RouteMeta> atlas);
            MethodSpec.Builder LoadIntoMethodOfGroupBuilder =
                    MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)   //方法名 loadInfo
                            .addAnnotation(Override.class)              //方法上的注解
                            .returns(TypeName.VOID)                     //方法返回值
                            .addModifiers(Modifier.PUBLIC)              //方法修饰符
                            .addParameter(groupParamSpec);              //方法参数

            List<RouteMeta> groupData = entry.getValue();

            //循环遍历group下的节点信息，写到方法内
            for (RouteMeta routeMeta : groupData) {
                //LoadIntoMethodOfGroupBuilder 加一行代码。
                LoadIntoMethodOfGroupBuilder.addStatement(
                        "atlas.put($S, $T.build($T.$L, $T.class, $S, $S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase()
                );
            }

            //分组名
            String groupName = entry.getKey();

            //创建java文件...并写入
            String groupClassName = Consts.NAME_OF_GROUP + groupName;
            JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(groupClassName)
                            .addSuperinterface(ClassName.get(iRouteGroup))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(LoadIntoMethodOfGroupBuilder.build())
                            .build()
            ).build().writeTo(filerUtils);

            //分组名和生成的对应的group类的类名
            rootMap.put(groupName, groupClassName);
        }
    }

    /**
     * 生成root类 作用:记录 <分组，对应的Group类>
     */
    private void GeneratedRoot(TypeElement iRouteRoot, TypeElement iRouteGroup) throws IOException {
        //类型 Map<String,Class<? extends IRouteGroup>> routes>
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                )
        );

        //参数 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec rootParamSpec = ParameterSpec.builder(routes, "routes").build();

        //函数 public void loadInfo(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder LoadIntoMethodOfRootBuilder =
                MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)
                        .returns(TypeName.VOID)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(rootParamSpec);

        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            LoadIntoMethodOfRootBuilder.addStatement(
                    "routes.put($S, $T.class)",
                    entry.getKey(),
                    ClassName.get(Consts.PACKAGE_OF_GENERATE_FILE, entry.getValue() )
            );
        }

        //生成root类
        String rootClassName = Consts.NAME_OF_ROOT + moduleName;
        JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootClassName)
                        .addSuperinterface(ClassName.get(iRouteRoot))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(LoadIntoMethodOfRootBuilder.build())
                        .build()
        ).build().writeTo(filerUtils);
    }


    private void categories(RouteMeta routeMeta) {
        if (routeVerify(routeMeta)){
            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
            if (Utils.isEmpty(routeMetas)){
                List<RouteMeta> routeMetaList = new ArrayList<>();
                routeMetaList.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetaList);
            }else{
                routeMetas.add(routeMeta);
            }
        }else{
            log.i("Group Info Error: " + routeMeta.getPath());
        }
    }

    /**
     * 验证路由信息必须存在path
     */
    private boolean routeVerify(RouteMeta meta){
        String path = meta.getPath();
        String group = meta.getGroup();

        //地址不能为空
        if (Utils.isEmpty(path)) {
            return false;
        }

        //有些设置为：“/main/mainActivity”;
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        meta.setPath(path);

        //如果group不为空，直接设置
        if (!Utils.isEmpty(group)){
            meta.setGroup(group);
            return true;
        }
        //如果group为空，这从path中截取前一截为group
        String defaultGroup;
        //从main/mainActivity中截取main
        if (path.contains("/")){
            defaultGroup = path.substring(0, path.indexOf("/"));
        }else{
            defaultGroup = path;
        }

        if (!Utils.isEmpty(defaultGroup)){
            meta.setGroup(defaultGroup);
            return true;
        }
        return false;
    }

}
