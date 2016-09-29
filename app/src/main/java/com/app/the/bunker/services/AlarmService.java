package com.app.the.bunker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.LoggedUserTable;
import com.app.the.bunker.data.NotificationTable;
import com.app.the.bunker.provider.DataProvider;

public class AlarmService extends IntentService {

    private static final String TAG = "AlarmService";

    public AlarmService() {
        super(AlarmService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Cursor cursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, null, null, NotificationTable.COLUMN_TIME + " ASC");
        if (cursor != null && cursor.moveToFirst()){

            for (int i=0; i<cursor.getCount(); i++){
                Integer notifyId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
                String notifyTime = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME));

                registerAlarm(notifyId, notifyTime);

                cursor.moveToNext();
            }

            cursor.close();
        }

        Cursor newCursor = getContentResolver().query(DataProvider.LOGGED_USER_URI, LoggedUserTable.ALL_COLUMNS, null, null, null);
        if (newCursor != null && newCursor.moveToFirst()){
            String memberId = newCursor.getString(newCursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
            int platformId = newCursor.getInt(newCursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_PLATFORM));
            registerNewGamesAlarm(memberId, platformId);
            newCursor.close();
        }

    }

    private void registerNewGamesAlarm(String memberId, int platformId) {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int interval = sharedPrefs.getInt(DrawerActivity.NEW_NOTIFY_TIME_PREF, DrawerActivity.DEFAULT_INTERVAL);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_NEW_NOTIFICATIONS);
        intent.putExtra("memberId",memberId);
        intent.putExtra("platformId",platformId);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), interval, pIntent);
        Log.w(TAG, "New game alarm registered after boot.");

    }

    private void registerAlarm(Integer notifyId, String notifyTime) {
        long time = Long.parseLong(notifyTime);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_SCHEDULED_NOTIFICATIONS);
        intent.putExtra(AlarmReceiver.NOTIFY_ID, notifyId);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, time, pIntent);
        Log.w(TAG, "Alarm Register after boot. Notification ID: " + notifyId);
    }
}