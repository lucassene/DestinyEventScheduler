package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTable {

    public static final String TABLE_NAME = "event";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_LIGHT = "min_light";
    public static final String COLUMN_GUARDIANS = "max_guardians";

    public static final String[] ALL_COLUMNS = {EventTable.COLUMN_ID, EventTable.COLUMN_NAME, EventTable.COLUMN_ICON, EventTable.COLUMN_TYPE, EventTable.COLUMN_LIGHT, EventTable.COLUMN_GUARDIANS};
    public static final String[] VIEW_COLUMNS = {EventTable.COLUMN_NAME, EventTable.COLUMN_ICON, EventTable.COLUMN_LIGHT, EventTable.COLUMN_GUARDIANS};

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL, "
            + COLUMN_TYPE
            + " INTEGER NOT NULL, "
            + COLUMN_LIGHT
            + " INTEGER NOT NULL, "
            + COLUMN_GUARDIANS
            + " INTEGER NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        Log.e("EventTable", "Event table created");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'tier1', 'ic_patrol', '1', 190, 3);");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'tier2', 'ic_patrol', '1', 240, 3);");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'tier3', 'ic_patrol', '1', 300, 3);");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'clash', 'ic_clash', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'classic3', 'ic_classic', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'classic6', 'ic_classic', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'classic1', 'ic_rumble', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'control', 'ic_control', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'doubles', 'ic_doubles', '2', '5', '2');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'elimination', 'ic_elimination', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'inferno3', 'ic_classic', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'inferno6', 'ic_classic', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'iron_banner', 'ic_iron_banner', '2', '230', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'mayhem_clash', 'ic_clash', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'rift', 'ic_rift', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'salvage', 'ic_salvage', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'skirmish', 'ic_skirmish', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'trials', 'ic_trials', '2', '251', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'zone_control', 'ic_zone_control', '2', '5', '6');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'cosmodrome', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'dreadnaught', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'mars', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'moon', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'venus', 'ic_patrol', '3', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'prison28', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'prison32', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'prison34', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'prison35', 'ic_prison', '4', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'crota_normal', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'crota_hard', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'king_normal', 'ic_raid', '5', '290', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'king_hard', 'ic_raid', '5', '310', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'vault_normal', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'vault_hard', 'ic_raid', '5', '5', '6');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'story_normal', 'ic_story', '6', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'story_heroic', 'ic_story', '6', '240', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'valus', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'dust', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'echo', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'saber', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'shield', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'devils', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'nexus', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'shadow', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'pits', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'sunless', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'mind', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'will', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'winter', 'ic_strike', '7', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'nightfall', 'ic_nightfall', '8', '280', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'weekly', 'ic_weekly_strike', '8', '260', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'heroic', 'ic_strike_heroic', '8', '260', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'vanguard', 'ic_strike', '8', '200', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'legacy', 'ic_strike', '8', '5', '3');");








    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.e(EventTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
