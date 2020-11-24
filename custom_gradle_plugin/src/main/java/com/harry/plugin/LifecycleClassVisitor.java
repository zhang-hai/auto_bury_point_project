package com.harry.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by zhanghai on 2019/7/17.
 * function：
 */
public class LifecycleClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;
    private boolean isNeedInsert = false;

    public LifecycleClassVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("LifecycleClassVisitor : visit -----> started ：" + name);
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if(desc.equals("com/harry2815/autoburypoint/annotation/AutoLogTest")){
            isNeedInsert = true;
        }
        return super.visitAnnotation(desc,visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("LifecycleClassVisitor : visitMethod : " + name);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if(isNeedInsert){
            if ("onCreate".equals(name) ) {
                //处理onCreate
                return new LifecycleOnCreateMethodVisitor(mv);
            } else if ("onDestroy".equals(name)) {
                //处理onDestroy
                return new LifecycleOnDestroyMethodVisitor(mv);
            }else if ("printTwo".equals(name)){
                return new LifecycleOtherMethodVisitor(ASM5,mv,access,name,desc);
            }
        }
        isNeedInsert = false;
        return mv;
    }

    @Override
    public void visitEnd() {
        System.out.println("LifecycleClassVisitor : visit -----> end");
        super.visitEnd();
    }
}
