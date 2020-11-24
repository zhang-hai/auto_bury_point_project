package com.harry2815.burypoint.common;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by zhanghai on 2019/7/18.
 * function：
 */
public class CommonClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;//class's name

    public CommonClassVisitor( ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new CommonMethodVisitor(ASM5,mv,access,name,desc,this.mClassName);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if(FilterAction.isPrintConsole){
            System.out.println("");
        }
        FilterAction.isPrintConsole = false;
    }
}
