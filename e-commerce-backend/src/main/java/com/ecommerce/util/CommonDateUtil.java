package com.ecommerce.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



/**
 * author: LeTuBac
 */
public class CommonDateUtil extends MovieDateUtil {
    
    /** The Constant DATE_TIME_HYPHEN. */
    public static final String DATE_TIME_HYPHEN = "dd/MM/yyyy HH:mm:ss";
	public static final String DDMMYYYY_HYPHEN = "dd/MM/yyyy";
   
    public static Date setMaxTime(Date date) {
        date = MovieDateUtil.truncate(date, Calendar.DATE);
        return MovieDateUtil.addMilliseconds(MovieDateUtil.addDays(date, 1), -1);
    }
   
    public static Date removeTime(Date date) {
        return MovieDateUtil.truncate(date, Calendar.DATE);
    }
   
    public static Date getSystemDateTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }
    
    public static Date getSystemDate() {
        Date sysDate = getSystemDateTime();
        return removeTime(sysDate);
    }
    
    public static Date parseDate(String dateString, String format) {
        try {
            return MovieDateUtil.parseDate(dateString, format);
        } catch (ParseException e) {
            return null;
        }
    }
    
    public static Date addDate(Date offsetDate, int addDay) {
        return MovieDateUtil.addDays(offsetDate, addDay);
    }
    
    public static String formatDateToString(Date date, String pattern) {
        String result = null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        if (date != null) {
            result = simpleDateFormat.format(date);
        }

        return result;
    }
    
    
    public static Date formatStringToDate(String dateString, String format) throws ParseException{
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(dateString);
    }
    
    public static Date formatSearchToDate(Date toDate) throws ParseException{
      Date faceToDate = null;
        if(toDate!=null) {
            faceToDate = toDate;
            faceToDate.setHours(23);
            faceToDate.setMinutes(59);
            faceToDate.setSeconds(59);
        }
        return faceToDate;
    }
    
    public static Date formatSearchToStartDate(Date toDate) throws ParseException{
        Date faceToDate = null;
          if(toDate!=null) {
              faceToDate = toDate;
              faceToDate.setHours(0);
              faceToDate.setMinutes(0);
              faceToDate.setSeconds(0);
          }
          return faceToDate;
      }
    
    

}
