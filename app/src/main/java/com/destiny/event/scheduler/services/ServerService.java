package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class ServerService extends IntentService {

    private static final String TAG = "ServerService";

    private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    private static final String GAME_ENDPOINT = "game";

    private static final String MEMBER_HEADER = "membership";
    private static final String PLATFORM_HEADER = "platform";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static final String RESQUEST_TAG = "request";
    public static final String ERROR_TAG = "error";
    public static final String MEMBER_TAG = "memberId";
    public static final String PLATFORM_TAG = "platformId";
    public static final String EVENT_TAG = "eventId";
    public static final String TIME_TAG = "time";
    public static final String LIGHT_TAG = "minLight";
    public static final String RECEIVER_TAG = "receiver";
    public static final String INT_TAG = "intData";
    public static final String STRING_TAG = "stringData";
    public static final String GAME_TAG = "gameList";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 210;
    public static final int STATUS_ERROR = 2404;

    public static final int TYPE_NEW_GAMES = 1;
    public static final int TYPE_CREATE_GAME = 2;

    public static final int NO_ERROR = 0;
    public static final int ERROR_INCORRECT_REQUEST = 10;
    public static final int ERROR_NO_CONNECTION = 20;
    public static final int ERROR_HTTP_REQUEST = 30;
    public static final int ERROR_RESPONSE_CODE = 40;
    public static final int ERROR_NULL_RESPONSE = 50;
    public static final int ERROR_INCORRECT_RESPONSE = 60;
    public static final int ERROR_JSON = 70;

    public static final String RUNNING_SERVICE = "serverRunning";

    private int gameId;
    private String memberId;
    private int platformId;
    private ArrayList<GameModel> gameList;

    public ServerService() {
        super(ServerService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, true);
        editor.apply();
        Log.w(TAG, "Service running!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, false);
        editor.apply();
        Log.w(TAG, "Service destroyed!");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int type = intent.getIntExtra(RESQUEST_TAG, 0);
        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_TAG);
        memberId = intent.getStringExtra(MEMBER_TAG);
        platformId = intent.getIntExtra(PLATFORM_TAG, 0);
        int error;
        String url;
        Bundle bundle = new Bundle();

        switch (type){
            case 0:
                sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_CREATE_GAME:
                bundle.clear();
                bundle.putInt(EVENT_TAG, intent.getIntExtra(EVENT_TAG, 0));
                bundle.putString(TIME_TAG, intent.getStringExtra(TIME_TAG));
                bundle.putInt(LIGHT_TAG, intent.getIntExtra(LIGHT_TAG, 0));
                url = SERVER_BASE_URL + GAME_ENDPOINT;
                error = requestServer(receiver, type, url, bundle);
                if (error != NO_ERROR){
                    sendError(receiver, error);
                } else sendIntData(receiver, gameId);
                break;
            case TYPE_NEW_GAMES:
                url = SERVER_BASE_URL + GAME_ENDPOINT;
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR) {
                    sendError(receiver, error);
                } else sendSerializableData(receiver, gameList);
                break;
        }
        this.stopSelf();

    }

    private void sendSerializableData(ResultReceiver receiver, ArrayList<GameModel> gameList) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_TAG, gameList);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendIntData(ResultReceiver receiver, int data) {
        Bundle bundle = new Bundle();
        bundle.putInt(INT_TAG, data);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendError(ResultReceiver receiver, int error) {
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putInt(ERROR_TAG, error);
        receiver.send(STATUS_ERROR, bundle);
    }

    private int requestServer(ResultReceiver receiver, int type, String url, Bundle bundle) {
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        try {
            if (NetworkUtils.checkConnection(getApplicationContext())){
                URL myURL = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();

                switch (type){
                    case TYPE_CREATE_GAME:
                        urlConnection = setCreateGameCall(urlConnection, bundle);
                        break;
                    case TYPE_NEW_GAMES:
                        urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);
                        break;
                }

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    if (response != null){
                        try {
                            switch (type){
                                case TYPE_CREATE_GAME:
                                    gameId = Integer.parseInt(response);
                                    return NO_ERROR;
                                case TYPE_NEW_GAMES:
                                    int error = parseNewGames(response);
                                    if (error != NO_ERROR){
                                        return error;
                                    } else return NO_ERROR;
                                default:
                                    return NO_ERROR;
                            }
                        } catch (Exception e){
                            return ERROR_INCORRECT_RESPONSE;
                        }
                    } else {
                        Log.w(TAG, "Response is null");
                        return ERROR_NULL_RESPONSE;
                    }
                } else {
                    Log.w(TAG, "Response code different than 200");
                    return ERROR_RESPONSE_CODE;
                }
            } else {
                Log.w(TAG, "No internet connection found");
                return ERROR_NO_CONNECTION;
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Error in HTTP request");
            return ERROR_HTTP_REQUEST;
        }
    }

    private int parseNewGames(String response){
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
                game.setEventName(jEvent.getString("name"));
                game.setEventIcon(jEvent.getString("icon"));
                game.setMaxGuardians(jEvent.getInt("maxGuardians"));
                JSONObject jType = jEvent.getJSONObject("eventType");
                game.setTypeName(jType.getString("name"));
                game.setTime(jGame.getString("time"));
                game.setMinLight(jGame.getInt("light"));
                game.setInscriptions(jGame.getInt("inscriptions"));
                gameList.add(game);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR_JSON;
        }
        return NO_ERROR;
    }

    private HttpURLConnection setCreateGameCall(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject gameJSON = createCreateGameJSON(bundle.getInt(EVENT_TAG), bundle.getString(TIME_TAG), bundle.getInt(LIGHT_TAG));
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(gameJSON.toString());
        writer.flush();

        return urlConnection;
    }

    private HttpURLConnection getDefaultHeaders(HttpURLConnection urlConnection, String postMethod) throws ProtocolException {
        urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
        urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
        urlConnection.setRequestMethod(postMethod);
        return urlConnection;
    }

    private JSONObject createCreateGameJSON(int eventId, String time, int minLight) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject jEvent = new JSONObject();
        jEvent.put("id",eventId);
        json.put("event",jEvent);
        json.put("time",time);
        json.put("light",minLight);
        json.put("status",0);
        Log.w(TAG, "GameJSON: " + json.toString());
        return json;
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
