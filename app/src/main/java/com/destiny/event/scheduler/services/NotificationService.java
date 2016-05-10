package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.provider.DataProvider;

public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";

    private int notificationId;

    private String gameId;

    public NotificationService() {
        super(NotificationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getNotificationInfo();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void getNotificationInfo() {
        Cursor cursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, null, null,NotificationTable.COLUMN_TIME + " ASC");
        if (cursor != null && cursor.moveToFirst()){
            Log.w(TAG, "Notificação encontrada, abrindo...");
            notificationId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_EVENT));
            int iconId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ICON));
            String typeName = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE));
            gameId = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME));
            makeNotification(title, iconId, typeName);
            cursor.close();
        } else Log.w(TAG, "Nenhuma Notificação foi encontrada.");

    }

    private void makeNotification(String title, int iconId, String typeName) {

        Intent nIntent = new Intent(getApplicationContext(), DrawerActivity.class);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, 0);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(iconId);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(getString(R.string.your_match_of) + typeName + getString(R.string.will_begin_soon));
        nBuilder.setTicker("Your match will begin...");
        nBuilder.setPriority(Notification.PRIORITY_HIGH);
        nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0, nBuilder.build());

        deleteShowedNotification();
        updateGameStatus();

        this.stopSelf();

    }

    private void updateGameStatus() {
        ContentValues values = new ContentValues();
        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_WAITING);
        Uri uri = Uri.parse(DataProvider.GAME_URI + "/" + gameId);
        getContentResolver().update(uri, values, GameTable.COLUMN_ID + "=" + gameId, null);
        Log.w(TAG, "Game status from ID: " + gameId + " updated to Waiting for Validation");
        values.clear();
    }

    private void deleteShowedNotification() {
        getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_ID + "=" + notificationId,null);
        Log.w(TAG, "Notification created, and then deleted!");
    }
}
