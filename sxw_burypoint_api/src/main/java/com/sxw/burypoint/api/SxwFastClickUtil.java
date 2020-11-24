package com.sxw.burypoint.api;

import android.util.Log;

import com.sxw.burypoint.api.annotation.FastClickIntercept;

/**
 * Created by zhanghai on 2019/7/25.
 * function：防连续点击工具类
 */
public final class SxwFastClickUtil {

    //点击的viewid
    private static int mViewId;
    //开始点击view的时间戳
    private static long mFirstTimestamp;

    /**
     * 重复点击事件检测
     * @param viewId 点击的组件id
     *
     * @return true 表示是连续点击（需要做防连续点击处理），false 非连续点击
     */
    public static boolean fastClickView(int viewId){
        return fastClickView(viewId, FastClickIntercept.MIN_REPEAT_TIME);
    }

    /**
     * 重复点击事件检测
     * @param viewId 点击的组件id
     *
     * @return true 表示是连续点击（需要做防连续点击处理），false 非连续点击
     */
    public static boolean fastClickView(int viewId,int intervalTime){
        if(mViewId == viewId){//判断点击的是同一个组件
            if(System.currentTimeMillis() - mFirstTimestamp < (intervalTime <= 0 ? FastClickIntercept.MIN_REPEAT_TIME : intervalTime)){
                Log.i("SxwFastClickUtil","重复点击了...");
                return true;
            }
        }
        mFirstTimestamp = System.currentTimeMillis();
        mViewId = viewId;
        return false;
    }
}
