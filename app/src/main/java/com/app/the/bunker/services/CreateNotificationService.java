package com.app.the.bunker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.NotificationTable;
import com.app.the.bunker.models.GameModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateNotificationService extends IntentService {

    private static final String TAG = "CreateNotifyService";

    public static final String RUNNING_SERVICE = "createNotifyRunning";

    public static final String GAME_HEADER = "gameList";

    public CreateNotificationService(){
        super(CreateNotificationService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, true);
        editor.apply();
        Log.w(TAG, "Service running!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, false);
        editor.apply();
        Log.w(TAG, "Service destroyed!");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {

        List<GameModel> gameList = (List<GameModel>) intent.getSerializableExtra(GAME_HEADER);
        List<Integer> notifyList = new ArrayList<>();
        List<Integer> nIdList = new ArrayList<>();

        Cursor notifyCursor = null;
        try {
            notifyCursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, null, null, null);
            if (notifyCursor != null && notifyCursor.moveToFirst()){
                for (int i=0;i<notifyCursor.getCount();i++){
                    nIdList.add(notifyCursor.getInt(notifyCursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID)));
                    notifyList.add(notifyCursor.getInt(notifyCursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME)));
                    notifyCursor.moveToNext();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (notifyCursor != null) notifyCursor.close();
        }

        createNotifications(gameList, notifyList, nIdList);

    }

    private void createNotifications(List<GameModel> gameList, List<Integer> notifyList, List<Integer> nIdList) {
        boolean insert = false;
        if (notifyList.size()>0){
            Log.w(TAG, "notifyList size > 0 (" + notifyList.size() + ")");
            for (int i=0;i<gameList.size();i++){
                for (int x=0;x<notifyList.size();x++){
                    if (gameList.get(i).getGameId() == notifyList.get(x)){
                        insert = false;
                        break;
                    } else insert = true;
                }
                if (insert){
                    insertNotifications(gameList.get(i));
                }
            }
            boolean delete = false;
            if (gameList.size()>0){
                for (int i=0;i<notifyList.size();i++){
                    for (int x=0;x<gameList.size();x++){
                        if (notifyList.get(i) == gameList.get(x).getGameId()){
                            delete = false;
                            break;
                        } else delete = true;
                    }
                    if (delete){
                        int deletedId = getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_GAME + "=" + notifyList.get(i), null);
                        if (deletedId != 0) cancelAlarmTask(nIdList.get(i));
                    }
                }
            } else {
                Log.w(TAG, "Deleting all notifications...");
                for (int i=0;i<notifyList.size();i++){
                    int deletedId = getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_ID + "= (SELECT MAX(_id) FROM notification WHERE notification_game=" + notifyList.get(i) + ")", null);
                    if (deletedId != 0) cancelAlarmTask(nIdList.get(i));
                }
            }
        } else {
            Log.w(TAG, "notifyList size < 0");
            if (gameList.size()>0){
                for (int i=0;i<gameList.size();i++){
                    insertNotifications(gameList.get(i));
                }
            }
        }
    }

    private void insertNotifications(GameModel gameModel) {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int diff = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF,0);
        int times = 1;
        if (diff != 0){
            Calendar now = Calendar.getInstance();
            Calendar notifyTime = DateUtils.stringToDate(getNotificationTime(gameModel.getTime()));
            if (notifyTime.getTimeInMillis() < now.getTimeInMillis()){
                times = 1;
            } else times = 2;
        }
        for (int i=0;i<times;i++){
            ContentValues values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, gameModel.getGameId());
            values.put(NotificationTable.COLUMN_EVENT, gameModel.getEventName());
            values.put(NotificationTable.COLUMN_TYPE, gameModel.getTypeName());
            values.put(NotificationTable.COLUMN_ICON, getEventIconId(gameModel.getEventIcon()));
            if (i==0){
                values.put(NotificationTable.COLUMN_TIME, gameModel.getTime());
            } else values.put(NotificationTable.COLUMN_TIME, getNotificationTime(gameModel.getTime()));
            values.put(NotificationTable.COLUMN_GAME_TIME, gameModel.getTime());
            Uri success = getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
            values.clear();
            if (success != null){
                Log.w(TAG, "Inserted Notification ID: " + success.getLastPathSegment());
                if (i==0){
                    registerAlarmTask(DateUtils.stringToDate(gameModel.getTime()), Integer.parseInt(success.getLastPathSegment()));
                } else registerAlarmTask(DateUtils.stringToDate(getNotificationTime(gameModel.getTime())), Integer.parseInt(success.getLastPathSegment()));
            }
        }
    }

    private void registerAlarmTask(Calendar notifyTime, int requestId) {
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (requestId != 0){
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            sIntent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_SCHEDULED_NOTIFICATIONS);
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

    private String getNotificationTime(String time) {
        Calendar gameTime = DateUtils.stringToDate(time);
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int notify = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF,0);
        gameTime.add(Calendar.MINUTE,-notify);
        return DateUtils.calendarToString(gameTime);
    }

    private int getEventIconId(String eventIcon) {
        String icon = eventIcon.substring(eventIcon.lastIndexOf("/")+1,eventIcon.length());
        return getResources().getIdentifier(icon,"drawable",getPackageName());
    }
}
