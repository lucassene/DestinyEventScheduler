package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;

public class SavedImagesTable {

    public static final String TABLE_NAME = "savedimages";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PATH = "path";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_PATH};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PATH
            + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
