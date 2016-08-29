package com.destiny.event.scheduler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.services.ServerService;

public class DestinyApplication extends Application {

    private static final String TAG = "DestinyApplication";

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler(){
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String exceptionClass = ex.getStackTrace()[0].toString().substring(ex.getStackTrace()[0].toString().indexOf("(")+1,ex.getStackTrace()[0].toString().lastIndexOf("."));
            SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            String membership = sharedPrefs.getString(DrawerActivity.MEMBER_PREF,"");
            int platform = sharedPrefs.getInt(DrawerActivity.PLATFORM_PREF, 0);
            String clanId = sharedPrefs.getString(DrawerActivity.CLAN_PREF, "");
            String errorMessage = getErrorMessage(ex);
            callServerService(membership, platform, clanId, exceptionClass, errorMessage);
            //Log.e(TAG, errorMessage);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    private String getErrorMessage(Throwable ex) {
        String error = ex.toString();
        String nextError;
        for (int i=0;i<ex.getStackTrace().length;i++){
            nextError = "\n at" + ex.getStackTrace()[i].toString();
            if (error.length() + nextError.length() >= 1000){
                break;
            } else error = error + nextError;
        }
        Log.w(TAG, "errorMessage lenght: " + error.length());
        return error;
    }

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
