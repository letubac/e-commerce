package com.ecommerce.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

/**
 * author: LeTuBac
 */
public class MovieDateUtil extends DateUtils{

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String YYYYMMDD_HYPHEN = "yyyy-MM-dd";

    public static final String YYYYMM = "yyyyMM";

    public static final String YYYYMM_HYPHEN = "yyyy-MM";

    public static final String YYMMDD = "yyMMdd";

    public static final String YYMMDD_HYPHEN = "yy-MM-dd";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    
    public static final String YYYYMMDDHHMMSS_HYPHEN = "yyyy-MM-dd HH:mm:ss";
    
    public static final String YYYYMMDDHHMMSSFFF = "yyyyMMddHHmmssFFF";

    public static final String YYYYMMDDHHMMSSFFF_HYPHEN = "yyyy-MM-dd HH:mm:ss.FFF";
    
    public static final String DDMMYYYY_HYPHEN = "dd/MM/yyyy";
    
    public static final String END_OF_DAY = "235959";
    
    public static String formatDateToString(Date date, String pattern) {
		String result = null;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		
		if (date != null) {
			result = simpleDateFormat.format(date);
		}

		return result;
	}
	
	public static Date getSystemDateTime() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}
	
	public static Date getSystemDate() {
		Date sysDate = getSystemDateTime();
		return removeTime(sysDate);
	}
	
	public static Date setMaxTime(Date date) {
		date = removeTime(date);
		
		return DateUtils.addMilliseconds(
				DateUtils.addDays(date, 1), -1);
	}
	
	public static Date removeTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// Clear time
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date addWorkingDate(Date offsetDate, int addDay, List<Date> holidays) {
		Date addResult = offsetDate;
		Calendar c = Calendar.getInstance();
		for (int day = 1; day <= addDay; ++day) {
			c.setTime(addResult);
			c.add(Calendar.DATE, 1);
			addResult = c.getTime();
			if (isHoliday(addResult, holidays)) {
				addDay = addDay + 1;
			}
		}
		return addResult;
	}

	public static Date addDate(Date offsetDate, int addDay) {
		Date addResult = offsetDate;
		Calendar c = Calendar.getInstance();
		c.setTime(addResult);
		c.add(Calendar.DATE, addDay);
		return c.getTime();
	}

	public static Date subtractDate(Date offsetDate, int subtractDay) {
		Date addResult = offsetDate;
		Calendar c = Calendar.getInstance();
		c.setTime(addResult);
		c.add(Calendar.DATE, subtractDay * (-1));
		return c.getTime();
	}

	public static boolean isHoliday(Date date, List<Date> holidays) {
		for (Date holiday : holidays) {
			if (date.equals(holiday)) {
				return true;
			}
		}
		return false;
	}

	public static boolean dateAfterDate(Date expiredDate, Date effectedDate) {
		return !expiredDate.before(effectedDate);
	}

	public static Date formatStringToDate(String dateString, String format) throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(dateString);
	}
	
	public static Date addHours(Date date, int hours) {
		Date newDate = DateUtils.addHours(date, hours);
		return newDate;

	}
	
	public static Date subtractHours(Date date, int hours) {
		Date newDate = DateUtils.addHours(date, -hours);
		return newDate;

	}
	
	public static Date addMinutes(Date date, int minutes) {
		Date newDate = DateUtils.addMinutes(date, minutes);
		return newDate;

	}
	
	public static Date subtractMinutes(Date date, int minutes) {
		Date newDate = DateUtils.addMinutes(date, -minutes);
		return newDate;
	}
	
	public static String[][] formatsPattern = new String[][] {
	    {"\\d+/\\d+/\\d+", "dd/MM/yyyy"},
	    {"\\p{Alpha}+\\s+\\d+\\s+\\d+", "MMM dd yy"}, 
	    {"\\d+\\s+\\p{Alpha}+\\s+\\d+", "dd MMM yy"}, 
	    {"\\d+-\\p{Alpha}+-\\d+", "dd-MMM-yy"},
	    {"\\d+-\\p{Alpha}+-\\d+", "dd-MMM-yyyy"},
	    {"\\d+-\\d+-\\d+", "dd-MM-yyyy"},
	};
	
	public static Date string2Date(String datestr) {
	    Date result;
	    for (String[] format : formatsPattern) {
	        if ((result = string2Date(datestr, format[0], format[1])) != null)
	                return result;
	    }
	    return null;
	}
	
	public static Date string2Date(String datestr, String regex, String dateformat) {
	    datestr = datestr.trim();
	    try {
	        if (datestr.matches(regex)) {
	            DateFormat df = new SimpleDateFormat(dateformat);
	            df.setLenient(false);
	            Date date = df.parse(datestr);
	            return date;
	        }
	    } catch (ParseException ex) {
	    	
	    }
	    return null;
	}
	
	public static boolean isLeapYear(long prolepticYear) {
        return ((prolepticYear & 3) == 0) && ((prolepticYear % 100) != 0 || (prolepticYear % 400) == 0);
    }
}
