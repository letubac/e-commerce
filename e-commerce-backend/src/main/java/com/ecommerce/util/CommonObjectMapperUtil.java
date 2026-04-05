package com.ecommerce.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

/**
 * author: LeTuBac
 */
public class CommonObjectMapperUtil {

    private static ModelMapper modelMapper = new ModelMapper();
    
    private CommonObjectMapperUtil() {
        
    }
    
    /**
     * 
     * mapAll
     * @param <O>
     * @param <S>
     * @param sourceList
     * @param outCLass
     * @return List object after mapper
     * @author BacLV
     */
    public static <O, S> List<O> mapAll(final Collection<S> sourceList, Class<O> outCLass) {
        return sourceList.stream().map(entity -> modelMapper.map(entity, outCLass)).collect(Collectors.toList());
    }
    
}