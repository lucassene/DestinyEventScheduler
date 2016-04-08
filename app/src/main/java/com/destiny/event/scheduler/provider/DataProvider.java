package com.destiny.event.scheduler.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.LoggedUserTable;

public class DataProvider extends ContentProvider {

    private DBHelper database;

    private static final int EVENT_TYPE = 10;
    private static final int EVENT_TYPE_ID = 11;
    private static final int EVENT = 20;
    private static final int EVENT_ID = 21;
    private static final int LOGGED_USER = 30;
    private static final int LOGGED_USER_ID = 31;

    private static final String AUTHORITY = "com.destiny.event.scheduler.provider";

    private static final String EVENT_TYPE_PATH = "eventtype";
    public static final Uri EVENT_TYPE_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_TYPE_PATH);
    private static final String EVENT_PATH = "event";
    public static final Uri EVENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_PATH);
    private static final String LOGGED_USER_PATH = "loggeduser";
    public static final Uri LOGGED_USER_URI = Uri.parse("content://" + AUTHORITY + "/" + LOGGED_USER_PATH);

    public static final String EVENT_TYPE_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/alleventtype";
    public static final String EVENT_TYPE_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleeventtype";
    public static final String EVENT_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allevent";
    public static final String EVENT_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleevent";
    public static final String LOGGED_USER_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allloggeduser";
    public static final String LOGGED_USER_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleloggeduser";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, EVENT_TYPE_PATH, EVENT_TYPE);
        uriMatcher.addURI(AUTHORITY, EVENT_TYPE_PATH + "/#", EVENT_TYPE_ID);
        uriMatcher.addURI(AUTHORITY, EVENT_PATH, EVENT);
        uriMatcher.addURI(AUTHORITY, EVENT_PATH + "/#", EVENT_ID);
        uriMatcher.addURI(AUTHORITY, LOGGED_USER_PATH, LOGGED_USER);
        uriMatcher.addURI(AUTHORITY, LOGGED_USER_PATH + "/#", LOGGED_USER_ID);
    }

    @Override
    public boolean onCreate() {
        Log.w("Database","Content Provider onCreated called succesfully.");
        database = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //checkColumns(uri, projection);

        int uriType = uriMatcher.match(uri);
        switch (uriType){
            case EVENT_TYPE:
                queryBuilder.setTables(EventTypeTable.TABLE_NAME);
                //Log.w("EventType Query","EventType ALL INFO Query made");
                break;
            case EVENT_TYPE_ID:
                queryBuilder.setTables(EventTypeTable.TABLE_NAME);
                queryBuilder.appendWhere(EventTypeTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                //Log.w("EventType Query","EventType ID INFO Query made");
                break;
            case EVENT:
                queryBuilder.setTables(EventTable.TABLE_NAME);
                //Log.w("Event Query", "Event ALL INFO Query made");
                break;
            case EVENT_ID:
                queryBuilder.setTables(EventTable.TABLE_NAME);
                queryBuilder.appendWhere(EventTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                //Log.w("Event Query","Event ID INFO Query made");
                break;
            case LOGGED_USER:
                queryBuilder.setTables(LoggedUserTable.TABLE_NAME);
                break;
            case LOGGED_USER_ID:
                queryBuilder.setTables(LoggedUserTable.TABLE_NAME);
                queryBuilder.appendWhere(LoggedUserTable.COLUMN_ID + "=" + uri.getLastPathSegment());
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType){
            case EVENT_TYPE:
                id = sqlDB.insert(EventTypeTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(EVENT_TYPE_PATH + "/" + id);
            case EVENT:
                id = sqlDB.insert(EventTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(EVENT_PATH + "/" + id);
            case LOGGED_USER:
                id = sqlDB.insert(LoggedUserTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(LOGGED_USER_PATH + "/" + id);
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        String id;

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType){
            case EVENT_TYPE:
                rowsDeleted = sqlDB.delete(EventTypeTable.TABLE_NAME, selection, selectionArgs);
                break;
            case EVENT_TYPE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(EventTypeTable.TABLE_NAME, EventTypeTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(EventTypeTable.TABLE_NAME, EventTypeTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case EVENT:
                rowsDeleted = sqlDB.delete(EventTable.TABLE_NAME, selection, selectionArgs);
                break;
            case EVENT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(EventTable.TABLE_NAME, EventTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(EventTable.TABLE_NAME, EventTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case LOGGED_USER:
                rowsDeleted = sqlDB.delete(LoggedUserTable.TABLE_NAME, selection, selectionArgs);
                break;
            case LOGGED_USER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(LoggedUserTable.TABLE_NAME, LoggedUserTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(LoggedUserTable.TABLE_NAME, LoggedUserTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        String id;

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowUpdated = 0;
        switch (uriType){
            case EVENT_TYPE:
                rowUpdated = sqlDB.update(EventTypeTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case EVENT_TYPE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(EventTypeTable.TABLE_NAME, values, EventTypeTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(EventTypeTable.TABLE_NAME, values, EventTypeTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case EVENT:
                rowUpdated = sqlDB.update(EventTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case EVENT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(EventTable.TABLE_NAME, values, EventTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(EventTable.TABLE_NAME, values, EventTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case LOGGED_USER:
                rowUpdated = sqlDB.update(LoggedUserTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LOGGED_USER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(LoggedUserTable.TABLE_NAME, values, LoggedUserTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(LoggedUserTable.TABLE_NAME, values, LoggedUserTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowUpdated;
    }

    /*private void checkColumns(Uri uri, String[] projection){
        int uriType = uriMatcher.match(uri);
        String[] realColumns;
        switch (uriType){
            case EVENT_TYPE | EVENT_TYPE_ID:
                realColumns = EventTypeTable.ALL_COLUMNS;
                break;
            default:
                throw new IllegalArgumentException("Table not found");
        }
        if (projection!=null){
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(realColumns));
            if (!availableColumns.containsAll(requestedColumns)){
                throw new IllegalArgumentException("Unknow column in projection");
            }
        }
    }*/

}
