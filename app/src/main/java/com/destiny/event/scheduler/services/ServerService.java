package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.models.EvaluationModel;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
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
import java.util.TimeZone;

public class ServerService extends IntentService {

    private static final String TAG = "ServerService";

    private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    private static final String GAME_ENDPOINT = "game";
    private static final String ENTRIES_ENDPOINT = "/entries";
    private static final String JOIN_ENDPOINT = "/join";
    private static final String LEAVE_ENDPOINT = "/leave";
    private static final String VALIDATE_ENDPOINT = "/validate";
    private static final String EVALUATION_ENDPOINT = "/evaluations/";

    private static final String STATUS_PARAM = "status=";
    private static final String JOINED_PARAM = "joined=";

    private static final String MEMBER_HEADER = "membership";
    private static final String PLATFORM_HEADER = "platform";
    private static final String TIMEZONE_HEADER = "zoneid";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String DELETE_METHOD = "DELETE";

    public static final String REQUEST_TAG = "request";
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
    public static final String GAMEID_TAG = "gameId";
    public static final String ENTRY_TAG = "entries";
    public static final String EVALUATIONS_TAG = "evaluations";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 210;
    public static final int STATUS_ERROR = 2404;

    public static final int TYPE_ALL_GAMES = 1;
    public static final int TYPE_CREATE_GAME = 2;
    public static final int TYPE_GAME_ENTRIES = 3;
    public static final int TYPE_JOIN_GAME = 4;
    public static final int TYPE_LEAVE_GAME = 5;
    public static final int TYPE_DELETE_GAME = 6;
    public static final int TYPE_NEW_GAMES = 7;
    public static final int TYPE_JOINED_GAMES = 8;
    public static final int TYPE_HISTORY_GAMES = 9;
    public static final int TYPE_VALIDATE_GAME = 10;
    public static final int TYPE_EVALUATE_GAME = 11;

    public static final int NO_ERROR = 0;
    public static final int ERROR_INCORRECT_REQUEST = 10;
    public static final int ERROR_NO_CONNECTION = 20;
    public static final int ERROR_HTTP_REQUEST = 30;
    public static final int ERROR_RESPONSE_CODE = 40;
    public static final int ERROR_NULL_RESPONSE = 50;
    public static final int ERROR_INCORRECT_RESPONSE = 60;
    public static final int ERROR_JSON = 70;
    public static final int ERROR_INCORRECT_GAMEID = 80;

    public static final String RUNNING_SERVICE = "serverRunning";

    private int gameId;
    private String memberId;
    private int platformId;
    private ArrayList<GameModel> gameList;
    private ArrayList<MemberModel> memberList;

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

