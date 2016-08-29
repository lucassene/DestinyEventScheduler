package com.destiny.event.scheduler.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int TYPE_SCHEDULED_NOTIFICATIONS = 1;
    public static final int TYPE_NEW_NOTIFICATIONS = 2;

    public static final String TYPE_HEADER = "type";
    public static final String NOTIFY_ID = "notifyId";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service;
        switch (intent.getIntExtra(TYPE_HEADER, 0)){
            case TYPE_SCHEDULED_NOTIFICATIONS:
                service = new Intent(context, NotificationService.class);
                service.putExtra(NOTIFY_ID, intent.getIntExtra(NOTIFY_ID, 0));
                context.startService(service);
                break;
            case TYPE_NEW_NOTIFICATIONS:
                service = new Intent(context, NewGameNotificationService.class);
                service.putExtra("memberId", intent.getStringExtra("memberId"));
                service.putExtra("platformId", intent.getIntExtra("platformId",0));
                context.startService(service);
                break;
        }
    }
}
