package com.sunxy.sunbutterknife.core;

import android.app.Activity;
import android.view.View;

import com.sunxy.sunbutterknife.annotation.UnBind;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
public class SunBfCore {

    private static Map<Class<?>, Constructor<?>> constructorMap = new HashMap<>();

    public static UnBind bind(Activity target){
        Class<?> cls = target.getClass();
        Constructor<?> constructor = getConstructor(cls);
        if (constructor != null){
            try {
                return (UnBind) constructor.newInstance(target, target.getWindow().getDecorView());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Constructor<?> getConstructor(Class<?> cls){
        try{
            Constructor<?> constructor = constructorMap.get(cls);
            if (constructor == null){
                String bindViewClassName = cls.getName() + "_BindView";
                Class<?> bindingClass = cls.getClassLoader().loadClass(bindViewClassName);
                constructor = bindingClass.getConstructor(cls, View.class);
                constructorMap.put(cls, constructor);
            }
            return constructor;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
