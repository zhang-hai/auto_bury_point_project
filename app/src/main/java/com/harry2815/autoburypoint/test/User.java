package com.harry2815.autoburypoint.test;

import com.sxw.burypoint.api.annotation.MethodTraceTime;

/**
 * Created by zhanghai on 2019/8/8.
 * function：
 */
public class User {
    private String id;
    private String name;
    private int age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
