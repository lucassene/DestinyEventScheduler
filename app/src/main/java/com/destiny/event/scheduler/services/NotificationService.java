package com.destiny.event.scheduler.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.provider.DataProvider;

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

        getNotificationInfo(pIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void getNotificationInfo(PendingIntent pIntent) {
        Cursor cursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, NotificationTable.COLUMN_ID + "=1", null,null);
        if (cursor != null && cursor.moveToFirst()){
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE));
            int iconId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ICON));
            String typeName = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE));

            makeNotification(pIntent, title, iconId, typeName);
        } else Log.w(TAG, "Nenhuma Notificação foi encontrada.");

    }

    private void makeNotification(PendingIntent pIntent, String title, int iconId, String typeName) {

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(iconId);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(getString(R.string.your_match_of) + typeName + getString(R.string.will_begin_soon));
        nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0, nBuilder.build());

    }
}
