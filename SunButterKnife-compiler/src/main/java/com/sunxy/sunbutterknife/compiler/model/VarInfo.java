package com.sunxy.sunbutterknife.compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
public class VarInfo {

    public String varName;
    public int value;
    private TypeName typeName;

    public VarInfo(String varName, int value, TypeName typeName) {
        this.varName = varName;
        this.value = value;
        this.typeName = typeName;
    }

    public ClassName getRawType() {
        if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).rawType;
        }
        return (ClassName) typeName;
    }
}
