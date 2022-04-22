package com.yunfd.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author penelopeWu
 * @version 1.0
 * @date 2018-04-18 0:06
 */
public class DateUtil {
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE_YMD_FORMAT_DASH = "yyyy-MM-dd";
    private static final SimpleDateFormat sdf_date_format_dash = new SimpleDateFormat(PATTERN_DATE_YMD_FORMAT_DASH);
    private static final SimpleDateFormat sdf_datetime_format = new SimpleDateFormat(
            DATETIME_FORMAT);
    /**
     * 比较两个日期相差的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int getMargin(String date1, String date2) {
        int margin;
        try {
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = sdf_date_format_dash.parse(date1, pos);
            Date dt2 = sdf_date_format_dash.parse(date2, pos1);
            long l = dt1.getTime() - dt2.getTime();
            margin = (int) (l / (24 * 60 * 60 * 1000));
            return margin;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getMargin(Date date1, Date date2) {
        try {
            long l = date1.getTime() - date2.getTime();
            return (int) (l / (24 * 60 * 60 * 1000));
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getMarginTime(Date date1, Date date2) {
        long margin;
        try {
            margin = date1.getTime() - date2.getTime();
            return margin;
        } catch (Exception e) {
            return 0;
        }
    }

    public static double getDoubleMargin(String date1, String date2) {
        double margin;
        try {
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = sdf_datetime_format.parse(date1, pos);
            Date dt2 = sdf_datetime_format.parse(date2, pos1);
            long l = dt1.getTime() - dt2.getTime();
            margin = (l / (24 * 60 * 60 * 1000.00));
            return margin;
        } catch (Exception e) {
            return 0;
        }
    }

    public static double getDoubleMinute(String date1, String date2) {
        double margin;
        try {
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = sdf_datetime_format.parse(date1, pos);
            Date dt2 = sdf_datetime_format.parse(date2, pos1);
            long l = dt1.getTime() - dt2.getTime();
            margin = (l / (60 * 1000.00));
            return margin;
        } catch (Exception e) {
            return 0;
        }
    }
//获得两日期的小时差
    public static Double getDistanceTimes(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        Double hour = 0.0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            min = diff / ( 60 * 1000);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        //long[] times = {day, hour, min, sec};
        hour = (double)min /60.0;

        BigDecimal temp = new BigDecimal(hour);
        double answer = temp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return answer;
    }
//获得  日期具体是星期几   3表示星期二
    public static int getWeekDay(Date date){
        int temp=0;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        temp =  c.get(Calendar.DAY_OF_WEEK);
        return temp;
    }



    //获得  日期具体是星期几   3表示星期二
    public static int getWeekDay(String date) throws ParseException {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟

        Date dateByString=sdf.parse(date);
        int temp=0;
        Calendar c = Calendar.getInstance();
        c.setTime(dateByString);
        temp =  c.get(Calendar.DAY_OF_WEEK);
        return temp;
    }


    public static int dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date datet = null;
        try {
            datet = f.parse(datetime);
            cal.setTime(datet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) ; // 指示一个星期中的某天。


        return w;
    }


    /**
     *
     * @param dBegin    开始日期
     * @param dEnd      结束日期
     * @return          两日期中间的日期列表
     */
    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (dEnd.after(calBegin.getTime())) {
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        lDate.remove(lDate.size()-1);
        return lDate;
    }


    //String 转 Date
    public static Date StringToDate(String date) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date result = null;
        try {
            result = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }



    /**
     * 获得某个日期周一到周五的日期列表
     * @param date  待查询的日期字符串     格式要求是 "yyyy-MM-dd"
     * @param isChina 是否按国内的星期格式
     * @param dateFormat 日期格式
     * @return 周一到周日的日期字符串列表
     * @throws ParseException
     */
    public static List<String> getWeekDays(String date,String dateFormat,boolean isChina) throws ParseException{
        List<String> list  = new ArrayList<String>();
        Calendar c = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        c.setTime(sdf.parse(date));
        int currentYear=c.get(Calendar.YEAR);
        int weekIndex = c.get(Calendar.WEEK_OF_YEAR);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek==1&&isChina){
            c.add(Calendar.DAY_OF_MONTH,-1);
            String date_str = sdf.format(c.getTime());
            list=getWeekDays(date_str,dateFormat,isChina);
        }
        else{
            c.setWeekDate(currentYear, weekIndex, 1);
            for(int i=1;i<=7;i++){
                c.add(Calendar.DATE, 1);
                String date_str = sdf.format(c.getTime());
                list.add(date_str);
            }
        }
        return list;
    }
}
