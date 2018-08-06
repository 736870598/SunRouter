package com.sunxy.sunbutterknife.compiler.model;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
public class ClassInfo {

    public String className;
    public String packageName;
    public String newClassName;

    public ClassInfo(String className, String packageName, String newClassName) {
        this.className = className;
        this.packageName = packageName;
        this.newClassName = newClassName;
    }
}
