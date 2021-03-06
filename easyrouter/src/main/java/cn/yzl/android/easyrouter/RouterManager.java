package cn.yzl.android.easyrouter;

import java.util.concurrent.ConcurrentHashMap;

class RouterManager {

    public static final String KEY_PARAMS = "router_params";

    static final ConcurrentHashMap<String, Class> ROUTERS = new ConcurrentHashMap<>();


    static {
        //自动注入的类
//        addRouter("test", RouterManager.class);
    }

    public static void addRouter(String path, Class targetClass) {
        ROUTERS.put(path, targetClass);
    }

}
