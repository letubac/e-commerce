package com.ecommerce.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;

/**
 * author: LeTuBac
 */
public class CommonObjectUtil {
    
    /** The column default. */
    private static String COLUMN_DEFAULT = "ID";

    private CommonObjectUtil() {

    }

    public static List<String> getColumnListFromEntity(Class<?> entityClass) {
        List<String> result = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields(); // private fields
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                result.add(column.name());
            }
        }
        return result;
    }
    
    public static String getColumnListFromEntity(Class<?> entityClass, String name) throws Exception{
        String result = COLUMN_DEFAULT;
        Field field;
        Class<?> current = entityClass;
        while(current.getSuperclass()!=null){ // we don't want to process Object.class
            // do something with current's fields
            try {
                field = current.getDeclaredField(name);
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    result = column.name();
                	current = current.getSuperclass();
                }
            } catch (NoSuchFieldException e) {
            	current = current.getSuperclass();
                continue;
            }
        }
        return result;
    }

    public static void copyPropertiesNonNull(Object src, Object target) {
        try {
            BeanUtils.copyProperties(src, target, getArrNullPropertyName(src));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] getArrNullPropertyName(Object source) throws Exception {
        Set<String> emptyNames = new HashSet<>();
        String[] result;
        try {
            final BeanWrapper src = new BeanWrapperImpl(source);
            java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

            for (java.beans.PropertyDescriptor pd : pds) {
                Object srcValue = src.getPropertyValue(pd.getName());
                if (srcValue == null)
                    emptyNames.add(pd.getName());
            }
            result = new String[emptyNames.size()];
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return emptyNames.toArray(result);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertObjectToMap(Object obj) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            resultMap = objectMapper.convertValue(obj, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
