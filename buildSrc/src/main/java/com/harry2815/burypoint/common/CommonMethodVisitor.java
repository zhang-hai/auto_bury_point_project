package com.harry2815.burypoint.common;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by zhanghai on 2019/7/18.
 * function：deal method visitor
 */
public class CommonMethodVisitor extends AdviceAdapter {

    private String methodName;
    private String mClazzName;

    Type[] argTypes;

    private Map<String,Map<String,String>> mAnnotationMap;


    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api    the ASM API version implemented by this visitor. Must be one
     *               of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     * @param clazzName class's name.
     */
    protected CommonMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc,String clazzName) {
        super(api, mv, access, name, desc);
        this.methodName = name;
        this.mClazzName = clazzName;
        this.argTypes = Type.getArgumentTypes(desc);
        mAnnotationMap = new HashMap<>();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
        if (annotationVisitor != null && desc != null && desc.length() > 0) {
            // 如果注解不是空，传递给AnnotationVisitorAdapter 进行代理处理。
            AnnotationVisitorAdapter avAdapter = new AnnotationVisitorAdapter(Opcodes.ASM5, annotationVisitor, desc);
            mAnnotationMap.put(desc,avAdapter.getKeyMap());
//          System.out.println("visitAnnotation size --> " + mAnnotationMap.size());
            return  avAdapter;
        }
        return annotationVisitor;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        //方法日志插桩
        if(isFilterLog()){
            printVisitClassLog();
            onAutoInsertLog();
        }
        //方法耗时插桩
        if(isFilterTimeCost()){
            printVisitClassLog();
            onAutoInsertTimeCostStart();
        }
        //防按钮连续点击插桩
        if(isInterceptFastClick()){
            printVisitClassLog();
            onAutoInterceptFastClick(getArgIndex(FilterAction.ACTION_VIEW_DES));
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if(isFilterTimeCost()){
            onAutoInsertTimeCostEnd();
        }
    }


    /**
     * 自动插入日志的字节码
     */
    private void onAutoInsertLog() {
        Map<String,String> map = mAnnotationMap.get(FilterAction.ACTION_LOG);
        if(map != null){
            printLog("@AutoLog parameters " + map.toString());
            printLog("insert log.");
            mv.visitLdcInsn(map.containsKey("tag") ? map.get("tag") : "TAG");
            mv.visitLdcInsn(String.format(Locale.CHINESE,"%s - < %s > -----> %s",mClazzName,methodName,(map.containsKey("msg") ? map.get("msg") : "")));
            mv.visitMethodInsn(INVOKESTATIC,FilterAction.ALog.OWNER,map.containsKey("method") ? map.get("method") : FilterAction.ALog.NAME_I,FilterAction.ALog.DESC,false);
            mv.visitInsn(POP);
        }
    }

    private int startTimeIndex;

    /**
     * 方法统计耗时开始插入字节码
     */
    private void onAutoInsertTimeCostStart(){
        printLog("insert trace time.");
        mv.visitMethodInsn(INVOKESTATIC, FilterAction.ASystem.OWNER, FilterAction.ASystem.NAME, FilterAction.ASystem.DESC, false);
        startTimeIndex = newLocal(Type.getType(Long.class));
        mv.visitIntInsn(LSTORE, startTimeIndex);
    }

    /**
     * 方法结束，插入时间字节码，进行统计时间
     */
    private void onAutoInsertTimeCostEnd(){
        int var2 = newLocal(Type.getType(Long.class));
        mv.visitMethodInsn(INVOKESTATIC, FilterAction.ASystem.OWNER, FilterAction.ASystem.NAME, FilterAction.ASystem.DESC, false);
        mv.visitVarInsn(LSTORE, var2);
        mv.visitLdcInsn("TraceTime");
        mv.visitTypeInsn(NEW, FilterAction.AStringBuilder.OWNER);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, FilterAction.AStringBuilder.OWNER, FilterAction.AStringBuilder.NAME_INIT, FilterAction.AStringBuilder.DESC_INIT, false);
        mv.visitLdcInsn(String.format(Locale.CHINESE,"%s - < %s > trace time : ",mClazzName ,methodName));
        mv.visitMethodInsn(INVOKEVIRTUAL, FilterAction.AStringBuilder.OWNER, FilterAction.AStringBuilder.NAME_APPEND, FilterAction.AStringBuilder.DESC_APPEND_S, false);
        mv.visitVarInsn(LLOAD, var2);
        mv.visitVarInsn(LLOAD, startTimeIndex);
        mv.visitInsn(LSUB);
        mv.visitMethodInsn(INVOKEVIRTUAL, FilterAction.AStringBuilder.OWNER, FilterAction.AStringBuilder.NAME_APPEND, FilterAction.AStringBuilder.DESC_APPEND_I, false);
        mv.visitLdcInsn("ms");
        mv.visitMethodInsn(INVOKEVIRTUAL, FilterAction.AStringBuilder.OWNER, FilterAction.AStringBuilder.NAME_APPEND, FilterAction.AStringBuilder.DESC_APPEND_S, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, FilterAction.AStringBuilder.OWNER, FilterAction.AStringBuilder.NAME_TOSTRING, FilterAction.AStringBuilder.DESC_TOSTRING, false);
        mv.visitMethodInsn(INVOKESTATIC, FilterAction.ALog.OWNER, FilterAction.ALog.NAME_I, FilterAction.ALog.DESC, false);
        mv.visitInsn(POP);

    }


    /**
     * 防止连续点击字节码插入
     * @param index
     */
    private void onAutoInterceptFastClick(int index) {
        //不存在参数View
        if(index == -1){
            printLog("Not find parameter of View. Cannot intercept view's fast click.");
            return;
        }
        Map<String,String> map = mAnnotationMap.get(FilterAction.ACTION_FAST_CLICK);
        boolean hasTime = map.containsKey("intervalTime");
        printLog("@FastClickIntercept parameters " + map.toString());
        printLog("Insert fast click for Views.Time is " + (hasTime ? map.get("intervalTime") : 500) + "ms.");
        //第几个参数 index 从0开始，load字节码时是从1开始 故要加1
        int vi = index+1;
        mv.visitVarInsn(ALOAD, vi);
        mv.visitTypeInsn(INSTANCEOF,FilterAction.AView.OWNER);
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ALOAD, vi);
        mv.visitMethodInsn(INVOKEVIRTUAL, FilterAction.AView.OWNER, FilterAction.AView.NAME_GETID, FilterAction.AView.DESC_GETID, false);
        if(hasTime){//设置了自定义时间
            mv.visitLdcInsn(Integer.parseInt(map.get("intervalTime")));
            mv.visitMethodInsn(INVOKESTATIC,FilterAction.AFastClick.OWNER,FilterAction.AFastClick.NAME,FilterAction.AFastClick.DESC_2,false);
        }else {//未设置自定义时间，默认为500ms
            mv.visitMethodInsn(INVOKESTATIC,FilterAction.AFastClick.OWNER,FilterAction.AFastClick.NAME,FilterAction.AFastClick.DESC_1,false);
        }
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
    }

    private void printVisitClassLog(){
        if(!FilterAction.isPrintConsole){
            System.out.println("> ----------- " + this.mClazzName);
            FilterAction.isPrintConsole = true;
        }
    }

    /**
     * 增加日志输出
     * @param msg
     */
    private void printLog(String msg){
        System.out.println("> <" + methodName + "> " + msg);
    }

    /**
     * 检查过滤条件是否需要插入日志
     * @return
     */
    private boolean isFilterLog(){
        return isContainKey(FilterAction.ACTION_LOG);
    }

    /**
     * 检查过滤条件是否是统计该方法的耗时
     * @return
     */
    private boolean isFilterTimeCost(){
        return isContainKey(FilterAction.ACTION_TIME_COST);
    }

    /**
     * 检查过滤条件是否是防快速点击
     * @return
     */
    private boolean isInterceptFastClick(){
        return isContainKey(FilterAction.ACTION_FAST_CLICK);
    }


    /**
     * 是否包含指定的值
     * @param value
     * @return
     */
    private boolean isContainKey(String value) {
        return mAnnotationMap != null && mAnnotationMap.size() > 0 && mAnnotationMap.containsKey(value);
    }

    /**
     * 检索获取参数View的索引位置
     */
    private int getArgIndex(String argDes) {
        if (argTypes != null && argTypes.length > 0) {
            for (int i = 0; i < argTypes.length; i++) {
//                System.out.println("getViewArgIndex -->> " + argTypes[i].toString());
                if (argTypes[i].toString().equals(argDes)) {
                    return i;
                }
            }
        }

        return -1;
    }
}
