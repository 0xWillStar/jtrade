package com.crypto.jtrade.common.util.myserialize;

import java.lang.reflect.Field;

public class MyFieldInfo implements Comparable<MyFieldInfo> {
    
    public final int ordinal;
    
    public final String name;
    
    public final Field field;
    
    public final String getMethodName;
    
    public final String setMethodName;
    
    public final boolean arrayFormat;
    
    public final boolean reference;
    
    public final boolean stringFormat;
    
    public final boolean key;
    
    
    public MyFieldInfo(int ordinal, String name, Field field, String getMethodName, String setMethodName, boolean arrayFormat, boolean reference, boolean stringFormat, boolean key) {
        this.ordinal = ordinal;
        this.name = name;
        this.field = field;
        this.getMethodName = getMethodName;
        this.setMethodName = setMethodName;
        this.arrayFormat = arrayFormat;
        this.reference = reference;
        this.stringFormat = stringFormat;
        this.key = key;
    }
    
    @Override
    public int compareTo(MyFieldInfo o) {
        if (this.ordinal > o.ordinal) {
            return 1;
        } else if (this.ordinal < o.ordinal) {
            return -1;
        } else {
            return 0;
        }
    }
}
