package cn.yzl.android.easyrouter_register;

import java.util.ArrayList;

public class RegisterInfo {

    public String initClassName = "cn/yzl/android/easyrouter/annotation/RouterManager";
    /**
     * java static{}
     */
    public String initMethodName = "<clinit>";
    //    public String registerClassName = "";
    public String registerMethodName = "addRouter";

    public String registerJarFilePath = "";


    public ArrayList<RouterBean> findRouters = new ArrayList();


    @Override
    public String toString() {
        return "RegisterInfo{" +
                "initClassName='" + initClassName + '\'' +
                ", registerMethodName='" + registerMethodName + '\'' +
                ", registerJarFilePath='" + registerJarFilePath + '\'' +
                ", findRouters=" + findRouters +
                '}';
    }

    static class RouterBean {
        String filePath;
        String className;
        String path;

        public RouterBean() {
        }

        public RouterBean(String filePath, String className, String path) {
            this.filePath = filePath;
            this.className = className;
            this.path = path;
        }

        @Override
        public int hashCode() {
            return (filePath + className).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RouterBean) {
                return filePath.equals(((RouterBean) o).filePath) && className.equals(((RouterBean) o).className);
            }
            return false;
        }


        @Override
        public String toString() {
            return "RouterBean{" +
                    "filePath='" + filePath + '\'' +
                    ", className='" + className + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }
}
