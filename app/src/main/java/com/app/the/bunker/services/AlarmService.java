package com.app.the.bunker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.LoggedUserTable;
import com.app.the.bunker.data.NotificationTable;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.DateUtils;

import java.util.Calendar;

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
        if (sharedPrefs.getBoolean(DrawerActivity.NEW_NOTIFY_PREF, false)) {
            int interval = sharedPrefs.getInt(DrawerActivity.NEW_NOTIFY_TIME_PREF, DrawerActivity.DEFAULT_INTERVAL);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_NEW_NOTIFICATIONS);
            intent.putExtra("memberId", memberId);
            intent.putExtra("platformId", platformId);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Intent nIntent = new Intent(this, DrawerActivity.class);
                nIntent.putExtra("isFromNews", true);
                PendingIntent npIntent = PendingIntent.getActivity(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo aC = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + interval, npIntent);
                alarm.setAlarmClock(aC, pIntent);
            } else alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pIntent);
            Log.w(TAG, "New game alarm registered in an interval of " + interval + " millis");
        }
    }

    private void registerAlarm(Integer notifyId, String notifyTime) {
        Calendar time = DateUtils.stringToDate(notifyTime);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_SCHEDULED_NOTIFICATIONS);
        intent.putExtra(AlarmReceiver.NOTIFY_ID, notifyId);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            alarm.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pIntent);
        } else alarm.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pIntent);
        Log.w(TAG, "Alarm Register after boot. Notification ID: " + notifyId);
    }
}
