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

    /**
     * 处理jar包中的class代码注入
     * 因为咱们的 注入类就在jar中.所以不考虑 class文件的写入,但是方式是一样的
     * {@link #generateCodeIntoClassFile}
     * @param jarFile
     * @return
     */
    private File generateCodeIntoJarFile(File jarFile) {
        if (jarFile) {
            //先重命名,然后修改后再改回来,覆盖掉源文件
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()

            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            //输出流
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                //遍历 文件
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()//文件名/类名
                InputStream inputStream = file.getInputStream(jarEntry)
                //构建输出Entry
                ZipEntry zipEntry = new ZipEntry(entryName)
                jarOutputStream.putNextEntry(zipEntry)

                if (isInitClass(entryName)) {
                    println('generate code into:' + entryName)
                    def bytes = doGenerateCode(inputStream)
                    //写入新字节码
                    jarOutputStream.write(bytes)
                } else {
                    //无需插入代码,写入原字节码
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            //删除原jar,将临时文件转换为原jar
            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }


    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    private byte[] generateCodeIntoClassFile(File file) {
        def optClass = new File(file.getParent(), file.name + ".opt")

        FileInputStream inputStream = new FileInputStream(file)
        FileOutputStream outputStream = new FileOutputStream(optClass)

        def bytes = doGenerateCode(inputStream)
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
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
    /**
     * 写入代码并获取 写入后的byte[]
     * @param inputStream
     * @return
     */
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
                //构建 methodvisitor
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
                    int methodOpcode = _static ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL
                    //调用注册方法将组件实例注册到组件库中
                    //构建一个变量  path
                    mv.visitLdcInsn(item.path)
                    //构建一个type类型的变量 其实就是 class
                    mv.visitLdcInsn(Type.getType("L${item.className};"))
                    //添加代码,两个变量就是用的上面的两个值
                    mv.visitMethodInsn(methodOpcode
                            , registerInfo.initClassName
                            , registerInfo.registerMethodName
                            , "(Ljava/lang/String;Ljava/lang/Class;)V"
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