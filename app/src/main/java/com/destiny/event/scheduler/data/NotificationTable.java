package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NotificationTable {

    public static final String TABLE_NAME = "notification";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GAME = "notification_game";
    public static final String COLUMN_EVENT = "notification_event";
    public static final String COLUMN_TYPE = "notification_type";
    public static final String COLUMN_ICON = "notification_icon";
    public static final String COLUMN_TIME = "notification_time";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_GAME, COLUMN_EVENT, COLUMN_TYPE, COLUMN_ICON, COLUMN_TIME};

    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_GAME
            + " INTEGER NOT NULL, "
            + COLUMN_EVENT
            + " TEXT NOT NULL, "
            + COLUMN_TYPE
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " INTEGER NOT NULL, "
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

    public static String getQualifiedColumn(String column){
        return TABLE_NAME + "." + column;
    }

    public static String getAliasColumn(String column){
        return TABLE_NAME + "_" + column;
    }

}
