package com.destiny.event.scheduler.utils;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.CookieManager;

public final class CookiesUtils {

    private static final String TAG = "CookiesUtils";

    public static final String DESTINY_CSRF = "bungled";

    private CookiesUtils() {
        throw new UnsupportedOperationException("Don't call this constructor!");
    }

    public static String getCookies(String siteUrl){
        CookieManager cookieManager = CookieManager.getInstance();
        //Log.w("CookieUtils", "Cookie: " + cookieManager.getCookie(siteUrl));
        return cookieManager.getCookie(siteUrl);
    }

    public static void clearCookies(){
        Log.w(TAG, "Clearing cookies...");
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
        }
    }

    @Nullable
    public static String getCrossReferenceToken(String cookies){
        if (cookies==null){
            return null;
        }

        String[] rawCookies = cookies.split("[; ]+");

        if (rawCookies.length > 1){
            for (String rawCookie : rawCookies){
                int splitpos = rawCookie.indexOf('=');
                if (splitpos != -1){
                    String name = rawCookie.substring(0, splitpos);
                    String value = rawCookie.substring(splitpos + 1, rawCookie.length());

                    if (DESTINY_CSRF.equals(name)){
                        return value;
                    }
                }
            }
        }
        return null;
    }

}