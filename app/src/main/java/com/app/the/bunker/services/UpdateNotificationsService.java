package com.app.the.bunker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.app.the.bunker.Constants;
import com.app.the.bunker.data.NotificationTable;
import com.app.the.bunker.models.NotificationModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class UpdateNotificationsService extends IntentService {

    private static final String TAG = "UpdateNotifyService";

    public static final String NOTIFY_RUNNING = "updateNotify";

    private ArrayList<NotificationModel> notificationList;

    private int previousTime;
    private boolean previousCheck;

    public UpdateNotificationsService(){
        super(UpdateNotificationsService.class.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {

        Cursor cursor = null;
        notificationList = new ArrayList<>();

        previousTime = intent.getIntExtra("previousTime",15);
        previousCheck = intent.getBooleanExtra("previousCheck",true);
        Log.w(TAG, "previousCheck: " + previousCheck);

        try {
            cursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, null, null, NotificationTable.COLUMN_TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()){
                Log.w(TAG, DatabaseUtils.dumpCursorToString(cursor));
                for (int i=0;i<cursor.getCount();i++){
                    int nId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
                    int gId = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME));
                    String nDate = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME));
                    String gDate = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME_TIME));
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

        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        int sharedTime = sharedPrefs.getInt(Constants.SCHEDULED_TIME_PREF,0);
        Log.w(TAG, "actualSharedTime: " + sharedTime + " previousSharedTime: " + previousTime);
        boolean isAllowed = sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, false);

        if (!isAllowed){
            for (int i=0;i<notificationList.size();i++){
                if (!notificationList.get(i).getGameTime().equals(notificationList.get(i).getNotificationTime())){
                    int deleted = getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_ID + "=" + notificationList.get(i).getNotificationId(),null);
                    cancelAlarmTask(notificationList.get(i).getNotificationId());
                    if (deleted != 0){
                        Log.w(TAG, "Notification deleted with success");
                    } else Log.w(TAG, "No notification deleted");
                }
            }
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
                } else if (previousTime == 0 || !previousCheck){
                    Calendar c = DateUtils.stringToDate(notificationList.get(i).getGameTime());
                    c.add(Calendar.MINUTE,-sharedTime);
                    ContentValues values = new ContentValues();
                    values.put(NotificationTable.COLUMN_GAME, notificationList.get(i).getGameId());
                    values.put(NotificationTable.COLUMN_EVENT, notificationList.get(i).getEvent());
                    values.put(NotificationTable.COLUMN_TYPE, notificationList.get(i).getType());
                    values.put(NotificationTable.COLUMN_ICON, notificationList.get(i).getIcon());
                    String notifyDate = DateUtils.calendarToString(c);
                    values.put(NotificationTable.COLUMN_TIME, notifyDate);
                    values.put(NotificationTable.COLUMN_GAME_TIME, notificationList.get(i).getGameTime());
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
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(NOTIFY_RUNNING, true);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "Service destroyed");
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(NOTIFY_RUNNING, false);
        editor.apply();
    }

    public void registerAlarmTask(Calendar notifyTime, int requestId) {
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (requestId != 0){
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            sIntent.putExtra(AlarmReceiver.NOTIFY_ID, requestId);
            PendingIntent psIntent = PendingIntent.getBroadcast(this, requestId, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                alarm.setExact(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), psIntent);
            } else alarm.set(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), psIntent);
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
