package com.ecommerce.util;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
/**
 * MovieCollectionUtil
 * 
 * @version 01-00
 * @since 01-00
 * @author BacLV
 */
public class MovieCollectionUtil {

    @SuppressWarnings("rawtypes")
    public static final Collection EMPTY_COLLECTION = CollectionUtils.EMPTY_COLLECTION;


    /**
     * 
     * isNotEmpty
     * @param collection
     * @return
     * @author BacLV
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return CollectionUtils.isNotEmpty(collection);
    }


    /**
     * 
     * isEmpty
     * @param collection
     * @return
     * @author BacLV
     */
    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }


    /**
     * 
     * size
     * @param collection
     * @return
     * @author BacLV
     */
    public static int size(Collection<?> collection) {
        return CollectionUtils.size(collection);
    }
}
