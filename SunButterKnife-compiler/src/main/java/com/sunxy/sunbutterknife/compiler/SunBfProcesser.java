package com.sunxy.sunbutterknife.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sunxy.sunbutterknife.annotation.BindView;
import com.sunxy.sunbutterknife.annotation.UnBind;
import com.sunxy.sunbutterknife.compiler.model.ClassInfo;
import com.sunxy.sunbutterknife.compiler.model.VarInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
@AutoService(Processor.class)
public class SunBfProcesser extends AbstractProcessor {

    /**
     * 存储要创建的class的消息，
     * key：要创建的class的全类名
     * value：classInfo
     */
    private Map<String, ClassInfo> classMap = new HashMap<>();

    /**
     * 存储classInfo中需要findViewById的属性
     * key: ClassInfo
     * value: 需要执行findViewById的属性集合
     */
    private Map<ClassInfo, List<VarInfo>> varMap = new HashMap<>();

    /**
     * 文件生成器 类/资源
     */
    private Filer filerUtils;


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedSet = new TreeSet<>();
        supportedSet.add(BindView.class.getName());
        return supportedSet;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filerUtils = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        if (elements != null){
            for (Element element : elements) {
                try {
                    dealElement(element);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        for (Map.Entry<String, ClassInfo> entry : classMap.entrySet()) {
            try {
                createCode(entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return true;
    }

    private void dealElement(Element element) throws IOException {
        BindView bindView = element.getAnnotation(BindView.class);
        //id值
        int id = bindView.value();
        if (id == 0){
            return;
        }

        //注解的父类节点 相当于是activity节点
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //注解所在类的全类名
        String classAllName = enclosingElement.getQualifiedName().toString();

        //注解所在类的类名
        String className = enclosingElement.getSimpleName().toString();

        //获取注解所在类的包名
        String packageName = classAllName.replace("."+ className, "");

        //要创建的新的类名
        String newClassName = className + "_BindView";

        //注解修饰的属性名
        String varName = element.getSimpleName().toString();

        //注解修饰的属性的类型
        TypeName varTypeName = ClassName.get(element.asType());


        //存放信息：
        ClassInfo classInfo = classMap.get(packageName+"."+newClassName);
        if (classInfo == null){
            classInfo = new ClassInfo(className, packageName, newClassName);
            classMap.put(packageName+"."+newClassName, classInfo);
        }
        List<VarInfo> infoList = varMap.get(classInfo);
        if (infoList == null){
            infoList = new ArrayList<>();
            varMap.put(classInfo, infoList);
        }
        infoList.add(new VarInfo(varName, id, varTypeName));

    }


    private void createCode(ClassInfo classInfo) throws IOException {
        //参数：Activity target
        ParameterSpec objectObj = ParameterSpec.builder(
                ClassName.get(classInfo.packageName, classInfo.className) , "target").build();

        //参数：View view
        ParameterSpec viewTarget = ParameterSpec.builder(
                ClassName.get("android.view", "View"), "view").build();

        //创建构造函数
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(objectObj)
                .addParameter(viewTarget)
                .addStatement("this.target = target");

        //创建普通方法，该方法是实现UnBind接口后要重写的方法。
        MethodSpec.Builder unBindViewMethod =
                MethodSpec.methodBuilder("unBind")   //方法名 loadInfo
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID);



        List<VarInfo> varList = varMap.get(classInfo);
        if (varList != null){
            for (VarInfo varInfo : varList) {
                //构造方法中写入：
                // 例如： target.tv = (TextView)view.findViewById(343422243)
                constructor.addStatement("target.$L = ($T)view.findViewById($L)",
                        varInfo.varName, varInfo.getRawType(), String.valueOf(varInfo.value));

                //解绑函数中写入：
                // 例如： target.tv = null;
                unBindViewMethod.addStatement("target.$L = null", varInfo.varName);
            }
            //最后在解绑方法中将 this.target 置未空
            unBindViewMethod.addStatement("this.target = null");
        }

        //创建一个Field： target；
        FieldSpec target = FieldSpec.builder(objectObj.type, "target")
                .addModifiers(Modifier.PRIVATE).build();

        //创建函数，实现UnBind接口
        JavaFile.builder(classInfo.packageName,
                TypeSpec.classBuilder(classInfo.newClassName)
                        .addSuperinterface(ClassName.get(UnBind.class))
                        .addModifiers(Modifier.PUBLIC)
                        .addField(target)
                        .addMethod(constructor.build())
                        .addMethod(unBindViewMethod.build())
                        .build()
        ).build().writeTo(filerUtils);
    }

}
