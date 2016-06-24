package com.destiny.event.scheduler.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtils {

    private static final String TAG = "DateUtils";

    private static final Locale locale = new Locale("pt", "BR");
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy", locale);

    // Formata um objeto Date no formato dd/MM/yyyy
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        return df.format(date);
    }

    // Cria um objeto Date a partir de uma string no formato dd/MM/yyyy
    public static Date createDate(String date) {
        int[] info = parseDateInfo(date);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.DAY_OF_MONTH, info[0]);
        c.set(Calendar.MONTH, info[1] - 1);
        c.set(Calendar.YEAR, info[2]);
        return c.getTime();
    }

    // Formata um string no formato dd/MM/yyyy a partir do ano, mês e dia fornecidos
    public static String formatDate(int year, int monthOfYear, int dayOfMonth) {
        return String.format(locale, "%02d/%02d/%04d", dayOfMonth, monthOfYear, year);
    }

    // Retorna a data de hoje em forma de um array
    // Posição 0: dia
    // Posição 1: mês
    // Posição 2: ano
    public static int[] today() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        return new int[]{ day, month, year };
    }

    // Retorna um data qualquer, no formato dd/MM/yyyy, em forma de um array
    // Posição 0: dia
    // Posição 1: mês
    // Posição 2: ano
    public static int[] parseDateInfo(String date) {
        try {
            String[] tokens = date.split("/");
            int day = Integer.parseInt(tokens[0]);
            int month = Integer.parseInt(tokens[1]);
            int year = Integer.parseInt(tokens[2]);

            return new int[]{ day, month, year };

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String onBungieDate(String text){
        String date = text.substring(0,text.indexOf("T"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date sinceDate = df.parse(date);
            df = new SimpleDateFormat("dd/MM/yyyy");
            String finalDate = df.format(sinceDate);
            return finalDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Log.w("DateUtils", "Data no DB: " + date);
        return date;
    }

    public static String getTime(String text){
        String t = text.substring(text.indexOf("T")+1,text.length());
        String time = t.substring(0,5);

        return time;
    }

    public static String getCurrentTime() {
        Calendar c = GregorianCalendar.getInstance();
        String minute = String.valueOf(c.get(Calendar.MINUTE));
        String hour = String.valueOf(c.get(Calendar.HOUR));
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(c.get(Calendar.MONTH)+1);
        String year = String.valueOf(c.get(Calendar.YEAR));

        // SimpleDateFormat Class
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd");
        String newdate =  sdfDateTime.format(new Date(System.currentTimeMillis()));

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String newtime = sdfTime.format(new Date(System.currentTimeMillis()));

        String finalString = newdate + "T" + newtime;

        return finalString ;
    }

    public static String getYear(String time){
        String date = time.substring(0,time.lastIndexOf("T"));
        String year = date.substring(0,date.indexOf("-"));
        Log.w(TAG, "Year: " + year);
        return year;
    }

    public static String getMonth(String time){
        String date = time.substring(0,time.lastIndexOf("T"));
        String month = date.substring(date.indexOf("-")+1,date.lastIndexOf("-"));
        Log.w(TAG, "Month: " + month);
        return month;
    }

    public static String getDay(String time){
        String date = time.substring(0,time.lastIndexOf("T"));
        String day = date.substring(time.lastIndexOf("-")+1,date.length());
        Log.w(TAG, "Day: " + day);
        return day;
    }

    public static String getHour(String time){
        String t = time.substring(time.indexOf("T")+1,time.length());
        String hour = t.substring(0,2);
        Log.w(TAG, "Hour: " + hour);
        return hour;
    }

    public static String getMinute(String time){
        String t = time.substring(time.indexOf("T")+1,time.length());
        String minute = t.substring(3,5);
        Log.w(TAG, "Minute: " + minute);
        return minute;

    }

    public static Calendar stringToDate(String time){
        int year = Integer.parseInt(getYear(time));
        int month = Integer.parseInt(getMonth(time))-1;
        int day = Integer.parseInt(getDay(time));
        int hour = Integer.parseInt(getHour(time));
        int minute = Integer.parseInt(getMinute(time));
        int second = 0;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);

        return c;
    }

    public static String calendarToString(Calendar calendar){
        int year = calendar.get(Calendar.YEAR);
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        if (Integer.parseInt(month)<10){
            month = "0" + month;
        }
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (Integer.parseInt(day)<10){
            day = "0" + day;
        }
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        if (Integer.parseInt(hour)<10){
            hour = "0" + hour;
        }
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        if (Integer.parseInt(minute)<10){
            minute = "0" + minute;
        }

        return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00";
    }


}
