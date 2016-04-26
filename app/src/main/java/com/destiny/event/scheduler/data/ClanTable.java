package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ClanTable {

    public static final String TABLE_NAME = "clan";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BUNGIE_ID = "bungie";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_BACKGROUND = "background";
    public static final String COLUMN_DESC = "desc";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_BUNGIE_ID, COLUMN_NAME, COLUMN_ICON, COLUMN_BACKGROUND, COLUMN_DESC};
    public static final String[] VIEW_COLUMNS = {COLUMN_NAME, COLUMN_ICON, COLUMN_BACKGROUND, COLUMN_DESC};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BUNGIE_ID
            + " INTEGER NOT NULL, "
            + COLUMN_NAME
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL, "
            + COLUMN_BACKGROUND
            + " TEXT NOT NULL, "
            + COLUMN_DESC
            + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
        Log.w("Clan Table","Clan table created sucessfully");
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(LoggedUserTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
