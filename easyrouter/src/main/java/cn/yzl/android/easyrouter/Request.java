package cn.yzl.android.easyrouter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class Request {

    private Object obj;
    /**
     * 0 context
     * 1 activity
     */
    private int type;


    private String path;

    private String json;


    public Request(Activity activity) {
        this.obj = activity;
        type = 1;
    }

    public Request(Context context) {
        this.obj = context;
        type = 0;
    }


    public Request setPath(String path) {
        this.path = path;
        return this;
    }

    public Request setParamJson(String json) {
        this.json = json;
        return this;
    }

    // TODO: 25/03/2019 excute 拦截器

//    public void excute() {
//        excute(false);
//    }
//
//    public void excute(boolean needResult) {
//        if (needResult && type != 1) {
//            throw new IllegalArgumentException("obj must be activity");
//        }
//
//    }

    public Intent getIntent() {
        if (path == null) {
            throw new IllegalArgumentException("path can not be null");
        }
        Intent intent = null;

        Class clazz = RouterManager.ROUTERS.get("path");

        if (clazz == null) {
            throw new IllegalArgumentException("no path router");
        }
        if (obj instanceof Activity) {
            intent = new Intent((Activity) obj, clazz);
        } else if (obj instanceof Context) {
            intent = new Intent((Context) obj, clazz);
        }

        intent.putExtra("router_params", json);

        return intent;
    }
}
