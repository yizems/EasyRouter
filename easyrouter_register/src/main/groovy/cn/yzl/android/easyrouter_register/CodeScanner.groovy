package cn.yzl.android.easyrouter_register


import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 *
 * @author billy.qi
 * @since 17/3/20 11:48
 */
class CodeScanner {

//    ArrayList<RegisterInfo> infoList
//    Map<String, ScanJarHarvest> cacheMap
//    Set<String> cachedJarContainsInitClass = new HashSet<>()
//
//    CodeScanner(ArrayList<RegisterInfo> infoList, Map<String, ScanJarHarvest> cacheMap) {
//        this.infoList = infoList
//        this.cacheMap = cacheMap
//    }
    RegisterTargetInfo targetInfo

    CodeScanner(targetInfo) {
        this.targetInfo = targetInfo
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

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            //support包不扫描
            if (entryName.startsWith("android/support"))
                break
            checkInitClass(entryName, destFile, srcFilePath)
            //是否要过滤这个类，这个可配置
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
     * @param entryName
     * @param destFile
     */
    boolean checkInitClass(String entryName, File destFile) {
        checkInitClass(entryName, destFile, "")
    }

    boolean checkInitClass(String entryName, File destFile, String srcFilePath) {
        if (entryName == null || !entryName.endsWith(".class"))
            return
        println("entryName:" + entryName)
        entryName = entryName.substring(0, entryName.lastIndexOf('.'))
        println("entryName:" + entryName)
        def found = false
        if (targetInfo.targetClass == entryName) {
            ext.fileContainsInitClass = destFile
            if (destFile.name.endsWith(".jar")) {
//                addToCacheMap(null, entryName, srcFilePath)
                found = true
            }
        }
        return found
    }

    // file in folder like these
    //com/billy/testplugin/Aop.class
    //com/billy/testplugin/BuildConfig.class
    //com/billy/testplugin/R$attr.class
    //com/billy/testplugin/R.class
    // entry in jar like these
    //android/support/v4/BuildConfig.class
    //com/lib/xiwei/common/util/UiTools.class
    boolean shouldProcessClass(String entryName) {
        println('classes:' + entryName)
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        entryName = entryName.substring(0, entryName.lastIndexOf('.'))
//        def length = infoList.size()
//        for (int i = 0; i < length; i++) {
//            if (shouldProcessThisClassForRegister(infoList.get(i), entryName))
//                return true
//        }
        return false
    }

    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    boolean scanClass(File file) {
        return scanClass(file.newInputStream(), file.absolutePath)
    }

    //refer hack class when object init
    boolean scanClass(InputStream inputStream, String filePath) {
        println("path:" + filePath)
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
                return new AnnotationVisitor(Opcodes.ASM5,super.visitAnnotation(desc, visible)) {
                    @Override
                    void visit(String name, Object value) {
                        super.visit(name, value)
                        println("找到 annotation值:$name -- $value")
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
            super.visit(version, access, name, signature, superName, interfaces)
        }

    }
}