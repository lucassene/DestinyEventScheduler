package com.app.the.bunker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "destinydb";
    private static final int DB_VERSION = 1;
    private static DBHelper mInstance = null;

    private  DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("Database", "DBHelper onCreate method called succesfully");
        EventTypeTable.onCreate(db);
        EventTable.onCreate(db);
        LoggedUserTable.onCreate(db);
        ClanTable.onCreate(db);
        MemberTable.onCreate(db);
        NotificationTable.onCreate(db);
        SavedImagesTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Database", "DBHelper is updating...");
        EventTypeTable.onUpdate(db, oldVersion, newVersion);
        EventTable.onUpdate(db, oldVersion, newVersion);
        LoggedUserTable.onUpdate(db, oldVersion, newVersion);
        ClanTable.onUpdate(db, oldVersion, newVersion);
        MemberTable.onUpdate(db, oldVersion, newVersion);
        NotificationTable.onUpdate(db, oldVersion, newVersion);
        SavedImagesTable.onUpdate(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
        Log.w("Database","Database opened succesfully");
    }

    @Override
    public synchronized void close() {
        super.close();
        Log.w("Database","Database closed succesfully");
    }

    public static DBHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new DBHelper(context.getApplicationContext());
        }
        return mInstance;
    }
}
