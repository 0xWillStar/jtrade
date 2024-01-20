package com.crypto.jtrade.common.annotation;

import com.crypto.jtrade.common.util.myserialize.MyPropertyNamingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface MyType {
    
    boolean arrayFormat() default false;
    
    boolean stringFormat() default false;
    
    String table() default "";
    
    MyPropertyNamingStrategy naming() default MyPropertyNamingStrategy.NeverUseThisValueExceptDefaultValue;
    
}
