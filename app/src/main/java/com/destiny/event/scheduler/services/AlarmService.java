package com.destiny.event.scheduler.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.provider.DataProvider;

public class AlarmService extends IntentService {

    private static final String TAG = "AlarmService";

    public AlarmService(String name) {
        super(name);
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

    }

    private void registerAlarm(Integer notifyId, String notifyTime) {

        long time = Long.parseLong(notifyTime);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, time, pIntent);
        Log.w(TAG, "Alarm Register after boot. Notification ID: " + notifyId);
    }
}
