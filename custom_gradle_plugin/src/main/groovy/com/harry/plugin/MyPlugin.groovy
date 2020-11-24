package com.harry.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project>{

    @SuppressWarnings("NullableProblems")
    @Override
    void apply(Project project) {
        System.out.println("========================")
        System.out.println("hello gradle plugin!")

        AppExtension appExtension = (AppExtension)project.getProperties().get("android")
        appExtension.registerTransform(new CustomTransform(project), Collections.EMPTY_LIST)

        System.out.println("========================")
    }
}