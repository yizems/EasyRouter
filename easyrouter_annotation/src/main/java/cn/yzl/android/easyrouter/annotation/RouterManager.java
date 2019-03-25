package cn.yzl.android.easyrouter.annotation;

import java.util.concurrent.ConcurrentHashMap;

public class RouterManager {

    private static final ConcurrentHashMap<String, Class> ROUTERS = new ConcurrentHashMap<>();


    static {
        //自动注入的类
       addRouter("test",RouterManager.class);
    }

    public static void addRouter(String path, Class targetClass) {
        ROUTERS.put(path, targetClass);
    }
}
