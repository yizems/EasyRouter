package cn.yzl.android.easyrouter_register


import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 扫描字节码
 */
class CodeScanner {

    RegisterInfo registerInfo

    CodeScanner(registerInfo) {
        this.registerInfo = registerInfo
    }
    /**
     * 扫描jar包
     * @param jarFile 来源jar包文件
     * @param destFile transform后的目标jar包文件 修改是在输出文件上修改的
     */
    boolean scanJar(File jarFile, File destFile) {
        //检查是否存在缓存，有就添加class list 和 设置fileContainsInitClass
        if (!jarFile)
            return false

        def srcFilePath = jarFile.absolutePath
        def file = new JarFile(jarFile)
        Enumeration enumeration = file.entries()
        //遍历jar中的元素
        while (enumeration.hasMoreElements()) {
            //这个就是class文件/其他文件
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            //support包不扫描
            if (entryName.startsWith("android/support"))
                break
            checkInitClass(entryName, destFile, srcFilePath)
            //是否要过滤这个类
            if (shouldProcessClass(entryName)) {
                println(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                scanClass(inputStream, jarFile.absolutePath)
                inputStream.close()
            }
        }
        if (null != file) {
            file.close()
        }
        //加入缓存
//        addToCacheMap(null, null, srcFilePath)
        return true
    }
    /**
     * 检查此entryName是不是被注入注册代码的类，如果是则记录此文件（class文件或jar文件）用于后续的注册代码注入
     * @param entryName cn/yzl/android/easyrouter/annotation/RouterManager.class
     * @param destFile
     */
    boolean checkInitClass(String entryName, File destFile) {
        checkInitClass(entryName, destFile, "")
    }
    /**
     *
     * @param entryName cn/yzl/android/easyrouter/annotation/RouterManager.class
     * @param destFile
     * @param srcFilePath
     * @return
     */
    boolean checkInitClass(String entryName, File destFile, String srcFilePath) {
        if (entryName == null || !entryName.endsWith(".class")) {
            return false
        }
        if (entryName.startsWith(registerInfo.initClassName)) {
            registerInfo.registerJarFilePath = destFile
            println("查找init class 成功:" + destFile)
            return true
//            if (destFile.name.endsWith(".jar")) {
////                addToCacheMap(null, entryName, srcFilePath)
//            }
        }
        return false
    }
    /**
     * 需要过滤掉一些非class文件
     * @param entryName
     * @return
     */
    boolean shouldProcessClass(String entryName) {
//        println('classes:' + entryName)
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        if (entryName.startsWith("org/jetbrains")
                || entryName.startsWith("org/intellij")
                || entryName.startsWith("kotlin")
                || entryName.startsWith("android")
                || entryName.startsWith("androidx")
        ) {
            return false
        }
        return true
    }

    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    boolean scanClass(File file) {
        return scanClass(file.newInputStream(), file.absolutePath)
    }

    /**
     * 开始访问文件
     * @param inputStream
     * @param filePath
     * @return
     */
    boolean scanClass(InputStream inputStream, String filePath) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw, filePath)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
        return cv.found
    }

    class ScanClassVisitor extends ClassVisitor {
        private String filePath
        private def found = false
        private def className = ""

        ScanClassVisitor(int api, ClassVisitor cv, String filePath) {
            super(api, cv)
            this.filePath = filePath
        }

        boolean is(int access, int flag) {
            return (access & flag) == flag
        }

        boolean isFound() {
            return found
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            println("visitAnnotation:" + desc)
//            AnnotationNode an = new AnnotationNode(desc)
            if (desc.startsWith("Lcn/yzl/android/easyrouter/annotation/Router")) {
                //构建AnnotationVisitor 访问 annotation
                return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
                    @Override
                    void visit(String name, Object value) {
                        super.visit(name, value)
//                        println("找到 annotation值:$name -- $value")
                        //找到被标记的类,开始添加到 registerInfo.findRouters
                        registerInfo.findRouters.add(new RegisterInfo.RouterBean(filePath, className, value))
                    }
                }
            }
            return null
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            //没有被annotation标记的,直接忽略
            if (is(access, Opcodes.ACC_ANNOTATION)) {
                return
            }
            //记录className
            className = name
            super.visit(version, access, name, signature, superName, interfaces)
        }

    }
}