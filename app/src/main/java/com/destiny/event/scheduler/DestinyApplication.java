package com.destiny.event.scheduler;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.destiny.event.scheduler.activities.DrawerActivity;

public class DestinyApplication extends Application {

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler(){
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String exceptionClass = "";
            String errorMessage = "";
            SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            String membership = sharedPrefs.getString(DrawerActivity.MEMBER_PREF,"");
            int platform = sharedPrefs.getInt(DrawerActivity.PLATFORM_PREF, 0);

            for (int i=0;i<ex.getStackTrace().length;i++){
                errorMessage = errorMessage + "\n at " + ex.getStackTrace()[i].toString();
                if (i==0){
                    String s = ex.getStackTrace()[i].toString();
                    exceptionClass = s.substring(s.indexOf("(")+1,s.lastIndexOf("."));
                }
            }
            Log.e("DestinyApplication", "Membership: " + membership + " platform: " + platform + "\nClass: " + exceptionClass + "\nException: " + ex.toString() + errorMessage);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}
