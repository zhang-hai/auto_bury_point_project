package com.sxw.burypoint.api.annotation;

import com.sxw.burypoint.api.SxwFastClickUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by zhanghai on 2019/7/22.
 * function：连续点击拦截注解
 */

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface FastClickIntercept {
    final int MIN_REPEAT_TIME = 500;//按钮连续点击重复判断时间，单位为ms

    /**
     * 间隔时间，以毫秒为单位，
     * 两次点击时间间隔小于该值时，认定为重复点击，反之，正常点击
     * @return
     */
    int intervalTime() default MIN_REPEAT_TIME;
}
