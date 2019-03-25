package cn.yzl.android.easyrouter;

/**
 * 拦截器
 */
public interface Interceptor {
    void interceptor();


    interface Chain {
        void process();
    }
}
