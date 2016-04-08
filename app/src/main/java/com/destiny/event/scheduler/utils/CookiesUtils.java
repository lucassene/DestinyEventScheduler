package com.destiny.event.scheduler.utils;

import android.support.annotation.Nullable;
import android.webkit.CookieManager;

/**
 * Created by Escritorio on 18/01/2016.
 */
public final class CookiesUtils {

    public static final String DESTINY_CSRF = "bungled";

    private CookiesUtils() {
        throw new UnsupportedOperationException("Don't call this constructor!");
    }

    public static String getCookies(String siteUrl){
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(siteUrl);
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