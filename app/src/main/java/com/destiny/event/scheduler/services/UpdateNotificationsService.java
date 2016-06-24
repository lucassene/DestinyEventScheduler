package com.destiny.event.scheduler.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.models.NotificationModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class UpdateNotificationsService extends IntentService {

    private static final String TAG = "UpdateNotifyService";

    public static final String NOTIFY_RUNNING = "updateNotify";

    private ArrayList<NotificationModel> notificationList;

    private int previousTime;

    public UpdateNotificationsService(){
        super(UpdateNotificationsService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Cursor cursor = null;
        notificationList = new ArrayList<>();

        previousTime = intent.getIntExtra("previous",15);

        String[] projection = {NotificationTable.getQualifiedColumn(NotificationTable.COLUMN_ID), NotificationTable.COLUMN_GAME, NotificationTable.COLUMN_TIME, NotificationTable.COLUMN_TYPE, NotificationTable.COLUMN_EVENT, NotificationTable.COLUMN_ICON, GameTable.COLUMN_TIME};

        try {
            cursor = getContentResolver().query(DataProvider.NOTIFICATION_GAMES_URI, projection, null, null, NotificationTable.COLUMN_TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()){
                //Log.w(TAG, DatabaseUtils.dumpCursorToString(cursor));
                for (int i=0;i<cursor.getCount();i++){
                    int nId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
                    int gId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME));
                    String nDate = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME));
                    String gDate = cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_TIME));
                    Log.w(TAG, nId + " | " + gId + " | " + nDate + " | " + gDate );
                    NotificationModel notification = new NotificationModel();
                    notification.setNotificationId(nId);
                    notification.setGameId(gId);
                    notification.setGameTime(gDate);
                    notification.setNotificationTime(nDate);
                    notification.setNeedUpdate(true);
                    notification.setEvent(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_EVENT)));
                    notification.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ICON)));
                    notification.setType(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE)));
                    notificationList.add(notification);
                    cursor.moveToNext();
                }
                updateNotifications();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

    }

    private void updateNotifications() {

        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int sharedTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF,15);
        Log.w(TAG, "actualSharedTime: " + sharedTime + " previousSharedTime: " + previousTime);
        boolean isAllowed = sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false);

        if (!isAllowed){
            getContentResolver().delete(DataProvider.NOTIFICATION_URI, null, null);
            for (int i=0;i<notificationList.size();i++){
                cancelAlarmTask(notificationList.get(i).getNotificationId());
            }
            Log.w(TAG, "All notifications deleted");
        } else {
            Calendar now = Calendar.getInstance();
            for (int i=0;i<notificationList.size();i++){
                if (!notificationList.get(i).getGameTime().equals(notificationList.get(i).getNotificationTime())){
                    if (sharedTime==0){
                        int deleted = getContentResolver().delete(DataProvider.NOTIFICATION_URI, NotificationTable.COLUMN_ID + "=" + notificationList.get(i).getNotificationId(),null);
                        if (deleted != 0){
                            Log.w(TAG, "Notification deleted with success");
                            cancelAlarmTask(notificationList.get(i).getNotificationId());
                        } else Log.w(TAG, "No notification deleted");
                    } else {
                        Calendar c = DateUtils.stringToDate(notificationList.get(i).getGameTime());
                        c.add(Calendar.MINUTE,-sharedTime);
                        if (c.getTimeInMillis() > now.getTimeInMillis()){
                            String notifyDate = DateUtils.calendarToString(c);
                            ContentValues values = new ContentValues();
                            values.put(NotificationTable.COLUMN_TIME,notifyDate);
                            int updated = getContentResolver().update(DataProvider.NOTIFICATION_URI,values,NotificationTable.COLUMN_ID + "=" + notificationList.get(i).getNotificationId(),null);
                            if (updated != 0){
                                cancelAlarmTask(notificationList.get(i).getNotificationId());
                                registerAlarmTask(c, notificationList.get(i).getNotificationId());
                                Log.w(TAG, "Notification updated with success");
                            } else Log.w(TAG, "No notification updated");
                            values.clear();
                        } else {
                            int deleted = getContentResolver().delete(DataProvider.NOTIFICATION_URI, NotificationTable.COLUMN_ID + "=" + notificationList.get(i).getNotificationId(),null);
                            if (deleted != 0){
                                Log.w(TAG, "Notification deleted with success");
                                cancelAlarmTask(notificationList.get(i).getNotificationId());
                            } else Log.w(TAG, "No notification deleted");
                        }
                    }
                } else if (previousTime == 0){
                    Calendar c = DateUtils.stringToDate(notificationList.get(i).getGameTime());
                    c.add(Calendar.MINUTE,-sharedTime);
                    ContentValues values = new ContentValues();
                    values.put(NotificationTable.COLUMN_GAME, notificationList.get(i).getGameId());
                    values.put(NotificationTable.COLUMN_EVENT, notificationList.get(i).getEvent());
                    values.put(NotificationTable.COLUMN_TYPE, notificationList.get(i).getType());
                    values.put(NotificationTable.COLUMN_ICON, notificationList.get(i).getIcon());
                    String notifyDate = DateUtils.calendarToString(c);
                    values.put(NotificationTable.COLUMN_TIME, notifyDate);
                    Uri inserted = getContentResolver().insert(DataProvider.NOTIFICATION_URI,values);
                    if (inserted != null){
                        Log.w(TAG, "Notification inserted with success");
                    } else Log.w(TAG, "No notification inserted");
                    values.clear();
                }
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "Service running");
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(NOTIFY_RUNNING, true);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "Service destroyed");
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(NOTIFY_RUNNING, false);
        editor.apply();
    }

    public void registerAlarmTask(Calendar notifyTime, int requestId) {
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (requestId != 0){
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent psIntent = PendingIntent.getBroadcast(this, requestId, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), psIntent);
        }
        Log.w(TAG, "Alarm for request Id " + requestId + " created");
    }

    public void cancelAlarmTask(int requestId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.w(TAG, "requestId canceled: " + requestId);
    }


}
