package cn.yzl.android.easyrouter_register

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 自动注册核心类
 */
public class RegisterTransform extends Transform {

    public static final String PLUGIN_NAME = RegisterPlugin.PLUGIN_NAME


    RegisterTargetInfo targetInfo;
    Project project;

    RegisterTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return PLUGIN_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        project.logger.warn("start ${PLUGIN_NAME} transform...")
        project.logger.warn(targetInfo.toString())

//        def clearCache = !isIncremental
//        // clean build cache
//        if (clearCache) {
//            outputProvider.deleteAll()
//        }
        outputProvider.deleteAll()

        CodeScanner scanProcessor = new CodeScanner(targetInfo)

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each {DirectoryInput directoryInput->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等


                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //对类型为jar文件的input进行遍历
            input.jarInputs.each {JarInput jarInput->

                //jar文件一般是第三方依赖库jar文件

                // 重命名输出文件（同目录copyFile会冲突）
//                def jarName = jarInput.name
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if(jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0,jarName.length()-4)
//                }
//                //生成输出路径
//                def dest = outputProvider.getContentLocation(jarName+md5Name,
//                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                //将输入内容复制到输出
//                FileUtils.copyFile(jarInput.file, dest)

                scanJar(jarInput, outputProvider, scanProcessor)
            }
        }
    }


    static void scanJar(JarInput jarInput, TransformOutputProvider outputProvider, CodeScanner scanProcessor) {

        // 获得输入文件
        File src = jarInput.file
        //遍历jar的字节码类文件，找到被注解标记的类
        File dest = getDestFile(jarInput, outputProvider)
        long time = System.currentTimeMillis();
        scanProcessor.scanJar(src, dest)
//        if (!scanProcessor.scanJar(src, dest) //直接读取了缓存，没有执行实际的扫描
//                //此jar文件中不需要被注入代码
//                //为了避免增量编译时代码注入重复，被注入代码的jar包每次都重新复制
//                && !scanProcessor.isCachedJarContainsInitClass(src.absolutePath)) {
//            //不需要执行文件复制，直接返回
//            return
//        }
        println "${PLUGIN_NAME} cost time: " + (System.currentTimeMillis() - time) + " ms to scan jar file:" + dest.absolutePath
        //复制jar文件到transform目录：build/transforms/cc-register/
        FileUtils.copyFile(src, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }
}