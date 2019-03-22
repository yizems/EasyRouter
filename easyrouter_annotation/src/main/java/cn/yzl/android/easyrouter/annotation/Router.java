package cn.yzl.android.easyrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Router {
    /**
     * 路径
     * @return
     */
    String path();

    /**
     * 参数key
     * @return
     */
    String paramsKey() default "router_params";
}
