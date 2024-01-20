package com.crypto.jtrade.common.util;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.constants.SerializeType;
import com.crypto.jtrade.common.util.myserialize.MyFieldInfo;
import com.crypto.jtrade.common.util.myserialize.MyPropertyNamingStrategy;
import com.crypto.jtrade.common.util.myserialize.MySerializeException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeUtils {
    
    public static List<MyFieldInfo> computeFields(Class<?> clazz, MyPropertyNamingStrategy propertyNamingStrategy, SerializeType serializeType) {
        Ordinal ordinal = new Ordinal(0);
        Map<String , MyFieldInfo> fieldCacheMap = new HashMap<>();
        parserAllFieldToCache(ordinal, clazz, fieldCacheMap, propertyNamingStrategy, serializeType);
        
        List<MyFieldInfo> fieldInfoList = new ArrayList<>(fieldCacheMap.values());
        Collections.sort(fieldInfoList);
        
        return fieldInfoList;
    }
    
    private static void  parserAllFieldToCache(Ordinal ordinal, Class<?> clazz, Map<String, MyFieldInfo> fieldCacheMap, MyPropertyNamingStrategy propertyNamingStrategy, SerializeType serializeType){
        Field[] fields = clazz.getDeclaredFields();
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            parserAllFieldToCache(ordinal, clazz.getSuperclass(), fieldCacheMap, propertyNamingStrategy, serializeType);
        }
        
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldCacheMap.containsKey(fieldName)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                
                boolean arrayFormat = false;
                boolean reference = false;
                boolean stringFormat = false;
                boolean key = false;
                MyField fieldAnnotation = field.getAnnotation(MyField.class);
                String propertyName = field.getName();
                if (fieldAnnotation != null) {
                    if ((serializeType == SerializeType.TEXT && !fieldAnnotation.text()) ||
                            (serializeType == SerializeType.JSON && !fieldAnnotation.json())) {
                        continue;
                    }
                    if (fieldAnnotation.name().length() != 0) {
                        propertyName = fieldAnnotation.name();
                    }
                    arrayFormat = fieldAnnotation.arrayFormat();
                    reference = fieldAnnotation.reference();
                    stringFormat = fieldAnnotation.stringFormat();
                    key = fieldAnnotation.key();
                }
                if (propertyNamingStrategy != null) {
                    propertyName = propertyNamingStrategy.translate(propertyName);
                }
                
                StringBuilder methodName = new StringBuilder();
                methodName.append(fieldName.substring(0, 1).toUpperCase());
                methodName.append(fieldName.substring(1));
                String getMethodName = "get" + methodName.toString() + "()";
                String setMethodName = "set" + methodName.toString();
                
                MyFieldInfo fieldInfo = new MyFieldInfo(ordinal.getAndInc(), propertyName, field, getMethodName, setMethodName, arrayFormat, reference, stringFormat, key);
                fieldCacheMap.put(fieldName, fieldInfo);
            }
        }
    }
    
    public static Class<?> getCollectionItemClass(Type fieldType) {
        if (fieldType instanceof ParameterizedType) {
            Class<?> itemClass;
            Type actualTypeArgument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            if (actualTypeArgument instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length == 1) {
                    actualTypeArgument = upperBounds[0];
                }
            }
            if (actualTypeArgument instanceof Class) {
                itemClass = (Class<?>) actualTypeArgument;
                if (!Modifier.isPublic(itemClass.getModifiers())) {
                    throw new MySerializeException("can not create ASMParser");
                }
            } else {
                throw new MySerializeException("can not create ASMParser");
            }
            return itemClass;
        }
        return Object.class;
    }
    
    
    public static class Ordinal {
        private int ordinal;
        
        public Ordinal(int ordinal) {
            this.ordinal = ordinal;
        }
        
        public int getAndInc() {
            return this.ordinal++;
        }
    }
    
}
