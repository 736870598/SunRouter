package com.sunxy.router.compiler.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/3 0003.
 */
public class Log {

    private Messager messager;

    private Log(Messager messager){
        this.messager = messager;
    }

    public static Log newLog(Messager messager){
        return new Log(messager);
    }

    public void i(String msg){
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