        int type = intent.getIntExtra(REQUEST_TAG, 0);
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
            case TYPE_ALL_GAMES:
                url = SERVER_BASE_URL + GAME_ENDPOINT;
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR) {
                    sendError(receiver, error);
                } else sendGameData(receiver, gameList);
                break;
            case TYPE_GAME_ENTRIES:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + ENTRIES_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendEntryData(receiver, memberList);
                } else sendError(receiver, ERROR_INCORRECT_GAMEID);
                break;
            case TYPE_JOIN_GAME:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + JOIN_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_JOIN_GAME);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_LEAVE_GAME:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + LEAVE_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_LEAVE_GAME);
                }
                break;
            case TYPE_DELETE_GAME:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1);
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_DELETE_GAME);
                }
                break;
            case TYPE_NEW_GAMES:
                url = SERVER_BASE_URL + GAME_ENDPOINT + "?" + STATUS_PARAM + "0&" + JOINED_PARAM + "false";
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR) {
                    sendError(receiver, error);
                } else sendGameData(receiver, gameList);
                break;
            case TYPE_JOINED_GAMES:
                url = SERVER_BASE_URL + GAME_ENDPOINT + "?" + JOINED_PARAM + "true";
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR){
                    sendError(receiver, error);
                } else sendGameData(receiver, gameList);
                break;
            case TYPE_HISTORY_GAMES:
                url = SERVER_BASE_URL + GAME_ENDPOINT + "?" + STATUS_PARAM + String.valueOf(GameTable.STATUS_VALIDATED);
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR) {
                    sendError(receiver, error);
                } else sendGameData(receiver, gameList);
                break;
            case TYPE_VALIDATE_GAME:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + VALIDATE_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    bundle.putStringArrayList(ENTRY_TAG, intent.getStringArrayListExtra(ENTRY_TAG));
                    bundle.putParcelableArrayList(EVALUATIONS_TAG, intent.getParcelableArrayListExtra(EVALUATIONS_TAG));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_VALIDATE_GAME);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_EVALUATE_GAME:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + EVALUATION_ENDPOINT + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1);
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    bundle.putParcelableArrayList(EVALUATIONS_TAG, intent.getParcelableArrayListExtra(EVALUATIONS_TAG));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_VALIDATE_GAME);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
        }
        this.stopSelf();

    }

    private void sendIdWithType(ResultReceiver receiver, int gameId, int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(INT_TAG, gameId);
        bundle.putInt(REQUEST_TAG, type);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendEntryData(ResultReceiver receiver, ArrayList<MemberModel> memberList) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ENTRY_TAG, memberList);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendGameData(ResultReceiver receiver, ArrayList<GameModel> gameList) {
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
        Log.w(TAG, "Error: " + error);
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putInt(ERROR_TAG, error);
        receiver.send(STATUS_ERROR, bundle);
    }

    private int requestServer(ResultReceiver receiver, int type, String url, Bundle bundle) {
        Log.w(TAG, "URL: " + url);
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        try {
            if (NetworkUtils.checkConnection(getApplicationContext())){
                URL myURL = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();

                switch (type){
                    case TYPE_CREATE_GAME:
                        urlConnection = createGameRequest(urlConnection, bundle);
                        break;
                    case TYPE_ALL_GAMES:
                    case TYPE_GAME_ENTRIES:
                    case TYPE_NEW_GAMES:
                    case TYPE_JOINED_GAMES:
                    case TYPE_HISTORY_GAMES:
                        urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);
                        break;
                    case TYPE_JOIN_GAME:
                        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
                        break;
                    case TYPE_LEAVE_GAME:
                    case TYPE_DELETE_GAME:
                        urlConnection = getDefaultHeaders(urlConnection, DELETE_METHOD);
                        break;
                    case TYPE_VALIDATE_GAME:
                        urlConnection = createValidateRequest(urlConnection, bundle);
                        break;
                    case TYPE_EVALUATE_GAME:
                        urlConnection = createEvaluationRequest(urlConnection, bundle);
                        break;
                }

                int statusCode;
                if (urlConnection != null){
                    statusCode = urlConnection.getResponseCode();
                } else return ERROR_JSON;

                if (statusCode == 200) {
                    int error = NO_ERROR;
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    if (response != null){
                        try {
                            switch (type){
                                case TYPE_CREATE_GAME:
                                    gameId = Integer.parseInt(response);
                                    return NO_ERROR;
                                case TYPE_ALL_GAMES:
                                case TYPE_NEW_GAMES:
                                case TYPE_JOINED_GAMES:
                                case TYPE_HISTORY_GAMES:
                                    error = parseGames(response);
                                    if (error != NO_ERROR){
                                        return error;
                                    } else return NO_ERROR;
                                case TYPE_GAME_ENTRIES:
                                    error = parseEntries(response);
                                    return error;
                                case TYPE_JOIN_GAME:
                                case TYPE_LEAVE_GAME:
                                case TYPE_DELETE_GAME:
                                case TYPE_VALIDATE_GAME:
                                case TYPE_EVALUATE_GAME:
                                    Log.w(TAG, "Response: " + response);
                                    return error;
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

    private int parseEntries(String response) {
        Log.w(TAG, "GetEntries response: " + response);
        JSONArray jResponse;
        memberList = new ArrayList<>();
        try {
            jResponse = new JSONArray(response);
            for (int i=0;i<jResponse.length();i++){
                JSONObject jEntry = jResponse.getJSONObject(i);
                JSONObject jMember = jEntry.getJSONObject("member");
                MemberModel member = new MemberModel();
                member.setMembershipId(jMember.getString("membership"));
                member.setName(jMember.getString("name"));
                member.setIconPath(jMember.getString("icon"));
                member.setPlatformId(jMember.getInt("platform"));
                member.setLikes(jMember.getInt("likes"));
                member.setDislikes(jMember.getInt("dislikes"));
                member.setGamesCreated(jMember.getInt("gamesCreated"));
                member.setGamesPlayed(jMember.getInt("gamesPlayed"));
                member.setLvl(jMember.getInt("likes"),jMember.getInt("dislikes"),jMember.getInt("gamesPlayed"),jMember.getInt("gamesCreated"));
                member.setEntryTime(jEntry.getString("time"));
                member.setTitle(getString(R.string.default_title));
                memberList.add(member);
            }
        } catch (JSONException e){
            e.printStackTrace();
            return ERROR_JSON;
        }
        return NO_ERROR;
    }

    private int parseGames(String response){
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
            return ERROR_JSON;
        }
        return NO_ERROR;
    }

    private boolean getBoolean(String joined) {
        return joined.equals("true");
    }

    private HttpURLConnection createGameRequest(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
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

    private HttpURLConnection createValidateRequest(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject validateJSON = createValidateJSON(bundle);
        if (validateJSON != null){
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(validateJSON.toString());
            writer.flush();
        } else return null;

        return urlConnection;
    }

    private HttpURLConnection createEvaluationRequest(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONArray evaluationJSON = createEvaluationJSON(bundle);
        if (evaluationJSON != null){
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(evaluationJSON.toString());
            writer.flush();
        } else return null;

        return urlConnection;
    }

    private HttpURLConnection getDefaultHeaders(HttpURLConnection urlConnection, String postMethod) throws ProtocolException {
        urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
        urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
        urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
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

    private JSONObject createValidateJSON(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray jEntriesArray = new JSONArray();
        ArrayList<String> entriesList = bundle.getStringArrayList(ENTRY_TAG);
        if (entriesList != null){
            for (int i=0;i<entriesList.size();i++){
                jEntriesArray.put(Long.valueOf(entriesList.get(i)));
            }
        } else return null;

        JSONArray jEvalArray = new JSONArray();
        ArrayList<EvaluationModel> evalList = bundle.getParcelableArrayList(EVALUATIONS_TAG);
        if (evalList != null){
            for (int i=0;i<evalList.size();i++){
                JSONObject jEvalObject = new JSONObject();
                jEvalObject.put("memberB",Long.valueOf(evalList.get(i).getMembershipId()));
                jEvalObject.put("rate",evalList.get(i).getRate());
                jEvalArray.put(jEvalObject);
            }
        }

        json.put("entries", jEntriesArray);
        json.put("evaluations", jEvalArray);
        Log.w(TAG, "ValidateJSON: " + json.toString());
        return json;
    }


    private JSONArray createEvaluationJSON(Bundle bundle) throws JSONException {
        JSONArray json = new JSONArray();
        ArrayList<EvaluationModel> evalList = bundle.getParcelableArrayList(EVALUATIONS_TAG);
        if (evalList != null){
            for (int i=0;i<evalList.size();i++){
                JSONObject jEvalObject = new JSONObject();
                jEvalObject.put("memberB",Long.valueOf(evalList.get(i).getMembershipId()));
                jEvalObject.put("rate",evalList.get(i).getRate());
                json.put(jEvalObject);
            }
        }
        Log.w(TAG, "EvaluationJSON: " + json.toString());
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
