package com.app.the.bunker.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.NotificationTable;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.DateUtils;

import java.util.Calendar;

public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";

    public NotificationService() {
        super(NotificationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notificationId = intent.getIntExtra(AlarmReceiver.NOTIFY_ID, 0);
        Log.w(TAG, "notificationId: " + notificationId);
        getNotificationInfo(notificationId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "NotificationService running...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "NotificationService destroyed");
    }

    private void getNotificationInfo(int notificationId) {
        if (notificationId != 0){
            Cursor cursor = null;
            try{
                cursor = getContentResolver().query(DataProvider.NOTIFICATION_URI, NotificationTable.ALL_COLUMNS, NotificationTable.COLUMN_ID + "=" + notificationId, null,NotificationTable.COLUMN_TIME + " ASC");
                if (cursor != null && cursor.moveToFirst()){
                    Log.w(TAG, "Notificação encontrada, abrindo...");
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_EVENT));
                    String iconId = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ICON));
                    String typeName = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE));
                    Calendar gameTime = DateUtils.stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME_TIME)));
                    String notifyTime = cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME));
                    cursor.close();
                    makeNotification(title, iconId, typeName, gameTime, notifyTime, notificationId);

                } else Log.w(TAG, "Nenhuma Notificação foi encontrada.");
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    private void makeNotification(String title, String iconId, String typeName, Calendar gameTime, String notifyTime, int notificationId) {

        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);

        if (sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, false)){

            Intent nIntent = new Intent(getApplicationContext(), DrawerActivity.class);
            nIntent.putExtra("notification",notificationId);
            nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            nIntent.setAction(String.valueOf(notificationId));
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nIntent.setData(Uri.parse(String.valueOf(notificationId)));

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
            nBuilder.setSmallIcon(R.drawable.ic_event_validate);
            nBuilder.setLargeIcon(getLargeIcon(iconId));
            nBuilder.setContentTitle(title);
            if (notifyTime.equals(DateUtils.calendarToString(gameTime))){
                nBuilder.setContentText(getString(R.string.your_match_of) + typeName + getString(R.string.will_begin_soon));
            } else {
                Calendar now = Calendar.getInstance();
                //Log.w(TAG, "now: " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND));
                long diffMs = gameTime.getTimeInMillis() - now.getTimeInMillis();
                long diffSec = diffMs / 1000;
                long timeDiff = (diffSec / 60) + 1;
                //Log.w(TAG, "timeDiff: " + timeDiff);
                String min;
                if (timeDiff > 1){
                    min = getString(R.string.minutes);
                } else min = getString(R.string.minute);
                String nMsg = getString(R.string.event_begin_in) + String.valueOf(timeDiff) + min;
                nBuilder.setContentText(nMsg);
            }
            nBuilder.setTicker(getString(R.string.match_begin));
            setPriority(nBuilder);
            nBuilder.setContentIntent(pIntent);

            boolean sound = sharedPrefs.getBoolean(Constants.SOUND_PREF,false);

            if (sound){
                nBuilder.setDefaults(Notification.DEFAULT_ALL);
            } else nBuilder.setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

            setVisibility(nBuilder);
            nBuilder.setAutoCancel(true);

            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.notify(0, nBuilder.build());

            deleteShowedNotification(notificationId);
            this.stopSelf();

        } else {
            this.stopSelf();
        }

    }

    @TargetApi(21)
    private void setVisibility(NotificationCompat.Builder nBuilder) {
        nBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
    }

    @TargetApi(16)
    private void setPriority(NotificationCompat.Builder nBuilder) {
        nBuilder.setPriority(Notification.PRIORITY_DEFAULT);
    }

    private void deleteShowedNotification(int notificationId) {
        getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_ID + "=" + notificationId,null);
        Log.w(TAG, "Notification created, and then deleted!");
    }

    private Bitmap getLargeIcon(String iconId){

        if (iconId.equals("ic_trials")){
            BitmapDrawable bD = (BitmapDrawable) ContextCompat.getDrawable(this, getResources().getIdentifier(iconId,"drawable",getPackageName()));
            return bD.getBitmap();
        } else {
            Drawable smallIcon = ContextCompat.getDrawable(this, getResources().getIdentifier(iconId,"drawable",getPackageName()));
            BitmapDrawable bD = (BitmapDrawable) smallIcon;
            Bitmap bigIcon = bD.getBitmap();
            Bitmap finalIcon = Bitmap.createBitmap(bigIcon.getWidth(), bigIcon.getHeight(), bigIcon.getConfig());
            Canvas canvas = new Canvas(finalIcon);
            Paint paint = new Paint();
            paint.setColorFilter(new PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(bigIcon,0,0,paint);
            return finalIcon;
        }
    }

}
