package com.sxw.burypoint.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhanghai on 2019/7/18.
 * function：自动插入日志
 */
@Target({ElementType.METHOD,ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface AutoLog {
    @MethodType String method() default I;  //Log的方法
    String tag() default "";                //日志的Tag
    String msg() default "";                //日志的消息内容


    final String I = "i";
    final String E = "e";
    final String W = "w";
    final String D = "d";

    @StringDef({I,E,W,D})
    public @interface MethodType{}
}
