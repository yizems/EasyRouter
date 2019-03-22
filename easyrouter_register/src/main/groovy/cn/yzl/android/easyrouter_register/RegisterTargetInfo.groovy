package cn.yzl.android.easyrouter_register
/**
 * 目标类
 */
public class RegisterTargetInfo {

    RegisterTargetInfo() {}

    String targetClass;

    String targetMethod;

    @Override
    public String toString() {
        return "RegisterTargetInfo{" +
                "targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                '}';
    }

    def propertyMissing(String name) {
        return null
    }

    def propertyMissing(String name, def arg) {

    }

    def methodMissing(String name, def args) {
        return null
    }
}