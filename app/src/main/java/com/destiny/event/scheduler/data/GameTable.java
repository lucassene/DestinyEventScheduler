package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GameTable {

    public static final String TABLE_NAME = "game_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATOR = "creator";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LIGHT = "light";
    public static final String COLUMN_GUARDIANS = "guardians";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_CREATOR, COLUMN_EVENT_ID, COLUMN_TIME, COLUMN_LIGHT, COLUMN_GUARDIANS};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CREATOR
            + " TEXT NOT NULL, "
            + COLUMN_EVENT_ID
            + " TEXT NOT NULL, "
            + COLUMN_TIME
            + " TEXT NOT NULL, "
            + COLUMN_LIGHT
            + " INTEGER NOT NULL, "
            + COLUMN_GUARDIANS
            + " INTEGER NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.e(LoggedUserTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
