package com.app.the.bunker.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.data.LoggedUserTable;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.models.GameModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.CipherUtils;
import com.app.the.bunker.utils.DateUtils;
import com.app.the.bunker.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import static com.app.the.bunker.Constants.AUTH_HEADER;
import static com.app.the.bunker.Constants.CLAN_HEADER;
import static com.app.the.bunker.Constants.DONE_ENDPOINT;
import static com.app.the.bunker.Constants.GAME_ENDPOINT;
import static com.app.the.bunker.Constants.GET_METHOD;
import static com.app.the.bunker.Constants.JOINED_PARAM;
import static com.app.the.bunker.Constants.LOGIN_ENDPOINT;
import static com.app.the.bunker.Constants.MEMBER_HEADER;
import static com.app.the.bunker.Constants.PLATFORM_HEADER;
import static com.app.the.bunker.Constants.SERVER_BASE_URL;
import static com.app.the.bunker.Constants.STATUS_PARAM;
import static com.app.the.bunker.Constants.TIMEZONE_HEADER;

public class ServerSyncService extends IntentService {

    private static final String TAG = "ServerSyncService";

    private String memberId;
    private int platformId;
    private int clanId;
    private boolean hasTriedOnce = false;
    private boolean hasNewGames = false;
    private boolean hasDoneGames = false;
    private String priorUrl;

    public ServerSyncService() {
        super(ServerSyncService.class.getName());
    }

    SharedPreferences sharedPrefs;
    ArrayList<Integer> selectedIds;
    ArrayList<GameModel> gameList;

    ArrayList<String> previousGames;

