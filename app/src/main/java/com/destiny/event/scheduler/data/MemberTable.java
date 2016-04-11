package com.destiny.event.scheduler.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MemberTable {

    public static final String TABLE_NAME = "members";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MEMBERSHIP = "membership";
    public static final String COLUMN_CLAN = "clan";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_PLATFORM = "platform";
    public static final String COLUMN_LIKES = "likes";
    public static final String COLUMN_DISLIKES = "dislikes";
    public static final String COLUMN_CREATED = "games_created";
    public static final String COLUMN_PLAYED = "games_played";
    public static final String COLUMN_SINCE = "member_since";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_MEMBERSHIP, COLUMN_CLAN, COLUMN_ICON, COLUMN_PLATFORM, COLUMN_LIKES, COLUMN_DISLIKES, COLUMN_CREATED, COLUMN_PLAYED, COLUMN_SINCE};
    public static final String[] VIEW_COLUMNS = {COLUMN_NAME, COLUMN_ICON, COLUMN_LIKES, COLUMN_DISLIKES, COLUMN_CREATED, COLUMN_PLAYED, COLUMN_SINCE};

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME
            + " TEXT NOT NULL, "
            + COLUMN_MEMBERSHIP
            + " INTEGER NOT NULL, "
            + COLUMN_CLAN
            + " INTEGER NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL, "
            + COLUMN_PLATFORM
            + " INTEGER NOT NULL, "
            + COLUMN_LIKES
            + " INTEGER NOT NULL, "
            + COLUMN_DISLIKES
            + " INTEGER NOT NULL, "
            + COLUMN_CREATED
            + " INTEGER, "
            + COLUMN_PLAYED
            + " INTEGER, "
            + COLUMN_SINCE
            + " TEXT"
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
