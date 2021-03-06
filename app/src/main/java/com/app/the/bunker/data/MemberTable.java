package com.app.the.bunker.data;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MemberTable {

    private static final String TAG = "MemberTable";

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
    public static final String COLUMN_TITLE = "member_title";

    public static final String LIKE_MODIFIER = "16";
    public static final String CREATOR_MODIFIER = "64";
    public static final String PLAYED_MODIFIER = "48";
    public static final String DISLIKE_MODIFIER = "16";
    public static final int EXP_CONSTANT = 8;
    public static final int EXP_FACTOR = 2;
    public static final String COLUMN_EXP = "(" + COLUMN_LIKES + "*" + LIKE_MODIFIER + ") + (" + COLUMN_CREATED + "*" + CREATOR_MODIFIER + ") + (" + COLUMN_PLAYED + "*" + PLAYED_MODIFIER + ") - (" + COLUMN_DISLIKES + "*" + DISLIKE_MODIFIER + ")";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_MEMBERSHIP, COLUMN_CLAN, COLUMN_ICON, COLUMN_PLATFORM, COLUMN_LIKES, COLUMN_DISLIKES, COLUMN_CREATED, COLUMN_PLAYED, COLUMN_EXP, COLUMN_TITLE};
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
            + COLUMN_TITLE
            + " TEXT NOT NULL"
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

    public static int getMemberLevel(int exp){
        double lvl = Math.sqrt(exp/MemberTable.EXP_CONSTANT);
        int inteiro = (int) lvl;
        double resto = lvl - inteiro;
        if (resto>0) inteiro++;
        return inteiro;
    }

    public static int getExpNeeded(int xp){
        int lvl = getMemberLevel(xp);
        double result = MemberTable.EXP_CONSTANT * Math.pow((double)lvl,(double)MemberTable.EXP_FACTOR);
        if (result <= 0){
            return 8;
        } else return (int) Math.round(result);
    }

    public static int getMemberXP(int likes, int dislikes, int gamesPlayed, int gamesCreated){
        int likesFator = ( likes * Integer.parseInt(LIKE_MODIFIER));
        int createdFator = ( gamesCreated * Integer.parseInt(CREATOR_MODIFIER));
        int playedFator = ( gamesPlayed * Integer.parseInt(PLAYED_MODIFIER));
        int dislikeFator = ( dislikes *  Integer.parseInt(DISLIKE_MODIFIER));
        return (likesFator + createdFator + playedFator) - dislikeFator;
    }

}
