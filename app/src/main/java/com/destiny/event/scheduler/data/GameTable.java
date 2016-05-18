package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GameTable {

    public static final String TABLE_NAME = "game";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATOR = "game_creator";
    public static final String COLUMN_CREATOR_NAME = "game_name";
    public static final String COLUMN_EVENT_ID = "game_event_id";
    public static final String COLUMN_TIME = "game_time";
    public static final String COLUMN_LIGHT = "game_light";
    public static final String COLUMN_INSCRIPTIONS = "game_inscriptions";
    public static final String COLUMN_STATUS = "game_status";

    public static final String STATUS_NEW = "0";
    public static final String STATUS_WAITING = "1";
    public static final String STATUS_VALIDATED = "2";
    public static final String STATUS_EVALUATED = "3";
    public static final String STATUS_SCHEDULED = "9";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_CREATOR, COLUMN_CREATOR_NAME, COLUMN_EVENT_ID, COLUMN_TIME, COLUMN_LIGHT, COLUMN_INSCRIPTIONS, COLUMN_STATUS};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CREATOR
            + " TEXT NOT NULL, "
            + COLUMN_CREATOR_NAME
            + " TEXT NOT NULL, "
            + COLUMN_EVENT_ID
            + " TEXT NOT NULL, "
            + COLUMN_TIME
            + " TEXT NOT NULL, "
            + COLUMN_LIGHT
            + " INTEGER NOT NULL, "
            + COLUMN_INSCRIPTIONS
            + " INTEGER NOT NULL, "
            + COLUMN_STATUS
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

    public static String getQualifiedColumn(String column){
        return TABLE_NAME + "." + column;
    }

    public static String getAliasColumn(String column){
        return TABLE_NAME + "_" + column;
    }

    public static String getAliasExpression(String column){
        return getQualifiedColumn(column) + " AS " + getAliasColumn(column);
    }


}
