package cn.yzl.android.easyrouter;

import android.app.Activity;
import android.content.Context;

public class EasyRouter {
    public static Request with(Activity activity) {
        return new Request(activity);
    }

    public static Request with(Context context) {
        return new Request(context);
    }


    public static void addRouter(String path, Class targetClass) {
        RouterManager.addRouter(path, targetClass);
    }

    public static Class getRouter(String path) {
       return RouterManager.ROUTERS.get(path);
    }
}
