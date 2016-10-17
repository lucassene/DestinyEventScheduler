package com.app.the.bunker.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.data.EventTable;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.models.EvaluationModel;
import com.app.the.bunker.models.EventModel;
import com.app.the.bunker.models.EventTypeModel;
import com.app.the.bunker.models.GameModel;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.models.NoticeModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.CipherUtils;
import com.app.the.bunker.utils.NetworkUtils;
import com.app.the.bunker.utils.StringUtils;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;

public class ServerService extends IntentService {

    private static final String TAG = "ServerService";

    //private static final String SERVER_BASE_URL = "https://destiny-scheduler.herokuapp.com/api/";
    private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/api/";
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
    private static final String NOTICE_ENDPOINT = "notice";
    private static final String EVENTS_ENDPOINT = "events";
    private static final String LOGIN_URL = "https://destiny-scheduler.herokuapp.com/login";

    private static final String STATUS_PARAM = "status=";
    private static final String JOINED_PARAM = "joined=";
    private static final String INITIAL_PARAM = "?initialId=";

    private static final String MEMBER_HEADER = "membership";
    private static final String PLATFORM_HEADER = "platform";
    private static final String CLAN_HEADER = "clanId";
    private static final String TIMEZONE_HEADER = "zoneid";
    private static final String AUTH_HEADER = "Authorization";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String DELETE_METHOD = "DELETE";

    public static final String REQUEST_TAG = "request";
    public static final String ERROR_TAG = "error";
    public static final String MEMBER_TAG = "memberId";
    public static final String PROFILE_TAG = "profile";
    public static final String PLATFORM_TAG = "platformId";
    public static final String RECEIVER_TAG = "receiver";
    public static final String INT_TAG = "intData";
    public static final String GAME_TAG = "gameList";
    public static final String GAMEID_TAG = "gameId";
    public static final String ENTRY_TAG = "entries";
    public static final String EVALUATIONS_TAG = "evaluations";
    public static final String CLAN_TAG = "clanId";
    public static final String CLASS_TAG = "class";
    public static final String EXCEPTION_TAG = "exception";
    public static final String DEVICE_TAG = "deviceName";
    public static final String ANDROID_TAG = "apiNumber";
    public static final String APP_TAG = "appVersion";
    public static final String NOTICE_TAG = "notice";
    public static final String URL_TAG = "firstUrl";
    public static final String BUNDLE_TAG = "firstBundle";

    public static final int STATUS_RUNNING = 200;
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
    public static final int TYPE_NOTICE = 16;
    public static final int TYPE_NEW_EVENTS = 17;
    public static final int TYPE_LOGIN = 18;
    public static final int TYPE_DONE = 19;
    public static final int TYPE_SYNC_SCHEDULED = 20;

    public static final int NO_ERROR = 0;
    public static final int ERROR_INCORRECT_REQUEST = 10;
    public static final int ERROR_NO_CONNECTION = 20;
    public static final int ERROR_HTTP_REQUEST = 30;
    public static final int ERROR_RESPONSE_CODE = 40;
    public static final int ERROR_NULL_RESPONSE = 50;
    public static final int ERROR_INCORRECT_RESPONSE = 60;
    public static final int ERROR_JSON = 70;
    public static final int ERROR_INCORRECT_GAMEID = 80;
    public static final int ERROR_NO_EVENT = 90;
    public static final int ERROR_INSERT = 100;

    public static final String RUNNING_SERVICE = "serverRunning";

    private int gameId;
    private String clanId;
    private String memberId;
    private int platformId;
    private ArrayList<GameModel> gameList;
    private ArrayList<MemberModel> memberList;
    private MemberModel member;
    private int type;
    private NoticeModel notice;
    private boolean hasTriedOnce = false;

