package com.crypto.jtrade.common.util.myserialize;

import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.SerializeType;
import com.crypto.jtrade.common.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class MySqlUtils {
    
    public static String getInsertSql(Class<?> clazz) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, MyPropertyNamingStrategy.SnakeCase, SerializeType.TEXT);
    
        StringBuilder code = new StringBuilder();
        code.append("String[] values = StringUtils.splitPreserveAllTokens(data, ',');\n");
        code.append("StringBuilder sb = new StringBuilder(1024);\n");
        
        // generate columns beginning
        StringBuilder columns = new StringBuilder();
        MyType classType = clazz.getAnnotation(MyType.class);
        String table = classType == null ? "" : classType.table();
        columns.append("INSERT INTO ").append(table).append("(");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            columns.append(fieldInfo.name);
            if (i < fieldInfoList.size() - 1) {
                columns.append(",");
            }
        }
        columns.append(")");
        // generate columns ending
        code.append("sb.append(\"").append(columns).append("\");\n");
        
        // generate values beginning
        code.append("sb.append(\"VALUES(\");\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            Class<?> fieldType = fieldInfo.field.getType();

            code.append("// ").append(fieldInfo.name).append("\n");
            code.append("if (values[").append(i).append("].equals(\"\")) {\n");
            code.append("sb.append(\"null\");\n");
            code.append("} else {\n");
    
            boolean isString = fieldType == boolean.class || fieldType == Boolean.class || fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat;
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
            code.append("sb.append(values[").append(i).append("]);\n");
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
            
            code.append("}\n");
            
            if (i < fieldInfoList.size() - 1) {
                code.append("sb.append(\",\");\n");
            }
        }
        // generate values ending
        code.append("sb.append(\")\");\n");
        
        code.append("return sb.toString();\n");
        return code.toString();
    }
    
    
    public static String getUpdateSql(Class<?> clazz) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, MyPropertyNamingStrategy.SnakeCase, SerializeType.TEXT);
    
        List<MyFieldInfo> columnFields = new ArrayList<>();
        List<MyFieldInfo> keyFields = new ArrayList<>();
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            if (fieldInfo.key) {
                keyFields.add(fieldInfo);
            } else {
                columnFields.add(fieldInfo);
            }
        }
    
        StringBuilder code = new StringBuilder();
        code.append("String[] values = StringUtils.splitPreserveAllTokens(data, ',');\n");
        code.append("StringBuilder sb = new StringBuilder(1024);\n");
    
        MyType classType = clazz.getAnnotation(MyType.class);
        String table = classType == null ? "" : classType.table();
        code.append("sb.append(\"UPDATE ").append(table).append(" SET \");\n");
        for (int i = 0; i < columnFields.size(); i++) {
            MyFieldInfo fieldInfo = columnFields.get(i);
            Class<?> fieldType = fieldInfo.field.getType();
            code.append("sb.append(\"").append(fieldInfo.name).append("=\");\n");
            
            code.append("if (values[").append(fieldInfo.ordinal).append("].equals(\"\")) {\n");
            code.append("sb.append(\"null\");\n");
            code.append("} else {\n");
    
            boolean isString = fieldType == boolean.class || fieldType == Boolean.class || fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat;
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
            code.append("sb.append(values[").append(fieldInfo.ordinal).append("]);\n");
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
    
            code.append("}\n");
    
            if (i < columnFields.size() - 1) {
                code.append("sb.append(\",\");\n");
            }
        }
        
        code.append("sb.append(\" WHERE \");\n");
        for (int i = 0; i < keyFields.size(); i++) {
            MyFieldInfo fieldInfo = keyFields.get(i);
            Class<?> fieldType = fieldInfo.field.getType();
            code.append("sb.append(\"").append(fieldInfo.name).append("=\");\n");
        
            code.append("if (values[").append(fieldInfo.ordinal).append("].equals(\"\")) {\n");
            code.append("sb.append(\"null\");\n");
            code.append("} else {\n");
        
            boolean isString = fieldType == boolean.class || fieldType == Boolean.class || fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat;
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
            code.append("sb.append(values[").append(fieldInfo.ordinal).append("]);\n");
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
        
            code.append("}\n");
        
            if (i < keyFields.size() - 1) {
                code.append("sb.append(\" AND \");\n");
            }
        }
    
        code.append("return sb.toString();\n");
        return code.toString();
    }
    
    
    public static String getDeleteSql(Class<?> clazz) {
        List<MyFieldInfo> fieldInfoList = TypeUtils.computeFields(clazz, MyPropertyNamingStrategy.SnakeCase, SerializeType.TEXT);
    
        List<MyFieldInfo> keyFields = new ArrayList<>();
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            if (fieldInfo.key) {
                keyFields.add(fieldInfo);
            }
        }
    
        StringBuilder code = new StringBuilder();
        code.append("String[] values = StringUtils.splitPreserveAllTokens(data, ',');\n");
        code.append("StringBuilder sb = new StringBuilder(512);\n");
    
        MyType classType = clazz.getAnnotation(MyType.class);
        String table = classType == null ? "" : classType.table();
        code.append("sb.append(\"DELETE FROM ").append(table).append(" WHERE \");\n");
    
        for (int i = 0; i < keyFields.size(); i++) {
            MyFieldInfo fieldInfo = keyFields.get(i);
            Class<?> fieldType = fieldInfo.field.getType();
            code.append("sb.append(\"").append(fieldInfo.name).append("=\");\n");
        
            code.append("if (values[").append(fieldInfo.ordinal).append("].equals(\"\")) {\n");
            code.append("sb.append(\"null\");\n");
            code.append("} else {\n");
        
            boolean isString = fieldType == boolean.class || fieldType == Boolean.class || fieldType == String.class || fieldType.isEnum() || fieldInfo.stringFormat;
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
            code.append("sb.append(values[").append(fieldInfo.ordinal).append("]);\n");
            if (isString) {
                code.append("sb.append(\"'\");\n");
            }
        
            code.append("}\n");
        
            if (i < keyFields.size() - 1) {
                code.append("sb.append(\" AND \");\n");
            }
        }
    
        code.append("return sb.toString();\n");
        return code.toString();
    }

}
