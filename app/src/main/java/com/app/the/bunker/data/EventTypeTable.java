package com.app.the.bunker.data;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTypeTable {

    public static final String TABLE_NAME = "event_type";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EN = "en";
    public static final String COLUMN_PT = "pt";
    public static final String COLUMN_ES = "es";
    public static final String COLUMN_ICON = "type_icon";

    public static final String[] ALL_COLUMNS = {COLUMN_EN, COLUMN_PT, COLUMN_ES, COLUMN_ICON, COLUMN_ID};

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EN
            + " TEXT NOT NULL, "
            + COLUMN_PT
            + " TEXT NOT NULL, "
            + COLUMN_ES
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        Log.e("EventType Table", "EventType table created");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Court of Oryx', 'Corte de Oryx', 'Corte de Oryx', 'ic_court');"); //1
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Crucible', 'Crisol', 'El Crisol', 'ic_crucible');"); //2
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Patrol', 'Patrulha', 'Patrulla', 'ic_patrol');"); //3
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Prison of Elders', 'Prisão dos Anciões', 'El Presidio de los Ancianos', 'ic_elders');"); //4
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Raid', 'Incursão', 'Incursión', 'ic_raid');"); //5
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Story', 'História', 'Historia', 'ic_story');"); //6
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Strike', 'Assalto', 'Asalto', 'ic_strike');"); //7
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Strike List', 'Lista de Assaltos', 'Lista de Asaltos', 'ic_strike_list');"); //8
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'SRL', 'SRL', 'SRL', 'ic_srl');"); //9
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ")" + " VALUES " + "(null, 'Archon´s Forge', 'Forja do Arconte', 'La Fragua del Arconte', 'ic_archons');"); //10
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

    public static String getName(Context context, Cursor cursor){
        context.getResources();
        switch (Resources.getSystem().getConfiguration().locale.getLanguage()){
            case "pt":
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_PT));
            case "es":
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ES));
            default:
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_EN));
        }
    }


}
