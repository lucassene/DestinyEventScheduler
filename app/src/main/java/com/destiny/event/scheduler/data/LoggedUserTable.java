package com.destiny.event.scheduler.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LoggedUserTable {

    public static final String TABLE_NAME = "logged_user";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MEMBERSHIP = "membership";
    public static final String COLUMN_CLAN = "clan";
    public static final String COLUMN_PLATFORM = "platform";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_MEMBERSHIP, COLUMN_CLAN, COLUMN_PLATFORM};
    public static final String[] VIEW_COLUMNS = {COLUMN_NAME};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME
            + " TEXT NOT NULL, "
            + COLUMN_MEMBERSHIP
            + " TEXT NOT NULL, "
            + COLUMN_CLAN
            + " INTEGER NOT NULL, "
            + COLUMN_PLATFORM
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
