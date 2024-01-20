package com.crypto.jtrade.common.util.myserialize;

import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.SerializeType;
import com.crypto.jtrade.common.util.TypeUtils;

import java.math.BigDecimal;
import java.util.List;

public class MySerializeUtils {
    
    public static String toJSONCode(Class<?> clazz) {
        ItemNameObject itemNameObject = new ItemNameObject("item");
        FirstNameObject firstNameObject = new FirstNameObject("first");
        return getJSONCode(clazz, true, null, itemNameObject, null, null, SerializeType.JSON, firstNameObject);
    }
    
    public static String toStringCode(Class<?> clazz) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, MyPropertyNamingStrategy.NoChange, SerializeType.TEXT);
        
        StringBuilder code = new StringBuilder();
        code.append("StringBuilder sb = new StringBuilder(512);\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            
            // a new field beginning
            code.append("if (").append(fieldInfo.getMethodName).append(" != null) {\n");
            
            code.append("sb.append(").append(fieldInfo.getMethodName).append(");\n");
            
            // a new field ending
            code.append("}\n");
            
            if (i < fieldInfoList.size() - 1) {
                code.append("sb.append(\",\");\n");
            }
        }
        code.append("return sb.toString();\n");
        
        return code.toString();
    }
    
    public static String toObjectCode(Class<?> clazz) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, MyPropertyNamingStrategy.NoChange, SerializeType.TEXT);
        String className = clazz.getSimpleName();
        
        StringBuilder code = new StringBuilder();
        code.append(className).append(" obj = new ").append(className).append("();\n");
        code.append("String[] values = StringUtils.splitPreserveAllTokens(str, ',');\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            Class<?> fieldType = fieldInfo.field.getType();
            
            // a new field beginning
            code.append("if (!values[").append(i).append("].equals(\"\")) {\n");
            
            code.append("obj.").append(fieldInfo.setMethodName).append("(");
            
            String value = "values[" + i + "]";
            if (fieldType == String.class) {
                code.append(value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                code.append("Boolean.valueOf(").append(value).append(")");
            } else if (fieldType == int.class || fieldType == Integer.class) {
                code.append("Integer.parseInt(").append(value).append(")");
            } else if (fieldType == long.class || fieldType == Long.class) {
                code.append("Long.parseLong(").append(value).append(")");
            } else if (fieldType == BigDecimal.class) {
                code.append("new BigDecimal(").append(value).append(")");
            } else if (fieldType.isEnum()) {
                code.append(fieldType.getSimpleName()).append(".valueOf(").append(value).append(")");
            } else {
                throw new MySerializeException("unsupported data type");
            }
            
            code.append(");\n");
            
            // a new field ending
            code.append("}\n\n");
        }
        code.append("return obj;\n");
        
        return code.toString();
    }
    
    private static String getJSONCode(Class<?> clazz, boolean isNew, String parentName, ItemNameObject itemNameObject, MyPropertyNamingStrategy propertyNamingStrategy,
            Boolean stringFormat, SerializeType serializeType, FirstNameObject firstNameObject) {
        boolean arrayFormat = false;
        if (isNew) {
            MyType classType = clazz.getAnnotation(MyType.class);
            propertyNamingStrategy = classType == null ? MyPropertyNamingStrategy.NoChange : classType.naming();
            stringFormat = classType == null ? false : classType.stringFormat();
            arrayFormat = classType == null ? false : classType.arrayFormat();
        }
        
        StringBuilder code = new StringBuilder();
        if (isNew) {
            code.append("StringBuilder sb = new StringBuilder(1024);\n");
        }
        
        if (arrayFormat) {
            code.append(getItemArrayCode(clazz, propertyNamingStrategy, null, stringFormat, serializeType));
            
        } else {
            List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, propertyNamingStrategy, serializeType);
            
            String firstName = firstNameObject.getAndInc();
            code.append("boolean ").append(firstName).append(" = true;\n");
            
            code.append("sb.append(\"{\");\n");
            for (int i = 0; i < fieldInfoList.size(); i++) {
                MyFieldInfo fieldInfo = fieldInfoList.get(i);
                Class<?> fieldType = fieldInfo.field.getType();
                String methodName = parentName == null ? fieldInfo.getMethodName : parentName + "." + fieldInfo.getMethodName;
                
                // a new field beginning
                code.append("if (").append(methodName).append(" != null) {\n");
                
                code.append("if (").append(firstName).append(") {\n");
                code.append(firstName).append(" = false;\n");
                code.append("} else {\n");
                code.append("sb.append(\",\");\n");
                code.append("}\n\n");
                
                code.append("sb.append(\"\\\"").append(fieldInfo.name).append("\\\":");
                
                if (List.class.isAssignableFrom(fieldType)) {
                    Class<?> itemClass = TypeUtils.getCollectionItemClass(fieldInfo.field.getGenericType());
                    String className = itemClass.getSimpleName();
                    
                    code.append("[\");\n");
                    
                    // for loop beginning
                    code.append("for (int i = 0; i < ").append(methodName).append(".size(); i++) {\n");
                    String itemName = itemNameObject.getAndInc();
                    code.append(className).append(" ").append(itemName).append(" = ").append(methodName).append(".get(i);\n");
                    
                    if (fieldInfo.reference) {
                        if (fieldInfo.arrayFormat) {
                            code.append(getItemArrayCode(itemClass, propertyNamingStrategy, itemName, stringFormat, serializeType));
                        } else {
                            code.append(getJSONCode(itemClass, false, itemName, itemNameObject, propertyNamingStrategy, stringFormat, serializeType, firstNameObject));
                        }
                        
                    } else {
                        code.append("if (").append(itemName).append(" != null) {\n");
                        
                        boolean isString = itemClass == String.class || fieldType.isEnum() || fieldInfo.stringFormat || stringFormat;
                        if (isString) {
                            code.append("sb.append(\"\\\"\");\n");
                        }
                        code.append("sb.append(").append(itemName).append(");\n");
                        if (isString) {
                            code.append("sb.append(\"\\\"\");\n");
                        }
                        
                        code.append("}\n");
                    }
                    
                    code.append("if (i < ").append(fieldInfo.getMethodName).append(".size() - 1) {\n");
                    code.append("sb.append(\",\");\n");
                    code.append("}\n");
                    // for loop ending
                    code.append("}\n");
                    
                    code.append("sb.append(\"]\");\n");
                    
                } else {
                    if (fieldInfo.reference) {
                        code.append("\");\n");
                        if (fieldInfo.arrayFormat) {
                            code.append(getItemArrayCode(fieldType, propertyNamingStrategy, fieldInfo.field.getName(), stringFormat, serializeType));
                        } else {
                            code.append(getJSONCode(fieldType, false, fieldInfo.field.getName(), itemNameObject, propertyNamingStrategy, stringFormat, serializeType, firstNameObject));
                        }
                        
                    } else {
                        boolean isString = fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat || stringFormat;
                        if (isString) {
                            code.append("\\\"");
                        }
                        code.append("\").append(").append(methodName).append(")");
                        if (isString) {
                            code.append(".append(\"\\\"\")");
                        }
                        code.append(";\n");
                    }
                }
                
                // a new field ending
                code.append("}\n\n");
            }
            code.append("sb.append(\"}\");\n");
        }
        
        if (isNew) {
            code.append("return sb.toString();\n");
        }
        
        return code.toString();
    }
    
    private static String getItemArrayCode(Class<?> clazz, MyPropertyNamingStrategy propertyNamingStrategy, String parentName, boolean stringFormat, SerializeType serializeType) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, propertyNamingStrategy, serializeType);
        
        StringBuilder code = new StringBuilder();
        code.append("sb.append(\"[\");\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            String methodName = parentName == null ? fieldInfo.getMethodName : parentName + "." + fieldInfo.getMethodName;
            Class<?> fieldType = fieldInfo.field.getType();
            boolean isString = fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat || stringFormat;
            
            code.append("if (").append(methodName).append(" != null) {\n");
            if (isString) {
                code.append("sb.append(\"\\\"\");\n");
            }
            code.append("sb.append(").append(methodName).append(");\n");
            if (isString) {
                code.append("sb.append(\"\\\"\");\n");
            }
            code.append("}\n");
            
            if (i < fieldInfoList.size() - 1) {
                code.append("sb.append(\",\");\n");
            }
        }
        code.append("sb.append(\"]\");\n");
        
        return code.toString();
    }
    
    public static class ItemNameObject {
        
        private String itemName;
        
        private int ordinal;
        
        public ItemNameObject(String itemName) {
            this.itemName = itemName;
            this.ordinal = 0;
        }
        
        public String getAndInc() {
            if (this.ordinal == 0) {
                this.ordinal++;
                return itemName;
            } else {
                return itemName + ordinal++;
            }
        }
    }
    
    public static class FirstNameObject {
        
        private String firstName;
        
        private int ordinal;
        
        public FirstNameObject(String firstName) {
            this.firstName = firstName;
            this.ordinal = 0;
        }
        
        public String getAndInc() {
            if (this.ordinal == 0) {
                this.ordinal++;
                return firstName;
            } else {
                return firstName + ordinal++;
            }
        }
    }
    
}
