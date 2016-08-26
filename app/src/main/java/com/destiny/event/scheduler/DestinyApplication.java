package com.destiny.event.scheduler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.services.ServerService;

public class DestinyApplication extends Application {

    private static final String TAG = "DestinyApplication";

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler(){
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String exceptionClass = "";
            String errorMessage = "";
            SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            String membership = sharedPrefs.getString(DrawerActivity.MEMBER_PREF,"");
            int platform = sharedPrefs.getInt(DrawerActivity.PLATFORM_PREF, 0);
            String clanId = sharedPrefs.getString(DrawerActivity.CLAN_PREF, "");
            int lines = 4;
            if (ex.getStackTrace().length<=4){ lines = ex.getStackTrace().length; }
            for (int i=0;i<lines;i++){
                errorMessage = errorMessage + "\n at " + ex.getStackTrace()[i].toString();
                if (i==0){
                    String s = ex.getStackTrace()[i].toString();
                    exceptionClass = s.substring(s.indexOf("(")+1,s.lastIndexOf("."));
                }
            }
            callServerService(membership, platform, clanId, exceptionClass, errorMessage);
            //Log.e(TAG, "Membership: " + membership + " platform: " + platform + "\nClass: " + exceptionClass + "\nException: " + ex.toString() + errorMessage);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    private void callServerService(String membership, int platformId, String clanId, String exceptionClass, String errorMessage) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
        intent.putExtra(ServerService.REQUEST_TAG, ServerService.TYPE_EXCEPTION);
        intent.putExtra(ServerService.MEMBER_TAG, membership);
        intent.putExtra(ServerService.PLATFORM_TAG, platformId);
        intent.putExtra(ServerService.CLAN_TAG, clanId);
        intent.putExtra(ServerService.CLASS_TAG, exceptionClass);
        intent.putExtra(ServerService.EXCEPTION_TAG, errorMessage);
        startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}
