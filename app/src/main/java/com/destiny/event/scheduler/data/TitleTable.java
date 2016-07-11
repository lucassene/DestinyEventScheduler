package com.destiny.event.scheduler.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TitleTable {

    private static final String TAG = "TitleTable";

    public static final String TABLE_NAME = "titles";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EVENT = "title_event";
    public static final String COLUMN_TITLE = "title_array_id";
    public static final String COLUMN_ORDER = "title_order";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_EVENT, COLUMN_TITLE, COLUMN_ORDER};

    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EVENT
            + " INTEGER NOT NULL, "
            + COLUMN_TITLE
            + " INTEGER NOT NULL, "
            + COLUMN_ORDER
            + " INTEGER NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

        //Court of Oryx
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 1, 0, 0);"); //tier 1
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 2, 0, 0);"); //tier 2
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 3, 0, 0);"); //tier 3

        //Crucible
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 4, 1, 1);"); //clash
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 5, 2, 1);"); //classic3x3
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 6, 2, 1);"); //classic6x6
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 7, 25, 0);"); //classic rumble
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 8, 3, 0);"); //control
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 9, 4, 1);"); //doubles
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 10, 5, 1);"); //elimination
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 11, 6, 1);"); //inferno3x3
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 12, 6, 1);"); //inferno6x6
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 13, 7, 0);"); //iron banner
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 14, 8, 1);"); //mayhem clash
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 15, 9, 0);"); //Rift
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 16, 10, 1);"); //salvage
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 17, 11, 0);"); //skirmish
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 18, 12, 1);"); //trials of osiris
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 19, 13, 0);"); //zone control

        //Patrol
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 20, 14, 1);"); //cosmodrome
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 21, 15, 0);"); //dreadnaught
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 22, 16, 1);"); //mars
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 23, 17, 0);"); //moon
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 24, 18, 1);"); //venus

        //Prison of Elders
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 25, 19, 1);"); //lvl 28
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 26, 19, 1);"); //lvl 32
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 27, 19, 1);"); //lvl 34
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 28, 19, 1);"); //lvl 35
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 29, 19, 1);"); //lvl 40
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 30, 19, 1);"); //lvl 42

        //Raid
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 31, 20, 0);"); //crotas normal
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 32, 20, 0);"); //crotas hard
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 33, 22, 0);"); //kings normal
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 34, 22, 0);"); //kings hard
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 35, 21, 1);"); //vault normal
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 36, 21, 1);"); //vault hard

        //Story
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 37, 26, 0);"); //story normal
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 38, 26, 0);"); //story heroic

        //Strike
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 39, 23, 0);"); //malok
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 40, 23, 0);"); //values
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 41, 23, 0);"); //dust
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 42, 23, 0);"); //echo
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 43, 23, 0);"); //saber
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 44, 23, 0);"); //shield
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 45, 23, 0);"); //devils
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 46, 23, 0);"); //nexus
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 47, 23, 0);"); //shadow
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 48, 23, 0);"); //pits
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 49, 23, 0);"); //sunless
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 50, 23, 0);"); //mind
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 51, 23, 0);"); //will
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 52, 23, 0);"); //winter

        //Strike List
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 53, 24, 1);"); //nightfall
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 54, 23, 0);"); //weekly
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 55, 23, 0);"); //heroic
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 56, 23, 0);"); //vanguard
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 57, 23, 0);"); //legacy

        //SRL
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EVENT + ", " + COLUMN_TITLE + ", " + COLUMN_ORDER + ")" + " VALUES " + "(null, 58, 27, 0);"); //swift
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

}
