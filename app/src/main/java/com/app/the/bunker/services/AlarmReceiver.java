package com.app.the.bunker.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    public static final int TYPE_SCHEDULED_NOTIFICATIONS = 1;

    public static final String TYPE_HEADER = "type";
    public static final String NOTIFY_ID = "notifyId";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service;
        Log.w(TAG, "AlarmReceiver called!");
        switch (intent.getIntExtra(TYPE_HEADER, 0)){
            case TYPE_SCHEDULED_NOTIFICATIONS:
                service = new Intent(context, NotificationService.class);
                service.putExtra(NOTIFY_ID, intent.getIntExtra(NOTIFY_ID, 0));
                context.startService(service);
                break;
        }
    }
}
