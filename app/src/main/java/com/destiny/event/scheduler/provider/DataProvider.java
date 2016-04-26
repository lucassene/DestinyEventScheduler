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

import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;

public class DataProvider extends ContentProvider {

    private DBHelper database;

    private static final int EVENT_TYPE = 10;
    private static final int EVENT_TYPE_ID = 11;
    private static final int EVENT = 20;
    private static final int EVENT_ID = 21;
    private static final int LOGGED_USER = 30;
    private static final int LOGGED_USER_ID = 31;
    private static final int CLAN = 40;
    private static final int CLAN_ID = 41;
    private static final int MEMBER = 50;
    private static final int MEMBER_ID = 51;
    private static final int GAME = 60;
    private static final int GAME_ID = 61;
    private static final int ENTRY = 70;
    private static final int ENTRY_ID = 71;

    private static final String AUTHORITY = "com.destiny.event.scheduler.provider";

    private static final String EVENT_TYPE_PATH = "eventtype";
    public static final Uri EVENT_TYPE_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_TYPE_PATH);
    private static final String EVENT_PATH = "event";
    public static final Uri EVENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_PATH);
    private static final String LOGGED_USER_PATH = "loggeduser";
    public static final Uri LOGGED_USER_URI = Uri.parse("content://" + AUTHORITY + "/" + LOGGED_USER_PATH);
    private static final String CLAN_PATH = "clan";
    public static final Uri CLAN_URI = Uri.parse("content://" + AUTHORITY + "/" + CLAN_PATH);
    private static final String MEMBER_PATH = "members";
    public static final Uri MEMBER_URI = Uri.parse("content://" + AUTHORITY + "/" + MEMBER_PATH);
    private static final String GAME_PATH = "game";
    public static final Uri GAME_URI = Uri.parse("content://" + AUTHORITY + "/" + GAME_PATH);
    private static final String ENTRY_PATH = "entry";
    public static final Uri ENTRY_URI = Uri.parse("content://" + AUTHORITY + "/" + ENTRY_PATH);

    public static final String EVENT_TYPE_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/alleventtype";
    public static final String EVENT_TYPE_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleeventtype";
    public static final String EVENT_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allevent";
    public static final String EVENT_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleevent";
    public static final String LOGGED_USER_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allloggeduser";
    public static final String LOGGED_USER_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleloggeduser";
    public static final String CLAN_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allclan";
    public static final String CLAN_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleclan";
    public static final String MEMBER_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allmember";
    public static final String MEMBER_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singlemember";
    public static final String GAME_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allgame";
    public static final String GAME_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singlegame";
    public static final String ENTRY_CONTENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/allentry";
    public static final String ENTRY_ITEM_CONTENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/singleentry";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, EVENT_TYPE_PATH, EVENT_TYPE);
        uriMatcher.addURI(AUTHORITY, EVENT_TYPE_PATH + "/#", EVENT_TYPE_ID);
        uriMatcher.addURI(AUTHORITY, EVENT_PATH, EVENT);
        uriMatcher.addURI(AUTHORITY, EVENT_PATH + "/#", EVENT_ID);
        uriMatcher.addURI(AUTHORITY, LOGGED_USER_PATH, LOGGED_USER);
        uriMatcher.addURI(AUTHORITY, LOGGED_USER_PATH + "/#", LOGGED_USER_ID);
        uriMatcher.addURI(AUTHORITY, CLAN_PATH, CLAN);
        uriMatcher.addURI(AUTHORITY, CLAN_PATH + "/#", CLAN_ID);
        uriMatcher.addURI(AUTHORITY, MEMBER_PATH, MEMBER);
        uriMatcher.addURI(AUTHORITY, MEMBER_PATH + "/#", MEMBER_ID);
        uriMatcher.addURI(AUTHORITY, GAME_PATH, GAME);
        uriMatcher.addURI(AUTHORITY, GAME_PATH + "/#", GAME_ID);
        uriMatcher.addURI(AUTHORITY, ENTRY_PATH, ENTRY);
        uriMatcher.addURI(AUTHORITY, ENTRY_PATH + "/#", ENTRY_ID);
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
                break;
            case CLAN:
                queryBuilder.setTables(ClanTable.TABLE_NAME);
                break;
            case CLAN_ID:
                queryBuilder.setTables(ClanTable.TABLE_NAME);
                queryBuilder.appendWhere(ClanTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case MEMBER:
                queryBuilder.setTables(MemberTable.TABLE_NAME);
                break;
            case MEMBER_ID:
                queryBuilder.setTables(MemberTable.TABLE_NAME);
                queryBuilder.appendWhere(MemberTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case GAME:
                StringBuilder sbGame = new StringBuilder();
                sbGame.append(GameTable.TABLE_NAME);
                sbGame.append(" JOIN ");
                sbGame.append(MemberTable.TABLE_NAME);
                sbGame.append(" ON ");
                sbGame.append(GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR));
                sbGame.append(" = ");
                sbGame.append(MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP));
                sbGame.append(" JOIN ");
                sbGame.append(EventTable.TABLE_NAME);
                sbGame.append(" ON ");
                sbGame.append(GameTable.getQualifiedColumn(GameTable.COLUMN_EVENT_ID));
                sbGame.append(" = ");
                sbGame.append(EventTable.getQualifiedColumn(EventTable.COLUMN_ID));
                queryBuilder.setTables(sbGame.toString());
                break;
            case GAME_ID:
                queryBuilder.setTables(GameTable.TABLE_NAME);
                queryBuilder.appendWhere(GameTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case ENTRY:
                StringBuilder sbEntry = new StringBuilder();
                sbEntry.append(EntryTable.TABLE_NAME);
                sbEntry.append(" JOIN ");
                sbEntry.append(GameTable.TABLE_NAME);
                sbEntry.append(" ON ");
                sbEntry.append(EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME));
                sbEntry.append(" = ");
                sbEntry.append(GameTable.getQualifiedColumn(GameTable.COLUMN_ID));
                sbEntry.append(" JOIN ");
                sbEntry.append(EventTable.TABLE_NAME);
                sbEntry.append(" ON ");
                sbEntry.append(GameTable.getQualifiedColumn(GameTable.COLUMN_EVENT_ID));
                sbEntry.append(" = ");
                sbEntry.append(EventTable.getQualifiedColumn(EventTable.COLUMN_ID));
                sbEntry.append(" JOIN ");
                sbEntry.append(MemberTable.TABLE_NAME);
                sbEntry.append(" ON ");
                sbEntry.append(EntryTable.getQualifiedColumn(EntryTable.COLUMN_MEMBERSHIP));
                sbEntry.append(" = ");
                sbEntry.append(MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP));
                queryBuilder.setTables(sbEntry.toString());
                break;
            case ENTRY_ID:
                queryBuilder.setTables(EntryTable.TABLE_NAME);
                queryBuilder.appendWhere(EntryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
/*            case GAME_WITH_EVENT:
                StringBuilder sb1 = new StringBuilder();
                sb1.append(GameTable.TABLE_NAME);
                sb1.append(" JOIN ");
                sb1.append(EventTable.TABLE_NAME);
                sb1.append(" ON ");
                sb1.append(GameTable.TABLE_NAME + "." + GameTable.COLUMN_EVENT_ID);
                sb1.append(" = ");
                sb1.append(EventTable.TABLE_NAME + "." + EventTable.COLUMN_ID);
                sb1.append(";");
                queryBuilder.setTables(sb1.toString());
                break;*/
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
            case CLAN:
                id = sqlDB.insert(ClanTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CLAN_PATH + "/" + id);
            case MEMBER:
                id = sqlDB.insert(MemberTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(MEMBER_PATH + "/" + id);
            case GAME:
                id = sqlDB.insert(GameTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                Log.w("Insert", "Game created sucessfully!");
                return Uri.parse(GAME_PATH + "/" + id);
            case ENTRY:
                id = sqlDB.insert(EntryTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(ENTRY_PATH + "/" + id);
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
            case CLAN:
                rowsDeleted = sqlDB.delete(ClanTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CLAN_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(ClanTable.TABLE_NAME, ClanTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(ClanTable.TABLE_NAME, ClanTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case MEMBER:
                rowsDeleted = sqlDB.delete(MemberTable.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMBER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(MemberTable.TABLE_NAME, MemberTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(MemberTable.TABLE_NAME, MemberTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case GAME:
                rowsDeleted = sqlDB.delete(GameTable.TABLE_NAME, selection, selectionArgs);
                break;
            case GAME_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(GameTable.TABLE_NAME, GameTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(GameTable.TABLE_NAME, GameTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case ENTRY:
                rowsDeleted = sqlDB.delete(EntryTable.TABLE_NAME, selection, selectionArgs);
                break;
            case ENTRY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(EntryTable.TABLE_NAME, EntryTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(EntryTable.TABLE_NAME, EntryTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
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
            case CLAN:
                rowUpdated = sqlDB.update(ClanTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CLAN_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(ClanTable.TABLE_NAME, values, ClanTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(ClanTable.TABLE_NAME, values, ClanTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case MEMBER:
                rowUpdated = sqlDB.update(MemberTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MEMBER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(MemberTable.TABLE_NAME, values, MemberTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(MemberTable.TABLE_NAME, values, MemberTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case GAME:
                rowUpdated = sqlDB.update(GameTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case GAME_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(GameTable.TABLE_NAME, values, GameTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(GameTable.TABLE_NAME, values, GameTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case ENTRY:
                rowUpdated = sqlDB.update(EntryTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ENTRY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowUpdated = sqlDB.update(EntryTable.TABLE_NAME, values, EntryTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowUpdated = sqlDB.update(EntryTable.TABLE_NAME, values, EntryTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
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
