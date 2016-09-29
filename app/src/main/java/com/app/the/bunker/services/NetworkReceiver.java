package com.app.the.bunker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.utils.NetworkUtils;

import java.util.Set;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.w(TAG, "NetworkReceiver called!");

        if (NetworkUtils.checkConnection(context)) {

            SharedPreferences prefs = context.getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            Set<String> set = prefs.getStringSet(DrawerActivity.DOWNLOAD_PREF, null);
            if (set != null){
                Intent serviceIntent = new Intent(context, DownloadService.class);
                context.startService(serviceIntent);
            }

        }

    }

}
