package com.destiny.event.scheduler.utils;

import android.text.TextUtils;

public class StringUtils {

    public static boolean isEmpty(CharSequence s) {
        return TextUtils.isEmpty(s);
    }

    public static boolean isEmptyOrWhiteSpaces(CharSequence s) {
        return isEmpty(s) || s.toString().trim().isEmpty();
    }
}
