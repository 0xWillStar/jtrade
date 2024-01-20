package com.crypto.jtrade.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface MyField {
    
    String name() default "";
    
    boolean text() default true;
    
    boolean json() default true;
    
    boolean arrayFormat() default false;
    
    boolean reference() default false;
    
    boolean stringFormat() default false;
    
    boolean key() default false;
    
}
