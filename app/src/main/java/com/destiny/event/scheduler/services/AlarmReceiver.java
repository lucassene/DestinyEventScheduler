package com.destiny.event.scheduler.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.destiny.event.scheduler.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);
        service.putExtra("title", intent.getStringExtra("title"));
        service.putExtra("icon", intent.getIntExtra("icon", R.drawable.ic_event_new));
        context.startService(service);
    }
}
