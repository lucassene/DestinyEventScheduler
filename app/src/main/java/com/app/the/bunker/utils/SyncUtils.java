package com.app.the.bunker.utils;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;

public class SyncUtils {

    private static final String TAG = "SyncUtils";
    public static final long DEFAULT_INTERVAL = 3600;
    public static final int SECS_IN_ONE_DAY = 86400;

    public static void toogleSync(Context context, boolean b, long interval){
        Log.w(TAG, "Changing syncAutomatically to " + b + " in intervals of " + interval + " secs.");
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String userName = prefs.getString(Constants.USERNAME_PREF, "");
        Account acc = new Account(userName, Constants.ACC_TYPE);
        ContentResolver.setSyncAutomatically(acc, context.getString(R.string.AUTHORITY), b);
        if (b){
            ContentResolver.addPeriodicSync(acc, context.getString(R.string.AUTHORITY), Bundle.EMPTY, interval);
        }
    }

    public static boolean isSyncEnabled(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String userName = prefs.getString(Constants.USERNAME_PREF, "");
        Account acc = new Account(userName, Constants.ACC_TYPE);
        return ContentResolver.getSyncAutomatically(acc, context.getString(R.string.AUTHORITY));
    }

}
