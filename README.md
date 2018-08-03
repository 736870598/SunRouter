# SunRouter
    手写阿里ARouter框架核心功能

### 效果：
    俩个module互不引用，但是能相互跳转到对方module的界面中...

### 核心原理：
    通过 AbstractProcessor 在编译时就将每个module中配置了路由信息的类都
    装在一个map中，并保存在固定包名下，以固定前缀为类名。
    在运行时通过扫描固定包名下的文件夹，将符合要求的类加载出来，拿到其中的
    路由信息，使用时候通过查找路径获取对象的class，从而进行跳转。
    优点：
        极少的反射，维护性高，（只要管理好路由地址就可以随便跳转。。。）

### 使用：
    1. 在每个module的build.gradle文件中加入：

        android {
            .......
            defaultConfig {
                 .......
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments = [moduleName: project.getName()]
                    }
                }
            }
             .......
        }

        dependencies {
            .......
            implementation project(':router-core')
            annotationProcessor project(':router-compiler')
            .......
        }

    2. RouterCore.getInstance().init(getApplication());
       极可能早的调用，最好在在application的onCreate中

    3. 在需要路由的activity上面加上地址（ 必须包含“/”，分组名/别名 ）
        @Route(path = "main/MainActivity")
        public class MainActivity extends AppCompatActivity{
            ......
        }

    4. 跳转：
        Intent intent = RouterCore.getInstance()
        .createIntent(MainActivity.this, "main/MainActivity");
        if (intent != null){
            startActivity(intent);
        }



### 解析：
    AbstractProcessor 的使用：

        @AutoService(Processor.class)

        /**
         * 处理器接收的参数替 代 {@link AbstractProcessor#getSupportedOptions()} 函数
         */
        @SupportedOptions("moduleName")

        /**
         * 指定使用的Java版本 替代 {@link AbstractProcessor#getSupportedSourceVersion()} 函数
         */
        @SupportedSourceVersion(SourceVersion.RELEASE_7)

        /**
         * 注册给哪些注解的  替代 {@link AbstractProcessor#getSupportedAnnotationTypes()} 函数
         */
        @SupportedAnnotationTypes({Consts.ANN_TYPE_ROUTE})
        public class RouteProcessor extends AbstractProcessor {
           .......................
           .......................
        }


    其中  @SupportedOptions("moduleName") 获取gradle中配置的moduleName信息

         比如在gradle中配置：
            javaCompileOptions {
                annotationProcessorOptions {
                    arguments = [moduleName: project.getName()]
                }
            }

         在AbstractProcessor中获取：
            Map<String, String> options = processingEnvironment.getOptions();
            if (!Utils.isEmpty(options)){
                moduleName = options.get("moduleName");
            }

    类中的：
        /**
         * 正式处理注解
         *
         * @param set   使用了支持处理注解  的节点集合
         * @param roundEnvironment  表示当前或是之前的运行环境,可以通过该对象查找找到的注解。
         * @return  true 表示后续处理器不会再处理(已经处理)
         */
        @Override
        public boolean process(Set<? extends TypeElement> set,
        RoundEnvironment roundEnvironment) {
            ......
        }

        必须重写，其中Set表示节点结合，module中有多少个SupportedAnnotationTypes中的节点，
    那么set集合中就有多少个。。。。

    在该方法中可通过 javapoet 生成java代码，官网地址：https://github.com/square/javapoet
        要点：先把目标类写出来，照着写格式就行了。。。。


### 总结：
    1. 如果项目中有大量重复的且格式差不多相同的类，则可以通过
    AbstractProcessor在编译时动态生成。

    2. ButterKnife 也是相同的套路。