    @Override
    protected void onHandleIntent(Intent intent) {

        sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        selectedIds = new ArrayList<>();

        int typeIds[] = getIdArray();

        if (typeIds != null) {
            for (int typeId : typeIds) {
                boolean b = sharedPrefs.getBoolean(String.valueOf(typeId), false);
                if (b) {
                    selectedIds.add(typeId);
                }
            }

            previousGames = getPreviousGamesList(sharedPrefs.getString(Constants.NEW_GAMES_PREF, ""));

            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(DataProvider.LOGGED_USER_URI, LoggedUserTable.ALL_COLUMNS, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    memberId = cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
                    platformId = cursor.getInt(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_PLATFORM));
                    clanId = cursor.getInt(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_CLAN));

                    boolean isNewNotifyAllowed = sharedPrefs.getBoolean(Constants.NEW_NOTIFY_PREF, true);
                    if (isNewNotifyAllowed) {
                        String url = SERVER_BASE_URL + GAME_ENDPOINT + STATUS_PARAM + "0" + JOINED_PARAM + "false";
                        requestServer(ServerService.TYPE_NEW_GAMES, url);
                    }

                    String lastNotification = sharedPrefs.getString(Constants.LAST_DAILY_PREF, "23-04-2100T10:42");
                    Calendar lastCal = DateUtils.stringToDate(lastNotification);
                    lastCal.add(Calendar.DAY_OF_MONTH, 1);
                    Calendar now = Calendar.getInstance();

                    if (now.getTimeInMillis() > lastCal.getTimeInMillis()) {
                    String date = DateUtils.calendarToString(now);
                    SharedPreferences.Editor edit = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
                    edit.putString(Constants.LAST_DAILY_PREF, date);
                    edit.apply();
                    if (sharedPrefs.getBoolean(Constants.DONE_NOTIFY_PREF, true))
                        getDoneGames();
                    getNewEvents();
                    getNewMembers();
                    } else Log.w(TAG, "Checks already happened today.");
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, false)) {
            String url = SERVER_BASE_URL + GAME_ENDPOINT + STATUS_PARAM + "0" + JOINED_PARAM + "true";
            requestServer(ServerService.TYPE_SYNC_SCHEDULED, url);
        }

    }

    private void getNewMembers() {
        Cursor cursor = null;
        try{
            cursor = getContentResolver().query(DataProvider.MEMBER_URI, new String[] {MemberTable.COLUMN_MEMBERSHIP}, null, null, null);
            if (cursor != null && cursor.moveToFirst()){
                ArrayList<String> mList = new ArrayList<>();
                for (int i=0;i<cursor.getCount();i++){
                    mList.add(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                    cursor.moveToNext();
                }
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
                intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.TYPE_UPDATE_CLAN);
                intent.putExtra("memberList", mList);
                intent.putExtra("clanId", String.valueOf(clanId));
                intent.putExtra("platformId", platformId);
                intent.putExtra("userMembership", memberId);
                startService(intent);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void getNewEvents() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
        intent.putExtra(ServerService.REQUEST_TAG, ServerService.TYPE_NEW_EVENTS);
        intent.putExtra(ServerService.MEMBER_TAG, memberId);
        intent.putExtra(ServerService.PLATFORM_TAG, platformId);
        intent.putExtra(ServerService.CLAN_TAG, String.valueOf(clanId));
        startService(intent);
    }

    private void getDoneGames() {
        String url = SERVER_BASE_URL + GAME_ENDPOINT + DONE_ENDPOINT;
        requestServer(ServerService.TYPE_DONE, url);
        if (hasNewGames || hasDoneGames) { makeNotification(); }
    }

    private int[] getIdArray() {
        Cursor cursor = null;
        ArrayList<Integer> list = new ArrayList<>();
        try {
            cursor = getContentResolver().query(DataProvider.EVENT_TYPE_URI, EventTypeTable.ALL_COLUMNS, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    list.add(cursor.getInt(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ID)));
                    cursor.moveToNext();
                }
            }
            return convertList(list);
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Error with getIdArray()");
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private int[] convertList(ArrayList<Integer> list) {
        int[] items = new int[list.size()];
        int index = 0;
        for (Integer ob : list){
            items[index++] = ob;
        }
        return items;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "ServerSyncService running.");
    }

    @Override
    public void onDestroy() {
        //DBHelper database = DBHelper.getInstance(getApplicationContext());
        //SQLiteDatabase db = database.getWritableDatabase();
        //database.close();
        //db.close();
        super.onDestroy();
        Log.w(TAG, "ServerSyncService destroyed");
    }

    private void requestServer(int type, String url) {
        try {
            if (NetworkUtils.checkConnection(this)) {
                URL myURL = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(10000);
                urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
                urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
                urlConnection.setRequestProperty(CLAN_HEADER, String.valueOf(clanId));
                urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
                String authKey = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).getString(Constants.KEY_PREF, "");
                try {
                    if (!authKey.isEmpty()) {
                        CipherUtils cipher = new CipherUtils();
                        urlConnection.setRequestProperty(AUTH_HEADER, cipher.decrypt(authKey));
                    } else urlConnection.setRequestProperty(AUTH_HEADER, authKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                urlConnection.setRequestMethod(GET_METHOD);
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == 200) {
                    switch (type) {
                        case ServerService.TYPE_NEW_GAMES:
                            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertInputStreamToString(inputStream);
                            if (response != null) {
                                parseGames(response);
                                hasNewGames = checkifHasSelectedGames();
                            }
                            break;
                        case ServerService.TYPE_LOGIN:
                            authKey = urlConnection.getHeaderField(AUTH_HEADER);
                            CipherUtils cipher = new CipherUtils();
                            SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
                            editor.putString(Constants.KEY_PREF, cipher.encrypt(authKey));
                            editor.apply();
                            requestServer(ServerService.TYPE_NEW_GAMES, priorUrl);
                            break;
                        case ServerService.TYPE_DONE:
                            InputStream iStream = new BufferedInputStream(urlConnection.getInputStream());
                            String resp = convertInputStreamToString(iStream);
                            if (resp != null) {
                                parseGames(resp);
                                if (gameList.size() > 0) {
                                    hasDoneGames = true;
                                    Log.w(TAG, "Done games found.");
                                } else {
                                    Log.w(TAG, "No done games found.");
                                    hasDoneGames = false;
                                }
                            }
                            break;
                        case ServerService.TYPE_SYNC_SCHEDULED:
                            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                            String r = convertInputStreamToString(stream);
                            if (r != null) {
                                parseGames(r);
                                if (gameList.size() > 0) {
                                    Log.w(TAG, "calling CreateNotificationService...");
                                    Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CreateNotificationService.class);
                                    intent.putExtra(CreateNotificationService.GAME_HEADER, gameList);
                                    startService(intent);
                                } else Log.w(TAG, "No schedule events found.");
                            }
                            break;
                    }
                } else {
                    Log.w(TAG, "Status code different than 200.");
                    if (statusCode == 500 || statusCode == 403) {
                        if (!hasTriedOnce) {
                            priorUrl = url;
                            String lUrl = SERVER_BASE_URL + LOGIN_ENDPOINT;
                            requestServer(ServerService.TYPE_LOGIN, lUrl);
                            hasTriedOnce = true;
                        }
                    }
                }
            } else {
                Log.w(TAG, "No internet connection.");
            }
        } catch (StackOverflowError e){
            e.printStackTrace();
            Log.w(TAG,"StackOverflowError");
        } catch (java.net.SocketTimeoutException e) {
            e.printStackTrace();
            Log.w(TAG, "Time out");
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Error in HTTP Request");
        }

    }

    private void makeNotification() {
        Log.w(TAG, "Creating New Game notification...");
        Intent nIntent = new Intent(getApplicationContext(), DrawerActivity.class);
        nIntent.putExtra("notification", 1);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_event_new);
        nBuilder.setLargeIcon(getLargeIcon(getResources().getIdentifier("ic_launcher","mipmap",getPackageName())));

        boolean sound = sharedPrefs.getBoolean(Constants.SOUND_PREF,false);
        if (sound){
            nBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else nBuilder.setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

        if (hasNewGames){
            nBuilder.setContentTitle(getString(R.string.menu_new_event));
            nBuilder.setContentText(getString(R.string.new_like_events));
        } else if (hasDoneGames){
            nBuilder.setContentTitle("Validate events");
            nBuilder.setContentText("You have events to validate or evaluate.");
        }

        setPriority(nBuilder);
        setVisibility(nBuilder);
        nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0, nBuilder.build());
    }

    @TargetApi(21)
    private void setVisibility(NotificationCompat.Builder nBuilder) {
        nBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
    }

    @TargetApi(16)
    private void setPriority(NotificationCompat.Builder nBuilder) {
        nBuilder.setPriority(Notification.PRIORITY_DEFAULT);
    }

    private Bitmap getLargeIcon(int iconId){
        Drawable smallIcon = ContextCompat.getDrawable(getApplicationContext(),iconId);
        BitmapDrawable bD = (BitmapDrawable) smallIcon;
        Bitmap bigIcon = bD.getBitmap();
        Bitmap finalIcon = Bitmap.createBitmap(bigIcon.getWidth(), bigIcon.getHeight(), bigIcon.getConfig());
        Canvas canvas = new Canvas(finalIcon);
        Paint paint = new Paint();
        canvas.drawBitmap(bigIcon,0,0,paint);
        return finalIcon;
    }

    private ArrayList<String> getPreviousGamesList(String string) {
        Log.w(TAG, "previousGames string: " + string);
        ArrayList<String> list = new ArrayList<>();
        if (!string.equals("")){
            while (string.length()>0){
                String gameId = string.substring(0,string.indexOf(","));
                Log.w(TAG, "Adding " + gameId + " to the list" );
                list.add(gameId);
                string = string.replace(gameId+",","");
            }
            Log.w(TAG, "previousGamesList size: " + list.size());
            return list;
        }
        return null;
    }

    private boolean checkifHasSelectedGames() {
        String gameIds = "";
        int numberOfGames = 0;
        ArrayList<String> idList = new ArrayList<>();
        Log.w(TAG, "gameList size: " + gameList.size());
        for (int i=0;i<gameList.size();i++){
            for (int x=0;x<selectedIds.size();x++){
                Log.w(TAG, "Comparing game.typeId: " + gameList.get(i).getTypeId() + " with selectedTypeId: " + selectedIds.get(x));
                if (gameList.get(i).getTypeId() == selectedIds.get(x)){
                    idList.add(String.valueOf(gameList.get(i).getGameId()));
                    gameIds = gameIds + String.valueOf(gameList.get(i).getGameId()) + ",";
                    numberOfGames++;
                }
            }
        }
        if (previousGames != null){
            for (int i=0;i<previousGames.size();i++){
                for (int x=0;x<idList.size();x++){
                    if (previousGames.get(i).equals(idList.get(x))){
                        idList.remove(x);
                        x--;
                    }
                }
            }
            numberOfGames = idList.size();
        }
        if (!gameIds.equals("")){
            Log.w(TAG, "Saving string: " + gameIds);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(Constants.NEW_GAMES_PREF, gameIds);
            editor.apply();
        }
        return numberOfGames > 0;
    }

    private void parseGames(String response){
        Log.w(TAG, "GetGames response: " + response);
        JSONArray jResponse;
        gameList = new ArrayList<>();
        try {
            jResponse = new JSONArray(response);
            for (int i=0;i<jResponse.length();i++){
                JSONObject jGame = jResponse.getJSONObject(i);
                GameModel game = new GameModel();
                game.setGameId(jGame.getInt("id"));
                JSONObject jCreator = jGame.getJSONObject("creator");
                game.setCreatorId(jCreator.getString("membership"));
                game.setCreatorName(jCreator.getString("name"));
                JSONObject jEvent = jGame.getJSONObject("event");
                game.setEventId(jEvent.getInt("id"));
                game.setEventName(jEvent.getString(getLanguageString()));
                game.setEventIcon(jEvent.getString("icon"));
                game.setMaxGuardians(jEvent.getInt("maxGuardians"));
                JSONObject jType = jEvent.getJSONObject("eventType");
                game.setTypeId(jType.getInt("id"));
                game.setTypeName(jType.getString(getLanguageString()));
                game.setTime(jGame.getString("time"));
                game.setMinLight(jGame.getInt("light"));
                game.setInscriptions(jGame.getInt("inscriptions"));
                game.setStatus(jGame.getInt("status"));
                game.setJoined(getBoolean(jGame.getString("joined")));
                game.setEvaluated(getBoolean(jGame.getString("evaluated")));
                gameList.add(game);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getLanguageString() {
        getResources();
        String lang = Resources.getSystem().getConfiguration().locale.getLanguage();
        switch (lang) {
            case "pt":
                return lang;
            case "es":
                return lang;
            default:
                return "en";
        }
    }

    private boolean getBoolean(String joined) {
        return joined.equals("true");
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            return result;
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Error converting InputStream to String");
            return null;
        }
    }
}
