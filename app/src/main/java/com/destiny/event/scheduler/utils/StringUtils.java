package com.destiny.event.scheduler.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

public class StringUtils {

    public static boolean isEmpty(CharSequence s) {
        return TextUtils.isEmpty(s);
    }

    public static boolean isEmptyOrWhiteSpaces(CharSequence s) {
        return isEmpty(s) || s.toString().trim().isEmpty();
    }

    public static String parseString(int number){
        if (Math.round(number) >= 100) {
            return "99";
        } else if (Math.round(number) <= 0) {
            return "01";
        } else if (Math.round(number) < 10) {
            return "0" + String.valueOf(number);
        } else return String.valueOf(number);
    }

    public static String getLanguageString(Context context) {
        context.getResources();
        String lang = Resources.getSystem().getConfiguration().locale.getLanguage();
        switch (lang) {
            case "pt":
                return lang;
            case "es":
                return lang;
            default:
                return "en";
        }
    }


}
