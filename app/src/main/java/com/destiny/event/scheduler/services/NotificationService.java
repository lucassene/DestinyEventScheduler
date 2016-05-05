package com.destiny.event.scheduler.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent nIntent = new Intent(getApplicationContext(), DrawerActivity.class);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, 0);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_event_new);
        nBuilder.setContentTitle("title");
        nBuilder.setContentText(getString(R.string.notification_match_begin));
        nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0, nBuilder.build());

        Log.w(TAG, "Notification Service started!");

        return super.onStartCommand(intent, flags, startId);
    }
}
