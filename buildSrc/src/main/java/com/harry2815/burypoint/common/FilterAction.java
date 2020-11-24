package com.harry2815.burypoint.common;

/**
 * Created by zhanghai on 2019/7/25.
 * function：
 */
public final class FilterAction {
    //检测日志条件
    public final static String ACTION_LOG = "Lcom/sxw/burypoint/api/annotation/AutoLog;";

    //检测方法耗时
    public static final String ACTION_TIME_COST = "Lcom/sxw/burypoint/api/annotation/MethodTraceTime;";

    //检测连续点击
    public static final String ACTION_FAST_CLICK = "Lcom/sxw/burypoint/api/annotation/FastClickIntercept;";

    //android 自带View的描述
    public static final String ACTION_VIEW_DES = "Landroid/view/View;";






    //是否可用输出日志到Build控制台
    public static boolean isPrintConsole = false;

    //日志方法
    public final static class ALog{
        public static final String OWNER = "android/util/Log";  //类
        public static final String NAME_I = "i";          //方法名称
        public final static String DESC = "(Ljava/lang/String;Ljava/lang/String;)I";
    }

    public final static class ASystem{
        public static final String OWNER = "java/lang/System";  //类
        public static final String NAME = "currentTimeMillis";          //方法名称
        public final static String DESC = "()J";
    }

    //StringBuilder
    public final static class AStringBuilder{
        public static final String OWNER = "java/lang/StringBuilder";  //类
        public static final String NAME_INIT = "<init>";          //方法名称
        public static final String DESC_INIT = "()V"; //描述

        public static final String NAME_APPEND = "append";          //方法名称
        public static final String DESC_APPEND_S = "(Ljava/lang/String;)Ljava/lang/StringBuilder;"; //描述
        public static final String DESC_APPEND_I = "(Ljava/lang/String;)Ljava/lang/StringBuilder;"; //描述

        public static final String NAME_TOSTRING = "toString";          //方法名称
        public static final String DESC_TOSTRING = "()Ljava/lang/String;"; //描述
    }

    //
    public final static class AView{
        public static final String OWNER = "android/view/View";  //类
        public static final String NAME_GETID = "getId";          //方法名称
        public final static String DESC_GETID = "()I";
    }

    /**
     * 快速点击常量
     */
    public final static class AFastClick{
        public static final String OWNER = "com/sxw/burypoint/api/SxwFastClickUtil";  //类
        public static final String NAME = "fastClickView";          //方法名称
        public final static String DESC_1 = "(I)Z";
        public final static String DESC_2 = "(II)Z";
    }
}
