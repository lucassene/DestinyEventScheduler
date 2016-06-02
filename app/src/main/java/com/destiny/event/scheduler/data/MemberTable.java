package com.destiny.event.scheduler.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MemberTable {

    public static final String TABLE_NAME = "members";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "member_name";
    public static final String COLUMN_MEMBERSHIP = "membership";
    public static final String COLUMN_CLAN = "clan";
    public static final String COLUMN_ICON = "member_icon";
    public static final String COLUMN_PLATFORM = "platform";
    public static final String COLUMN_LIKES = "likes";
    public static final String COLUMN_DISLIKES = "dislikes";
    public static final String COLUMN_CREATED = "games_created";
    public static final String COLUMN_PLAYED = "games_played";
    //public static final String COLUMN_SINCE = "member_since";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_MEMBERSHIP, COLUMN_CLAN, COLUMN_ICON, COLUMN_PLATFORM, COLUMN_LIKES, COLUMN_DISLIKES, COLUMN_CREATED, COLUMN_PLAYED};
    public static final String[] VIEW_COLUMNS = {COLUMN_NAME, COLUMN_ICON, COLUMN_LIKES, COLUMN_DISLIKES, COLUMN_CREATED, COLUMN_PLAYED};

    //public static final String POINTS_COLUMNS = "((" + COLUMN_LIKES + "*1.0)/(" + COLUMN_CREATED + "+" + COLUMN_PLAYED + "))*100+(" + COLUMN_CREATED + "*0.5)-" + COLUMN_DISLIKES;
    public static final String LIKE_MODIFIER = "16";
    public static final String CREATOR_MODIFIER = "64";
    public static final String PLAYED_MODIFIER = "48";
    public static final String DISLIKE_MODIFIER = "16";
    public static final int EXP_CONSTANT = 8;
    public static final int EXP_FACTOR = 2;
    public static final String COLUMN_EXP = "(" + COLUMN_LIKES + "*" + LIKE_MODIFIER + ") + (" + COLUMN_CREATED + "*" + CREATOR_MODIFIER + ") + (" + COLUMN_PLAYED + "*" + PLAYED_MODIFIER + ") - (" + COLUMN_DISLIKES + "*" + DISLIKE_MODIFIER + ")";

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
            + " INTEGER"
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

    public static String getMemberLevel(String exp){
        double xp = (double) Integer.parseInt(exp);
        double lvl = Math.sqrt(xp/ MemberTable.EXP_CONSTANT);
        int mLvl = (int) lvl;

        if (Math.round(mLvl) >= 100) {
            return "99";
        } else if (Math.round(mLvl) <= 0) {
            return "01";
        } else if (Math.round(mLvl) < 10) {
            return "0" + String.valueOf(mLvl);
        } else return String.valueOf(mLvl);
    }

    public static int getExpNeeded(int xp){
        int lvl = Integer.parseInt(getMemberLevel(String.valueOf(xp)));
        return MemberTable.EXP_CONSTANT * (lvl^MemberTable.EXP_FACTOR);
    }

}
