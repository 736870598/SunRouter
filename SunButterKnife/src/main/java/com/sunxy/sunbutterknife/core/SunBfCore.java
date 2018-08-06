package com.sunxy.sunbutterknife.core;

import android.app.Activity;
import android.view.View;

import com.sunxy.sunbutterknife.annotation.UnBind;

import java.lang.reflect.Constructor;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
public class SunBfCore {

    public static UnBind bind(Activity target){
        Class<?> cls = target.getClass();
        String bindViewClassName = cls.getName() + "_BindView";

        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(bindViewClassName);
            Constructor<?> constructor = bindingClass.getConstructor(target.getClass(), View.class);
            return (UnBind) constructor.newInstance(target, target.getWindow().getDecorView());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
