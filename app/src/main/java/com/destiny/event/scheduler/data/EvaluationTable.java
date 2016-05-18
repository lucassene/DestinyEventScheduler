package com.destiny.event.scheduler.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EvaluationTable {

    public static final String TABLE_NAME = "evaluation";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GAME = "eval_game_id";
    public static final String COLUMN_MEMBERSHIP_A = "eval_member_a";
    public static final String COLUMN_MEMBERSHIP_B = "eval_member_b";
    public static final String COLUMN_EVALUATION = "rate";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_GAME, COLUMN_MEMBERSHIP_A, COLUMN_MEMBERSHIP_B, COLUMN_EVALUATION};

    public static final int DISLIKE = -1;
    public static final int NEUTRAL = 0;
    public static final int LIKE = 1;

    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_GAME
            + " TEXT NOT NULL, "
            + COLUMN_MEMBERSHIP_A
            + " TEXT NOT NULL, "
            + COLUMN_MEMBERSHIP_B
            + " TEXT NOT NULL, "
            + COLUMN_EVALUATION
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
