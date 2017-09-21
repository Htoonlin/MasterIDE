package com.sdm.ide.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FXColumn {

    String label() default "";

    boolean visible() default true;

    boolean editable() default false;

    boolean sortable() default true;

    double width() default 75.0;
}
