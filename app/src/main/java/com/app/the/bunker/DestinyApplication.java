package com.app.the.bunker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.services.ServerService;

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
            try {
                callServerService(membership, platform, clanId, exceptionClass, errorMessage);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //Log.e(TAG, errorMessage);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    private String getErrorMessage(Throwable ex) {
        String error = ex.toString();
        String nextError;
        String log = Log.getStackTraceString(ex);
        boolean hasCaused = false;
        if (log.contains("Caused by")){
            int lenght = 990 - error.length();
            String msg = mySubstring(log,"Caused by",lenght);
            error = error + "\n" + msg;
            hasCaused = true;
        }

        if (!hasCaused){
            for (int i=0;i<ex.getStackTrace().length;i++){
                nextError = "\n at" + ex.getStackTrace()[i].toString();
                if (error.length() + nextError.length() >= 1000){
                    break;
                } else error = error + nextError;
            }
        }
        //Log.w(TAG, "error: " + error);
        Log.w(TAG, "errorMessage lenght: " + error.length());
        return error;
    }

    String mySubstring(String myString, String start, int length) {
        return myString.substring(myString.indexOf(start), Math.min(myString.indexOf(start) + length, myString.length()));
    }

    private void callServerService(String membership, int platformId, String clanId, String exceptionClass, String errorMessage) throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
        intent.putExtra(ServerService.REQUEST_TAG, ServerService.TYPE_EXCEPTION);
        intent.putExtra(ServerService.MEMBER_TAG, membership);
        intent.putExtra(ServerService.PLATFORM_TAG, platformId);
        intent.putExtra(ServerService.CLAN_TAG, clanId);
        intent.putExtra(ServerService.CLASS_TAG, exceptionClass);
        intent.putExtra(ServerService.DEVICE_TAG, getDeviceName());
        //Log.w(TAG, "Device name: " + getDeviceName());
        intent.putExtra(ServerService.ANDROID_TAG, Build.VERSION.SDK_INT);
        //Log.w(TAG, "API number: " + Build.VERSION.SDK_INT);
        intent.putExtra(ServerService.APP_TAG, getResources().getInteger(R.integer.versionCode));
        //Log.w(TAG, "App version: " + BuildConfig.VERSION_CODE);
        intent.putExtra(ServerService.EXCEPTION_TAG, errorMessage);
        startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
