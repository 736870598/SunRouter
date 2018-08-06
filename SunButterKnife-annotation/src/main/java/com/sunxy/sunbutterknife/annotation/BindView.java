package com.sunxy.sunbutterknife.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/6 0006.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value() default 0;
}
