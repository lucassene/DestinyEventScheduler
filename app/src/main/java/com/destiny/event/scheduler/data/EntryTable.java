package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EntryTable {

    public static final String TABLE_NAME = "entry";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEMBERSHIP = "entry_membership";
    public static final String COLUMN_GAME = "entry_game";
    public static final String COLUMN_TIME = "entry_time";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_MEMBERSHIP, COLUMN_GAME, COLUMN_TIME};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MEMBERSHIP
            + " TEXT NOT NULL, "
            + COLUMN_GAME
            + " INTEGER NOT NULL, "
            + COLUMN_TIME
            + " TEXT NOT NULL"
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
