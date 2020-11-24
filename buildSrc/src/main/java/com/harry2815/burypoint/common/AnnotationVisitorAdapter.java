package com.harry2815.burypoint.common;

import org.objectweb.asm.AnnotationVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghai on 2019/7/25.
 * function：
 */
public class AnnotationVisitorAdapter extends AnnotationVisitor {
    public String desc;
    public String name;
    public String value;

    private Map<String,String> keyMap;

    public AnnotationVisitorAdapter(int api, AnnotationVisitor av, String paramDesc) {
        super(api, av);
        this.desc = paramDesc;
        keyMap = new HashMap<>();
    }

    @Override
    public void visit(String paramName, Object paramValue) {
        this.name = paramName;
        this.value = paramValue.toString();
//        System.out.println("visitAnnotation: name=" + name + " value=" + value);
        keyMap.put(name,value);
    }

    /**
     * 返回注解参数值
     * @return
     */
    public Map<String,String> getKeyMap(){return keyMap;}
}
