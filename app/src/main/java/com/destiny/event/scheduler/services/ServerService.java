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
import com.destiny.event.scheduler.models.EventModel;
import com.destiny.event.scheduler.models.EventTypeModel;
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
    private static final String HISTORY_ENDPOINT = "/history";
    private static final String MEMBER_ENDPOINT = "/member/";
    private static final String PROFILE_ENDPOINT = "/profile";
    private static final String EXCEPTION_ENDPOINT = "log-app";
    private static final String MEMBERLIST_ENDPOINT = "member/list";

    private static final String STATUS_PARAM = "status=";
    private static final String JOINED_PARAM = "joined=";

    private static final String MEMBER_HEADER = "membership";
    private static final String PLATFORM_HEADER = "platform";
    private static final String CLAN_HEADER = "clanId";
    private static final String TIMEZONE_HEADER = "zoneid";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String DELETE_METHOD = "DELETE";

    public static final String REQUEST_TAG = "request";
    public static final String ERROR_TAG = "error";
    public static final String MEMBER_TAG = "memberId";
    public static final String PROFILE_TAG = "profile";
    public static final String PLATFORM_TAG = "platformId";
    public static final String EVENT_TAG = "eventId";
    public static final String TIME_TAG = "time";
    public static final String LIGHT_TAG = "minLight";
    public static final String RECEIVER_TAG = "receiver";
    public static final String INT_TAG = "intData";
    public static final String GAME_TAG = "gameList";
    public static final String GAMEID_TAG = "gameId";
    public static final String ENTRY_TAG = "entries";
    public static final String EVALUATIONS_TAG = "evaluations";
    public static final String CLAN_TAG = "clanId";
    public static final String CLASS_TAG = "class";
    public static final String EXCEPTION_TAG = "exception";

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
    public static final int TYPE_HISTORY = 12;
    public static final int TYPE_PROFILE = 13;
    public static final int TYPE_EXCEPTION = 14;
    public static final int TYPE_CLAN_MEMBERS = 15;

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
    private String clanId;
    private String memberId;
    private int platformId;
    private ArrayList<GameModel> gameList;
    private ArrayList<MemberModel> memberList;
    private MemberModel member;
    private int type;

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

        type = intent.getIntExtra(REQUEST_TAG, 0);
        Log.w(TAG, "Request Tag: " + type);
        ResultReceiver receiver;
        if (intent.hasExtra(RECEIVER_TAG)){
            receiver = intent.getParcelableExtra(RECEIVER_TAG);
        } else receiver = null;
        memberId = intent.getStringExtra(MEMBER_TAG);
        clanId = intent.getStringExtra(CLAN_TAG);
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
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + EVALUATION_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    bundle.putParcelableArrayList(EVALUATIONS_TAG, intent.getParcelableArrayListExtra(EVALUATIONS_TAG));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendIdWithType(receiver, intent.getIntExtra(GAMEID_TAG, -1), TYPE_VALIDATE_GAME);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_HISTORY:
                if (intent.getIntExtra(GAMEID_TAG, -1) != -1){
                    url = SERVER_BASE_URL + GAME_ENDPOINT + "/" + intent.getIntExtra(GAMEID_TAG, -1) + HISTORY_ENDPOINT;
                    bundle.clear();
                    bundle.putInt(GAMEID_TAG, intent.getIntExtra(GAMEID_TAG, -1));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendEntryData(receiver, memberList);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_PROFILE:
                if (intent.hasExtra(PROFILE_TAG)){
                    url = SERVER_BASE_URL + MEMBER_ENDPOINT + intent.getStringExtra(PROFILE_TAG) + PROFILE_ENDPOINT;
                    bundle.clear();
                    bundle.putString(PROFILE_TAG, intent.getStringExtra(PROFILE_TAG));
                    error = requestServer(receiver, type, url, bundle);
                    if (error != NO_ERROR){
                        sendError(receiver, error);
                    } else sendMemberData(receiver, member);
                } else sendError(receiver, ERROR_INCORRECT_REQUEST);
                break;
            case TYPE_EXCEPTION:
                if (intent.hasExtra(CLASS_TAG) && intent.hasExtra(EXCEPTION_TAG)){
                    url = SERVER_BASE_URL + EXCEPTION_ENDPOINT;
                    bundle.clear();
                    bundle.putString(CLASS_TAG, intent.getStringExtra(CLASS_TAG));
                    bundle.putString(EXCEPTION_TAG, intent.getStringExtra(EXCEPTION_TAG));
                    requestServer(receiver, type, url, bundle);
                }
                break;
        }
        this.stopSelf();

    }

    private void sendMemberData(ResultReceiver receiver, MemberModel member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROFILE_TAG, member);
        receiver.send(STATUS_FINISHED, bundle);
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
        bundle.putInt(REQUEST_TAG, type);
        bundle.putSerializable(GAME_TAG, gameList);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendIntData(ResultReceiver receiver, int data) {
        Bundle bundle = new Bundle();
        bundle.putInt(INT_TAG, data);
        bundle.putInt(REQUEST_TAG, type);
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
        if (receiver != null) { receiver.send(STATUS_RUNNING, Bundle.EMPTY); }
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
                    case TYPE_HISTORY:
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
                    case TYPE_PROFILE:
                        urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);
                        break;
                    case TYPE_EXCEPTION:
                        urlConnection = createExceptionRequest(urlConnection, bundle);
                        break;
                    case TYPE_CLAN_MEMBERS:
                        urlConnection = createMemberListRequest(urlConnection, bundle);
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
                                    Log.w(TAG, "Response: " + response);
                                    return error;
                                case TYPE_VALIDATE_GAME:
                                case TYPE_EVALUATE_GAME:
                                    Log.w(TAG, "Response: " + response);
                                    String mUrl = SERVER_BASE_URL + MEMBERLIST_ENDPOINT;
                                    int err = requestServer(receiver, TYPE_CLAN_MEMBERS, mUrl, bundle);
                                    Log.w(TAG, "MemberList error: " + err);
                                    if (err == NO_ERROR){
                                        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, LocalService.class);
                                        intent.putExtra(LocalService.REQUEST_HEADER, LocalService.TYPE_UPDATE_MEMBERS);
                                        intent.putExtra(LocalService.MEMBERS_HEADER, memberList);
                                        intent.putExtra(LocalService.CLAN_HEADER, clanId);
                                        startService(intent);
                                        return err;
                                    } else return err;
                                case TYPE_HISTORY:
                                    error = parseHistory(response);
                                    return error;
                                case TYPE_PROFILE:
                                    error = parseProfile(response);
                                    return error;
                                case TYPE_CLAN_MEMBERS:
                                    error = parseMembers(response);
                                    return error;
                                default:
                                    return NO_ERROR;
                            }
                        } catch (Exception e){
                            e.printStackTrace();
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

    private int parseMembers(String response) {
        Log.w(TAG, "MemberList response: " + response);
        memberList = new ArrayList<>();
        try{
            JSONArray jResponse = new JSONArray(response);
            for (int i=0;i<jResponse.length();i++){
                JSONObject jMember = jResponse.getJSONObject(i);
                MemberModel member = new MemberModel();
                member.setMembershipId(jMember.getString("membership"));
                member.setName(jMember.getString("name"));
                member.setIconPath(jMember.getString("icon"));
                member.setPlatformId(jMember.getInt("platform"));
                member.setLikes(jMember.getInt("likes"));
                member.setDislikes(jMember.getInt("dislikes"));
                member.setGamesCreated(jMember.getInt("gamesCreated"));
                member.setGamesPlayed(jMember.getInt("gamesPlayed"));
                EventModel event = new EventModel();
                if (jMember.isNull("favoriteEvent")){
                    event.setEventId(0);
                } else {
                    JSONObject jFavorite = jMember.getJSONObject("favoriteEvent");
                    event.setEventId(jFavorite.getInt("id"));
                }
                member.setFavoriteEvent(event);
                member.setInsert(true);
                memberList.add(member);
            }
            return NO_ERROR;
        } catch (JSONException e){
            e.printStackTrace();
            return ERROR_JSON;
        }
    }

    private int parseProfile(String response) {
        Log.w(TAG, "GetProfile response: " + response);
        member = new MemberModel();
        try {
            JSONObject jResponse = new JSONObject(response);

            JSONObject jMember = jResponse.getJSONObject("member");
            member.setMembershipId(jMember.getString("membership"));
            member.setName(jMember.getString("name"));
            member.setIconPath(jMember.getString("icon"));
            member.setPlatformId(jMember.getInt("platform"));
            member.setLikes(jMember.getInt("likes"));
            member.setDislikes(jMember.getInt("dislikes"));
            member.setGamesCreated(jMember.getInt("gamesCreated"));
            member.setGamesPlayed(jMember.getInt("gamesPlayed"));
            member.setEvaluationsMade(jResponse.getInt("evaluationsMade"));

            JSONArray jTypes = jResponse.getJSONArray("playedTypes");
            ArrayList<EventTypeModel> typeList = new ArrayList<>();
            for (int i=0;i<jTypes.length();i++){
                JSONObject jType = jTypes.getJSONObject(i);
                EventTypeModel type = new EventTypeModel();
                type.setTypeId(jType.getInt("eventTypeId"));
                type.setTypeName(jType.getString("eventTypeName"));
                type.setTimesPlayed(jType.getInt("timesPlayed"));
                typeList.add(type);
            }
            member.setTypesPlayed(typeList);

            EventModel event = new EventModel();
            EventTypeModel favType = new EventTypeModel();
            if (jResponse.isNull("favoriteEvent")){
                event.setEventId(0);
            } else {
                JSONObject jFavorite = jResponse.getJSONObject("favoriteEvent");
                event.setTimesPlayed(jFavorite.getInt("timesPlayed"));
                JSONObject jEvent = jFavorite.getJSONObject("event");
                event.setEventId(jEvent.getInt("id"));
                event.setEventName(jEvent.getString("name"));
                event.setEventIcon(jEvent.getString("icon"));
                JSONObject jFavType = jEvent.getJSONObject("eventType");
                favType.setTypeId(jFavType.getInt("id"));
                favType.setTypeName(jFavType.getString("name"));
                favType.setTypeIcon(jFavType.getString("icon"));
                event.setEventType(favType);
            }
            member.setFavoriteEvent(event);

            return NO_ERROR;
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR_JSON;
        }
    }

    private int parseHistory(String response) {
        Log.w(TAG, "GetHistory response: " + response);
        JSONArray jResponse;
        memberList = new ArrayList<>();
        try {
            jResponse = new JSONArray(response);
            for (int i=0;i<jResponse.length();i++){
                JSONObject jEntry = jResponse.getJSONObject(i);
                MemberModel member = new MemberModel();
                member.setMembershipId(jEntry.getString("membership"));
                member.setName(jEntry.getString("name"));
                member.setIconPath(jEntry.getString("icon"));
                member.setLikes(jEntry.getInt("totalLikes"));
                member.setDislikes(jEntry.getInt("totalDislikes"));
                member.setTitle(getString(R.string.default_title));
                EventModel event = new EventModel();
                if (jEntry.isNull("favoriteEvent")){
                    event.setEventId(0);
                } else {
                    event.setEventId(jEntry.getInt("favoriteEvent"));
                }
                member.setFavoriteEvent(event);
                memberList.add(member);
            }
        } catch (JSONException e){
            e.printStackTrace();
            return ERROR_JSON;
        }
        return NO_ERROR;
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
                EventModel event = new EventModel();
                if (jMember.isNull("favoriteEvent")){
                    event.setEventId(0);
                } else {
                    JSONObject jFavorite = jMember.getJSONObject("favoriteEvent");
                    event.setEventId(jFavorite.getInt("id"));
                }
                member.setFavoriteEvent(event);
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

    @SuppressWarnings("unchecked")
    private HttpURLConnection createMemberListRequest(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        ArrayList<EvaluationModel> memberList = (ArrayList<EvaluationModel>) bundle.getSerializable(EVALUATIONS_TAG);
        if (memberList != null){
            EvaluationModel member = new EvaluationModel();
            member.setMembershipId(memberId);
            memberList.add(member);
        }

        JSONArray listJSON = createJSONMemberList(memberList);
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(listJSON.toString());
        writer.flush();

        return urlConnection;
    }

    private HttpURLConnection createExceptionRequest(HttpURLConnection urlConnection, Bundle bundle) throws IOException, JSONException {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject gameJSON = createExceptionJSON(bundle.getString(CLASS_TAG), bundle.getString(EXCEPTION_TAG));
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(gameJSON.toString());
        writer.flush();

        return urlConnection;
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
        urlConnection.setRequestProperty(CLAN_HEADER, String.valueOf(clanId));
        urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
        urlConnection.setRequestMethod(postMethod);
        return urlConnection;
    }

    private JSONObject createExceptionJSON(String className, String exception) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("exception", exception);
        json.put("class", className);
        Log.w(TAG, "ExceptionJSON: " + json.toString());
        return json;
    }

    private JSONArray createJSONMemberList(ArrayList<EvaluationModel> memberList) {
        JSONArray json = new JSONArray();
        for (int i=0;i<memberList.size();i++){
            json.put(memberList.get(i).getMembershipId());
        }
        Log.w(TAG, "jsonMemberList: " + json.toString());
        return json;
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