    public ServerService() {
        super(ServerService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, true);
        editor.apply();
        Log.w(TAG, "Service running!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
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
                bundle.putSerializable(GAME_TAG, intent.getSerializableExtra(GAME_TAG));
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
                gameId = intent.getIntExtra(GAMEID_TAG, -1);
                if (gameId != -1){
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
                url = SERVER_BASE_URL + GAME_ENDPOINT + HISTORY_ENDPOINT;
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
                gameId = intent.getIntExtra(GAMEID_TAG, -1);
                if (gameId != -1){
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
                    bundle.putInt(ANDROID_TAG, intent.getIntExtra(ANDROID_TAG, 0));
                    bundle.putInt(APP_TAG, intent.getIntExtra(APP_TAG,0));
                    bundle.putString(DEVICE_TAG, intent.getStringExtra(DEVICE_TAG));
                    requestServer(receiver, type, url, bundle);
                }
                break;
            case TYPE_NOTICE:
                url = SERVER_BASE_URL + NOTICE_ENDPOINT;
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR){
                    sendError(receiver, error);
                } else sendNotice(receiver, notice);
                break;
            case TYPE_NEW_EVENTS:
                url = SERVER_BASE_URL + EVENTS_ENDPOINT + INITIAL_PARAM + getDBEventsCount();
                error = requestServer(receiver, type, url, null);
                if (error != NO_ERROR){
                    sendError(receiver, error);
                } else sendStatus(receiver, STATUS_FINISHED);
                break;
        }
        this.stopSelf();

    }

    private void sendStatus(ResultReceiver receiver, int statusRunning) {
        Bundle bundle = new Bundle();
        bundle.putInt(REQUEST_TAG, type);
        receiver.send(statusRunning, bundle);
    }

