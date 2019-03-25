package cn.yzl.android.easyrouter_register

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 自动注册核心类
 */
class RegisterTransform extends Transform {

    public static final String PLUGIN_NAME = RegisterPlugin.PLUGIN_NAME


    RegisterInfo registerInfo = new RegisterInfo()

    Project project

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

        //todo 增量更新

        boolean leftSlash = File.separator == '/'
        outputProvider.deleteAll()
        //初始化 CodeScanner
        CodeScanner scanProcessor = new CodeScanner(registerInfo)

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                String root = directoryInput.file.absolutePath
                //文件分隔符处理
                if (!root.endsWith(File.separator))
                    root += File.separator
                //遍历文件
                directoryInput.file.eachFileRecurse { File file ->
                    //只处理 class文件
                    if (file.absolutePath.endsWith(".class")) {
                        def path = file.absolutePath.replace(root, '')
                        if (file.isFile()) {
                            def entryName = path
                            if (!leftSlash) {
                                entryName = entryName.replaceAll("\\\\", "/")
                            }
                            scanProcessor.checkInitClass(entryName, new File(dest.absolutePath + File.separator + path))
                            scanProcessor.scanClass(file)
                        }
                    }
                }
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //开始扫描jar中的class文件,复制操作放到里面做了
                scanJar(jarInput, outputProvider, scanProcessor)
            }
        }

        println(registerInfo.toString())
        //开始生成文件
        new CodeGenerater(registerInfo).insertCode()
    }


    static void scanJar(JarInput jarInput, TransformOutputProvider outputProvider, CodeScanner scanProcessor) {

        // 获得输入文件
        File src = jarInput.file
        //遍历jar的字节码类文件，找到被注解标记的类
        File dest = getDestFile(jarInput, outputProvider)
//        long time = System.currentTimeMillis();
        scanProcessor.scanJar(src, dest)
//        println "${PLUGIN_NAME} cost time: " + (System.currentTimeMillis() - time) + " ms to scan jar file:" + dest.absolutePath
        //复制jar文件到transform目录：build/intermediates/transforms/easy_router
        FileUtils.copyFile(src, dest)
    }
    /**
     * 获取生成的行文件 jar 文件名和路径
     * jar要被重命名,否则会覆盖掉
     * @param jarInput
     * @param outputProvider
     * @return
     */
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