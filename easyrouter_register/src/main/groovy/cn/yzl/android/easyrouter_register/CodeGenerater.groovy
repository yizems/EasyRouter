package cn.yzl.android.easyrouter_register

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 代码生成
 */
class CodeGenerater {

    RegisterInfo registerInfo

    CodeGenerater(registerInfo) {
        this.registerInfo = registerInfo
    }

    /**
     * 开始写入代码
     */
    void insertCode() {
        generateCodeIntoJarFile(new File(registerInfo.registerJarFilePath))
    }

    //处理jar包中的class代码注入
    private File generateCodeIntoJarFile(File jarFile) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (isInitClass(entryName)) {
                    println('generate code into:' + entryName)
                    def bytes = doGenerateCode(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

    /**
     * 是否是初始化的 class
     * @param entryName
     * @return
     */
    boolean isInitClass(String entryName) {
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        if (entryName.startsWith(registerInfo.initClassName)) {
            return true
        }
        return false
    }

    private byte[] doGenerateCode(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc,
                                  String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
//            println("method name:${name}")
            if (name == registerInfo.initMethodName) {
                //注入代码到指定的方法之中

                boolean _static = (access & Opcodes.ACC_STATIC) > 0
                mv = new MyMethodVisitor(Opcodes.ASM5, mv, _static)
            }
            return mv
        }
    }

    class MyMethodVisitor extends MethodVisitor {
        boolean _static;

        MyMethodVisitor(int api, MethodVisitor mv, boolean _static) {
            super(api, mv)
            this._static = _static;
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                registerInfo.findRouters.each { item ->
                    if (!_static) {
                        //加载this
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                    }
//                    String paramType
//                    if (extension.paramType == RegisterInfo.PARAM_TYPE_CLASS){
//                        mv.visitLdcInsn(Type.getType("L${name};"))
//                        paramType = 'java/lang/Class'
//                    } else if (extension.paramType == RegisterInfo.PARAM_TYPE_CLASS_NAME){
//                        mv.visitLdcInsn(name.replaceAll("/", "."))
//                        paramType = 'java/lang/String'
//                    } else {
//                        //用无参构造方法创建一个组件实例
//                        mv.visitTypeInsn(Opcodes.NEW, name)
//                        mv.visitInsn(Opcodes.DUP)
//                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
//                        paramType = extension.interfaceName
//                    }
                    int methodOpcode = _static ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL
                    //调用注册方法将组件实例注册到组件库中

                    mv.visitLdcInsn(item.path)
                    mv.visitLdcInsn(Type.getType("L${item.className};"))

                    mv.visitMethodInsn(methodOpcode
                            , registerInfo.initClassName
                            , registerInfo.registerMethodName
                            , "(Ljava/lang/Object;Ljava/lang/Object;)V"
                            , false)
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}