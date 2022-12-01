package com.example.coursecompiler;

public class ClassUtil {
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            return obj.getClass().getField(fieldName).get(obj);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

}


