package com.destiny.event.scheduler.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.utils.NetworkUtils;

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
import java.util.TimeZone;

public class NewGameNotificationService extends IntentService {

    private static final String TAG = "NewGameNotifyService";

    private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    private static final String GAME_ENDPOINT = "game";
    private static final String STATUS_PARAM = "?status=0";
    private static final String JOINED_PARAM = "&joined=false";
    private static final String MEMBER_HEADER = "membership";
    private static final String PLATFORM_HEADER = "platform";
    private static final String TIMEZONE_HEADER = "zoneid";
    private static final String GET_METHOD = "GET";

    private String memberId;
    private int platformId;

    public NewGameNotificationService() {
        super(NewGameNotificationService.class.getName());
    }

    SharedPreferences sharedPrefs;
    ArrayList<Integer> selectedIds;
    ArrayList<GameModel> gameList;

    ArrayList<String> previousGames;

    @Override
    protected void onHandleIntent(Intent intent) {

        sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        selectedIds = new ArrayList<>();

        int typeIds[] = getResources().getIntArray(R.array.event_type_ids);
        for (int typeId : typeIds) {
            boolean b = sharedPrefs.getBoolean(String.valueOf(typeId), false);
            if (b) { selectedIds.add(typeId); }
        }
        Log.w(TAG, "selectedIds size: " + selectedIds.size());

        previousGames = getPreviousGamesList(sharedPrefs.getString(DrawerActivity.NEW_GAMES_PREF,""));

        memberId = intent.getStringExtra("memberId");
        platformId = intent.getIntExtra("platformId",0);

        requestServer();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "NewGameNotificationService running.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "NewGameNotificationService destroyed");
    }

    private void requestServer() {
        String url = SERVER_BASE_URL + GAME_ENDPOINT + STATUS_PARAM + JOINED_PARAM;

        try{
            if (NetworkUtils.checkConnection(this)){
                URL myURL = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
                urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
                urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
                urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
                urlConnection.setRequestMethod(GET_METHOD);

                if (urlConnection.getResponseCode() == 200){
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    parseGames(response);
                    boolean hasSelectedGames = checkifHasSelectedGames();
                    if (hasSelectedGames) {
                        Log.w(TAG, "New games found. Making notification.");
                        makeNotification();
                    } else {
                        Log.w(TAG, "No new games found.");
                    }
                } else { Log.w(TAG, "Status code different than 200."); }

            } else { Log.w(TAG, "No internet connection."); }
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Error in HTTP Request");
        }

    }

    private void makeNotification() {
        Log.w(TAG, "Creating New Game notification...");
        if (sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false)) {
            Intent nIntent = new Intent(getApplicationContext(), DrawerActivity.class);
            nIntent.putExtra("notification", 1);
            nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
            nBuilder.setSmallIcon(R.drawable.ic_event_new);
            nBuilder.setLargeIcon(getLargeIcon(getResources().getIdentifier("ic_launcher","mipmap",getPackageName())));
            nBuilder.setContentTitle(getString(R.string.menu_new_event));
            nBuilder.setContentText(getString(R.string.new_like_events));
            setPriority(nBuilder);
            setVisibility(nBuilder);
            nBuilder.setContentIntent(pIntent);
            nBuilder.setAutoCancel(true);

            boolean sound = sharedPrefs.getBoolean(DrawerActivity.SOUND_PREF,false);
            if (sound){
                nBuilder.setDefaults(Notification.DEFAULT_ALL);
            } else nBuilder.setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.notify(0, nBuilder.build());
        }
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
        //paint.setColorFilter(new PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bigIcon,0,0,paint);
        return finalIcon;
    }

    private ArrayList<String> getPreviousGamesList(String string) {
        Log.w(TAG, "previousGames string: " + string);
        if (string.equals(",42")) string = "";
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
            editor.putString(DrawerActivity.NEW_GAMES_PREF, gameIds);
            editor.apply();
        }
        return numberOfGames > 0;
    }

    private void parseGames(String response){
        Log.w(TAG, "GetNewGames response: " + response);
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
                game.setEventName(jEvent.getString("name"));
                game.setEventIcon(jEvent.getString("icon"));
                game.setMaxGuardians(jEvent.getInt("maxGuardians"));
                JSONObject jType = jEvent.getJSONObject("eventType");
                game.setTypeId(jType.getInt("id"));
                game.setTypeName(jType.getString("name"));
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

    private boolean getBoolean(String joined) {
        return joined.equals("true");
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        return result;
    }
}
