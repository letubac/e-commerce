package com.ecommerce.service;

import java.util.Date;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.exception.DetailException;

@Service
/**
 * author: LeTuBac
 */
public interface CommonService {

    Date getSystemDate();

    Date getSystemDateTime();

    String generateCodeFromId(Long id);

    String generateCodeFromIdWithFormat(Long id, String format);

    public Sort buildSortAlias(Sort sort, Class<?> clazz, String alias) throws DetailException;

    Sort buildSortAliasNotUseDefault(Sort sort, Class<?> clazz, String alias) throws DetailException;

    <T extends Enum<T>> Sort buildSortEnums(Sort sort, T[] enumsDatas,Class<?> clazz) throws DetailException;

    String generateTitleS(String code, String title);

	Object parseValueByDataType(String value, String type);

    String generateDocumentCode(Long seqId, String prefix, String format);
}
