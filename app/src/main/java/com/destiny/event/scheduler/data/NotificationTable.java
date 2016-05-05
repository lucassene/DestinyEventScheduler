package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NotificationTable {

    public static final String TABLE_NAME = "notification";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EVENT = "notification_event";
    public static final String COLUMN_ICON = "notification_icon";
    public static final String COLUMN_TIME = "notification_time";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_EVENT, COLUMN_TIME, COLUMN_ICON};

    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EVENT
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL, "
            + COLUMN_TIME
            + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
        Log.w("NotificationTable","Notification table created sucessfully");
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(LoggedUserTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