    private void sendNotice(ResultReceiver receiver, NoticeModel notice) {
        Bundle bundle = new Bundle();
        bundle.putInt(REQUEST_TAG, type);
        bundle.putSerializable(NOTICE_TAG, notice);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private void sendMemberData(ResultReceiver receiver, MemberModel member) {
        Bundle bundle = new Bundle();
        bundle.putInt(REQUEST_TAG, type);
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
        bundle.putInt(REQUEST_TAG, type);
        bundle.putInt(GAMEID_TAG, gameId);
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
        if (receiver != null) { sendStatus(receiver, STATUS_RUNNING); }
        try {
            if (NetworkUtils.checkConnection(getApplicationContext())){
                URL myURL;
                if (type == TYPE_LOGIN) {
                    myURL = new URL(LOGIN_URL);
                } else myURL = new URL(url);
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
                    case TYPE_NEW_EVENTS:
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
                        //return ERROR_HTTP_REQUEST;
                        break;
                    case TYPE_CLAN_MEMBERS:
                        urlConnection = createMemberListRequest(urlConnection, bundle);
                        break;
                    case TYPE_NOTICE:
                        urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);
                        break;
                    case TYPE_LOGIN:
                        urlConnection = getLoginHeaders(urlConnection);
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
                                    error = parseGames(receiver, response);
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
                                        intent.putExtra(LocalService.RECEIVER_HEADER, receiver);
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
                                case TYPE_NOTICE:
                                    if (!StringUtils.isEmptyOrWhiteSpaces(response)){
                                        error = parseNotice(response);
                                        return error;
                                    } else return NO_ERROR;
                                case TYPE_NEW_EVENTS:
                                    error = parseNewEvents(response);
                                    return error;
                                case TYPE_LOGIN:
                                    String authKey = urlConnection.getHeaderField(AUTH_HEADER);
                                    //Log.w(TAG, "authKey: " + authKey);
                                    CipherUtils cipher = new CipherUtils();
                                    SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    String encrypted = cipher.encrypt(authKey);
                                    editor.putString(Constants.KEY_PREF, encrypted);
                                    editor.apply();
                                    String userName = prefs.getString(Constants.USERNAME_PREF, "");
                                    AccountManager manager = AccountManager.get(this);
                                    Account acc = new Account(userName, Constants.ACC_TYPE);
                                    manager.setAuthToken(acc, Constants.ACC_TYPE, encrypted);
                                    error = requestServer(receiver, bundle.getInt(REQUEST_TAG),bundle.getString(URL_TAG),bundle.getBundle(BUNDLE_TAG));
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
                    String s = convertInputStreamToString(urlConnection.getErrorStream());
                    Log.w(TAG, "error: " + s);
                    if (statusCode == 500 || statusCode == 403){
                        if (hasTriedOnce) return ERROR_RESPONSE_CODE;
                        hasTriedOnce = true;
                        switch (type){
                            case TYPE_CREATE_GAME:
                                return ERROR_NO_EVENT;
                            default:
                                Log.w(TAG, "authKey expired or null. Acquiring a new one...");
                                Bundle loginBundle = new Bundle();
                                loginBundle.putInt(REQUEST_TAG, type);
                                loginBundle.putString(URL_TAG, url);
                                loginBundle.putBundle(BUNDLE_TAG, bundle);
                                requestServer(receiver, TYPE_LOGIN, LOGIN_URL, loginBundle);
                                break;
                        }
                    }
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

    private int parseNewEvents(String response) {
        Log.w(TAG, "getNewEvents response: " + response);
        int err = NO_ERROR;
        int maxType = getDBTypesCount();
        ArrayList<EventTypeModel> typeList = new ArrayList<>();
        try{
            JSONArray jArray = new JSONArray(response);
            for (int i=0;i<jArray.length();i++){
                JSONObject jEvent = jArray.getJSONObject(i);
                EventModel e = new EventModel();
                EventTypeModel eT = new EventTypeModel();
                e.setEventId(jEvent.getInt("id"));
                e.setEventIcon(jEvent.getString("icon"));
                e.setMinLight(jEvent.getInt("minLight"));
                e.setMaxGuardians(jEvent.getInt("maxGuardians"));

                JSONObject jType = jEvent.getJSONObject("eventType");
                eT.setTypeId(jType.getInt("id"));
                eT.setTypeIcon(jType.getString("icon"));
                eT.setEnName(jType.getString("en"));
                eT.setPtName(jType.getString("pt"));
                eT.setEsName(jType.getString("es"));
                if (eT.getTypeId() > maxType){
                    typeList.add(eT);
                }

                e.setEventType(eT);
                e.setEnName(jEvent.getString("en"));
                e.setPtName(jEvent.getString("pt"));
                e.setEsName(jEvent.getString("es"));
                err = insertEvent(e);
            }
            if (typeList.size() >0){
                Collections.sort(typeList, new TypeComparator());
                for (int i=0;i<typeList.size();i++){
                    insertType(typeList.get(i));
                }
            }
            return err;
        } catch (JSONException e){
            e.printStackTrace();
            return ERROR_JSON;
        }
    }

    private int insertEvent(EventModel e) {
        try{
            Log.w(TAG, "Inserting event (" + e.getEnName() + ")");
            ContentValues values = new ContentValues();
            values.put(EventTable.COLUMN_ID,e.getEventId());
            values.put(EventTable.COLUMN_EN, e.getEnName());
            values.put(EventTable.COLUMN_PT, e.getPtName());
            values.put(EventTable.COLUMN_ES, e.getEsName());
            values.put(EventTable.COLUMN_ICON, e.getEventIcon());
            values.put(EventTable.COLUMN_TYPE, e.getEventType().getTypeId());
            values.put(EventTable.COLUMN_LIGHT, e.getMinLight());
            values.put(EventTable.COLUMN_GUARDIANS, e.getMaxGuardians());
            Uri uri = getContentResolver().insert(DataProvider.EVENT_URI,values);
            if (uri != null){
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
                editor.putInt(Constants.EVENT_PREF, getDBEventsCount());
                editor.apply();
            }
            values.clear();
            return NO_ERROR;
        }catch (Exception exc){
            exc.printStackTrace();
            return ERROR_INSERT;
        }
    }

    private void insertType(EventTypeModel eventType) {
        Log.w(TAG, "Inserting eventType (" + eventType.getEnName() + ")");
        ContentValues values = new ContentValues();
        values.put(EventTypeTable.COLUMN_ID, eventType.getTypeId());
        values.put(EventTypeTable.COLUMN_EN, eventType.getEnName());
        values.put(EventTypeTable.COLUMN_PT, eventType.getPtName());
        values.put(EventTypeTable.COLUMN_ES, eventType.getEsName());
        values.put(EventTypeTable.COLUMN_ICON, eventType.getTypeIcon());
        Uri uri = getContentResolver().insert(DataProvider.EVENT_TYPE_URI, values);
        if (uri != null){
            SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
            editor.putInt(Constants.TYPE_PREF,getDBTypesCount());
            editor.apply();
        }
        values.clear();
    }

    private int getDBEventsCount() {
        Cursor cursor = null;
        try{
            cursor = getContentResolver().query(DataProvider.EVENT_URI,EventTable.ALL_COLUMNS,null,null,null);
            if (cursor != null){
                return cursor.getCount();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 999;
    }

    private int getDBTypesCount() {
        Cursor cursor = null;
        try{
            cursor = getContentResolver().query(DataProvider.EVENT_TYPE_URI,EventTypeTable.ALL_COLUMNS,null,null,null);
            if (cursor != null){
                return cursor.getCount();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 999;
    }

    private int parseNotice(String response) {
        Log.w(TAG, "getNotice response: " + response);
        notice = new NoticeModel();
        try {
            JSONObject jNotice = new JSONObject(response);
            notice.setId(jNotice.getInt("id"));
            notice.setUrl(jNotice.getString("url"));
            notice.setMessage(jNotice.getString("message"));
            notice.setVersionCode(jNotice.getInt("versionCode"));
            notice.setForceUpdate(jNotice.getBoolean("forceUpdate"));
            return NO_ERROR;
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR_JSON;
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
                if (jMember.isNull("memberTitle")){
                    member.setTitle(getString(R.string.default_title));
                } else {
                    JSONObject jTitle = jMember.getJSONObject("memberTitle");
                    member.setTitle(jTitle.getString(StringUtils.getLanguageString(this)));
                }
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
                type.setTypeName(jType.getString(StringUtils.getLanguageString(this)));
                type.setTimesPlayed(jType.getInt("timesPlayed"));
                typeList.add(type);
            }
            member.setTypesPlayed(typeList);
            if (jMember.isNull("memberTitle")){
                member.setTitle(getString(R.string.default_title));
            } else {
                JSONObject jTitle = jMember.getJSONObject("memberTitle");
                member.setTitle(jTitle.getString(StringUtils.getLanguageString(this)));
            }

            EventModel event = new EventModel();
            EventTypeModel favType = new EventTypeModel();
            if (jResponse.isNull("favoriteEvent")){
                event.setEventId(0);
            } else {
                JSONObject jFavorite = jResponse.getJSONObject("favoriteEvent");
                event.setTimesPlayed(jFavorite.getInt("timesPlayed"));
                JSONObject jEvent = jFavorite.getJSONObject("event");
                event.setEventId(jEvent.getInt("id"));
                event.setEventName(jEvent.getString(StringUtils.getLanguageString(this)));
                event.setEventIcon(jEvent.getString("icon"));
                JSONObject jFavType = jEvent.getJSONObject("eventType");
                favType.setTypeId(jFavType.getInt("id"));
                favType.setTypeName(jFavType.getString(StringUtils.getLanguageString(this)));
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
                if (jEntry.isNull("memberTitle")){
                    member.setTitle(getString(R.string.default_title));
                } else {
                    JSONObject jTitle = jEntry.getJSONObject("memberTitle");
                    member.setTitle(jTitle.getString(StringUtils.getLanguageString(this)));
                }
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
                if (jMember.isNull("memberTitle")){
                    member.setTitle(getString(R.string.default_title));
                } else {
                    JSONObject jTitle = jMember.getJSONObject("memberTitle");
                    member.setTitle(jTitle.getString(StringUtils.getLanguageString(this)));
                }
                memberList.add(member);
            }
        } catch (JSONException e){
            e.printStackTrace();
            return ERROR_JSON;
        }
        return NO_ERROR;
    }

    private int parseGames(ResultReceiver receiver, String response){
        Log.w(TAG, "getGames response: " + response);
        JSONArray jResponse;
        gameList = new ArrayList<>();
        int maxEvent = getDBEventsCount();
        boolean hasNewEvents = false;
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
                if (game.getEventId() > maxEvent){hasNewEvents = true;}
                game.setEventName(jEvent.getString(StringUtils.getLanguageString(this)));
                game.setEventIcon(jEvent.getString("icon"));
                game.setMaxGuardians(jEvent.getInt("maxGuardians"));
                JSONObject jType = jEvent.getJSONObject("eventType");
                game.setTypeId(jType.getInt("id"));
                game.setTypeName(jType.getString(StringUtils.getLanguageString(this)));
                game.setTypeIcon(jType.getString("icon"));
                game.setTime(jGame.getString("time"));
                game.setMinLight(jGame.getInt("light"));
                game.setInscriptions(jGame.getInt("inscriptions"));
                game.setStatus(jGame.getInt("status"));
                if (!jGame.isNull("comment")) { game.setComment(jGame.getString("comment")); }
                if (!jGame.isNull("reserved")) { game.setReserved(jGame.getInt("reserved")); }
                game.setJoined(getBoolean(jGame.getString("joined")));
                game.setEvaluated(getBoolean(jGame.getString("evaluated")));
                gameList.add(game);
            }
            if (hasNewEvents){
                Log.w(TAG, "New event found. Inserting...");
                String url = SERVER_BASE_URL + EVENTS_ENDPOINT + INITIAL_PARAM + maxEvent;
                requestServer(receiver, TYPE_NEW_EVENTS, url, null);
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
    private HttpURLConnection createMemberListRequest(HttpURLConnection urlConnection, Bundle bundle) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
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

    private HttpURLConnection createExceptionRequest(HttpURLConnection urlConnection, Bundle bundle) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject gameJSON = createExceptionJSON(bundle);
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(gameJSON.toString());
        writer.flush();

        return urlConnection;
    }

    private HttpURLConnection createGameRequest(HttpURLConnection urlConnection, Bundle bundle) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject gameJSON = createCreateGameJSON(bundle);
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(gameJSON.toString());
        writer.flush();

        return urlConnection;
    }

    private HttpURLConnection createValidateRequest(HttpURLConnection urlConnection, Bundle bundle) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
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

    private HttpURLConnection createEvaluationRequest(HttpURLConnection urlConnection, Bundle bundle) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
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

    private HttpURLConnection getLoginHeaders(HttpURLConnection urlConnection) throws IOException, JSONException {
        urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
        urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
        urlConnection.setRequestProperty(CLAN_HEADER, String.valueOf(clanId));
        urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
        urlConnection.setRequestMethod(POST_METHOD);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject jLogin = createLoginJSON();
        if (jLogin != null){
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(jLogin.toString());
            writer.flush();
        } else return null;

        return urlConnection;
    }

    private HttpURLConnection getDefaultHeaders(HttpURLConnection urlConnection, String postMethod) throws Exception {
        urlConnection.setRequestProperty(MEMBER_HEADER, memberId);
        urlConnection.setRequestProperty(PLATFORM_HEADER, String.valueOf(platformId));
        urlConnection.setRequestProperty(CLAN_HEADER, String.valueOf(clanId));
        urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
        String authKey = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).getString(Constants.KEY_PREF,"");
        try{
            if (!authKey.isEmpty()){
                CipherUtils cipher = new CipherUtils();
                urlConnection.setRequestProperty(AUTH_HEADER, cipher.decrypt(authKey));
            } else urlConnection.setRequestProperty(AUTH_HEADER, authKey);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        urlConnection.setRequestMethod(postMethod);
        return urlConnection;
    }

    private JSONObject createExceptionJSON(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("exception", bundle.getString(EXCEPTION_TAG));
        json.put("class", bundle.getString(CLASS_TAG));
        json.put("apiNumber", bundle.getInt(ANDROID_TAG,0));
        json.put("versionCode", bundle.getInt(APP_TAG, 0));
        json.put("deviceName", bundle.getString(DEVICE_TAG));
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

    private JSONObject createCreateGameJSON(Bundle bundle) throws JSONException {
        GameModel game = (GameModel) bundle.getSerializable(GAME_TAG);
        JSONObject json = new JSONObject();
        if (game != null){
            JSONObject jEvent = new JSONObject();
            jEvent.put("id", game.getEventId());
            json.put("event",jEvent);
            json.put("time", game.getTime());
            json.put("light", game.getMinLight());
            json.put("status",0);
            json.put("comment", game.getComment());
            json.put("reserved", game.getReserved());

            JSONArray jArray = new JSONArray();
            if (game.getEntryList().size()>0){
                for (int i=0;i<game.getEntryList().size();i++){
                    JSONObject jEntry = new JSONObject();
                    JSONObject jMember = new JSONObject();
                    jMember.put("membership", game.getEntryList().get(i).getMembershipId());
                    jEntry.put("member", jMember);
                    jArray.put(jEntry);
                }
                json.put("entries", jArray);
            }

            Log.w(TAG, "GameJSON: " + json.toString());
        }
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

    private JSONObject createLoginJSON() throws JSONException {
        JSONObject jLogin = new JSONObject();
        jLogin.put("username", memberId);
        String pass;
        CipherUtils cUtils = new CipherUtils();
        try{
            pass = cUtils.encrypt(memberId);
            Log.w(TAG, "password: " + pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        jLogin.put("password", pass);
        return jLogin;
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
        result = Html.fromHtml(result).toString();
        return result;
    }

    private class TypeComparator implements java.util.Comparator<EventTypeModel> {
        @Override
        public int compare(EventTypeModel et1, EventTypeModel et2) {
            return et2.getTypeId() - et1.getTypeId();
        }
    }

}
