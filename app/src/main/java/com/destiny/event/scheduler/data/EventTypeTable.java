package com.destiny.event.scheduler.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTypeTable {

    public static final String TABLE_NAME = "event_type";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "type_name";
    public static final String COLUMN_ICON = "type_icon";

    public static final String[] ALL_COLUMNS = {COLUMN_NAME, COLUMN_ICON, COLUMN_ID};
    public static final String[] VIEW_COLUMNS = {COLUMN_NAME, COLUMN_ICON};

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        Log.e("EventType Table", "EventType table created");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'court', 'ic_court');"); //1
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'crucible', 'ic_crucible');"); //2
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'patrol', 'ic_patrol');"); //3
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'prison', 'ic_prison');"); //4
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'raid', 'ic_raid');"); //5
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'story', 'ic_story');"); //6
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'strike', 'ic_strike');"); //7
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'strike_list', 'ic_strike');"); //8
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'srl', 'ic_srl');"); //9
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.e(EventTypeTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
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
