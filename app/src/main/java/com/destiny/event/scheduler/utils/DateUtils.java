package com.destiny.event.scheduler.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

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

}
