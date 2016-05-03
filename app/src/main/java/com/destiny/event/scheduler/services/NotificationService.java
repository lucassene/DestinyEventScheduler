package com.destiny.event.scheduler.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.destiny.event.scheduler.R;

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

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
            Log.w(TAG, "Icon ID: " + intent.getIntExtra("icon", 404) + " / Title: " + intent.getStringExtra("title"));
            nBuilder.setSmallIcon(intent.getIntExtra("icon", R.drawable.ic_event_new));
            nBuilder.setContentTitle(intent.getStringExtra("title"));
            nBuilder.setContentText("A partida irá começar em instantes...");
            nBuilder.setTicker("Sua partida no Destiny está prestes a começar!");
            nBuilder.setAutoCancel(true);

            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.notify(0, nBuilder.build());

        Log.w(TAG, "Notification Service started!");
        Toast.makeText(this, "Notification Service started!", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }
}
