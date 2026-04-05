package com.ecommerce.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * author: LeTuBac
 */
public class CommonStringUtil extends StringUtils{

	public static boolean isNotBlank(final CharSequence cs) {
		return StringUtils.isNotBlank(cs);
	}

    public static String repeat(final String str, final int repeat) {
    	return StringUtils.repeat(str, repeat);
    }
    public static String substring(final String str, int start) {
    	return StringUtils.substring(str, start);
    }

    public static String removeAccent(String str) {
        str = str.replaceAll("Đ", "D");
        str = str.replaceAll("đ", "d");
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    public static String removeSpecial(String str) {
        return str.replaceAll("[^A-Za-z0-9]","");
    }
}
