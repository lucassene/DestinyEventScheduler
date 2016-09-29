package com.app.the.bunker.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;

import com.app.the.bunker.BuildConfig;
import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.ClanTable;
import com.app.the.bunker.data.EventTable;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.data.LoggedUserTable;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.data.SavedImagesTable;
import com.app.the.bunker.models.EventModel;
import com.app.the.bunker.models.EventTypeModel;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.CipherUtils;
import com.app.the.bunker.utils.ImageUtils;
import com.app.the.bunker.utils.NetworkUtils;

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
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import static com.app.the.bunker.services.ServerService.ERROR_JSON;

public class BungieService extends IntentService {

    private static final String BASE_URL = "https://www.bungie.net/Platform/";
    private static final String BASE_IMAGE_URL = "http://www.bungie.net";

    private static final String SERVER_BASE_URL = "https://destiny-scheduler.herokuapp.com/";
    //private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    private static final String API_SERVER_ENDPOINT = "api/";
    private static final String LOGIN_ENDPOINT = "login";
    private static final String CLAN_ENDPOINT = "clan";
    private static final String MEMBERS_ENDPOINT = "members";
    private static final String MEMBERLIST_ENDPOINT = "member/list";
    private static final String EVENTS_ENDPOINT = "events";
    private static final String INITIAL_PARAM = "?initialId=";

    private static final String SERVER_MEMBER_HEADER = "membership";
    private static final String SERVER_PLATFORM_HEADER = "platform";
    private static final String AUTH_HEADER = "Authorization";

    private static final String USER_PREFIX = "User/";
    private static final String GROUP_PREFIX = "Group/";

    public static final String GET_CURRENT_ACCOUNT = "GetCurrentBungieAccount/";

    public static final int TYPE_LOGIN = 100;
    public static final int TYPE_UPDATE_CLAN = 110;
    public static final int TYPE_VERIFY_MEMBER = 120;
    public static final int TYPE_RELOGIN = 130;

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 110;
    public static final int STATUS_ERROR = 1404;
    public static final int STATUS_DOCS = 11;
    public static final int STATUS_VERIFY = 12;
    public static final int STATUS_FRIENDS = 14;
    public static final int STATUS_PARTY = 15;
    public static final int STATUS_EVENTS = 16;

    public static final String COOKIE_EXTRA = "cookies";
    public static final String REQUEST_EXTRA = "request";
    public static final String RECEIVER_EXTRA = "receiver";
    public static final String XCSRF_EXTRA = "x-csrf";
    public static final String PLATFORM_EXTRA = "platform";
    public static final String MEMBERSHIP_EXTRA = "membershipId";
    public static final String CLAN_EXTRA = "clanId";

    private static final String KEY_HEADER = "x-api-key";
    private static final String COOKIE_HEADER = "cookie";
    private static final String XCSRF_HEADER = "x-csrf";
    private static final String TIMEZONE_HEADER = "zoneid";

    public static final String ERROR_TAG = "error";
    public static final int NO_ERROR = 0;
    public static final int ERROR_INCORRECT_REQUEST = 99;
    public static final int ERROR_NO_CONNECTION = 100;
    public static final int ERROR_HTTP_REQUEST = 110;
    public static final int ERROR_NO_ICON = 120;
    public static final int ERROR_CURRENT_USER = 130;
    public static final int ERROR_RESPONSE_CODE = 140;
    public static final int ERROR_NO_CLAN = 150;
    public static final int ERROR_MEMBERS_OF_CLAN = 160;
    public static final int ERROR_CLAN_MEMBER = 170;
    public static final int ERROR_AUTH = 180;
    public static final int ERROR_SERVER = 190;

    private static final String DEFAULT_ICON = "/img/profile/avatars/Destiny26.jpg";

    private int error = 0;

    private String displayName = "";
    private String membershipId = "";
    private int platformId;

    private String clanId = "";
    private String clanName = "";
    private String motto = "";
    private String clanIcon = "";
    private String clanBanner = "";

    private ArrayList<MemberModel> membersModelList;
    private ArrayList<String> iconsList;

    private static final String TAG = "BungieService";

    private String cookie;
    private String xcsrf;

    private String getCurrentBungieAccountResponse;

    private ArrayList<String> actualMemberList;

    public static final String RUNNING_SERVICE = "bungieRunning";

    public BungieService() {
        super(BungieService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int request = intent.getIntExtra(REQUEST_EXTRA, 0);
        if (intent.hasExtra(COOKIE_EXTRA)) cookie = intent.getStringExtra(COOKIE_EXTRA);
        if (intent.hasExtra(XCSRF_EXTRA)) xcsrf = intent.getStringExtra(XCSRF_EXTRA);
        if (intent.hasExtra(PLATFORM_EXTRA)) platformId = intent.getIntExtra(PLATFORM_EXTRA, 0);
        if (intent.hasExtra(MEMBERSHIP_EXTRA)) membershipId = intent.getStringExtra(MEMBERSHIP_EXTRA);

        membersModelList = new ArrayList<>();
        iconsList = new ArrayList<>();

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_EXTRA);

        switch (request) {
            case TYPE_LOGIN:
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                error = getBungieAccount(receiver);
                Log.w(TAG, "getBungieAccount error: " + error);
                if (error != NO_ERROR){
                    sendError(receiver);
                } else {
                    error = parseCurrentBungieAccount(receiver);
                    Log.w(TAG, "parseCurrentBungieAccount error: " + error);
                    if (error != NO_ERROR){
                        sendError(receiver);
                    } else {
                        error = getMembersOfClan(receiver);
                        getIconList(true);
                        Log.w(TAG, "getMembersOfClan error: " + error);
                        if (error != NO_ERROR){
                            sendError(receiver);
                        } else {
                            insertClan();
                            downloadImages();
                            Log.w(TAG, "Inserting clanMembers data. MemberList size: " + membersModelList.size());
                            for (int i=0;i<membersModelList.size();i++){
                                insertClanMember(membersModelList.get(i));
                            }
                            insertLoggedUser();
                            getNewEvents(receiver);
                            receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                        }
                    }
                }
                break;
            case TYPE_VERIFY_MEMBER:
            case TYPE_RELOGIN:
                cookie = intent.getStringExtra(COOKIE_EXTRA);
                xcsrf = intent.getStringExtra(XCSRF_EXTRA);
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                error = getBungieAccount(receiver);
                if (error != NO_ERROR){
                    sendError(receiver);
                } else {
                    receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                }
                break;
            case TYPE_UPDATE_CLAN:
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                clanId = intent.getStringExtra("clanId");
                actualMemberList = intent.getStringArrayListExtra("memberList");
                membershipId = intent.getStringExtra("userMembership");
                platformId = intent.getIntExtra("platformId",0);
                error = getMembersOfClan(receiver);
                if (error != NO_ERROR){
                    sendError(receiver);
                } else {
                    error = updateClan(receiver);
                    if (error != NO_ERROR){
                        sendError(receiver);
                    } else {
                        receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                    }
                }
                break;
            default:
                error = ERROR_INCORRECT_REQUEST;
                sendError(receiver);
        }

        this.stopSelf();

    }

    private int getDBTypesCount() {
        Cursor cursor = null;
        try{
            cursor = getContentResolver().query(DataProvider.EVENT_TYPE_URI, EventTypeTable.ALL_COLUMNS,null,null,null);
            if (cursor != null){
                return cursor.getCount();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 999;
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

    private int updateClan(ResultReceiver receiver) {

        String myURL = BASE_URL + GROUP_PREFIX + clanId + "/";
        Log.w(TAG, myURL);
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {

            if (NetworkUtils.checkConnection(getApplicationContext())) {

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty(KEY_HEADER, BuildConfig.API_KEY);
                urlConnection.setRequestMethod(GET_METHOD);
                //urlConnection.setRequestProperty("Content-Type", "application/json;charset=ISO-8859-1");

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    //Log.w(TAG, "getGroup response: " + response );

                    if (response != null){
                        try {
                            JSONObject jO = new JSONObject(response);
                            Log.w(TAG, "getGroup response: " + jO.toString());
                            JSONObject jResponse = jO.getJSONObject("Response");
                            JSONObject jDetail = jResponse.getJSONObject("detail");
                            clanId = jDetail.getString("groupId");
                            clanName = jDetail.getString("name");
                            motto = jDetail.getString("motto");
                            //motto = URLEncoder.encode(motto, "UTF-8");
                            clanBanner = jDetail.getString("bannerPath");
                            Log.w(TAG, "clanDesc: " + motto);
                            clanIcon = jDetail.getString("avatarPath");

                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(DataProvider.CLAN_URI,ClanTable.ALL_COLUMNS,null, null, null);
                                if (cursor != null && cursor.moveToFirst()){
                                    String actualName = cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_NAME));
                                    String actualMotto = cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_DESC));
                                    String actualBanner = cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_BACKGROUND));
                                    String actualIcon = cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_ID));

                                    ContentValues values = new ContentValues();
                                    if (!clanName.equals(actualName)){
                                        values.put(ClanTable.COLUMN_NAME, clanName);
                                    }
                                    if (!clanBanner.equals(actualBanner)){
                                        String imageName = clanBanner.substring(clanBanner.lastIndexOf("/")+1, clanBanner.length());
                                        values.put(ClanTable.COLUMN_BACKGROUND, imageName);
                                        ImageUtils.downloadImage(getApplicationContext(), BASE_IMAGE_URL + clanBanner);
                                    }
                                    if (!clanIcon.equals(actualIcon)){
                                        String imageName = clanIcon.substring(clanIcon.lastIndexOf("/")+1, clanIcon.length());
                                        values.put(ClanTable.COLUMN_ICON, imageName);
                                        ImageUtils.downloadImage(getApplicationContext(), BASE_IMAGE_URL + clanIcon);
                                    }
                                    if (!motto.equals(actualMotto)){
                                        values.put(ClanTable.COLUMN_DESC, motto);
                                    }
                                    getContentResolver().update(DataProvider.CLAN_URI,values,ClanTable.COLUMN_BUNGIE_ID + "=" + clanId,null);
                                    values.clear();
                                    return NO_ERROR;
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            } finally {
                                if (cursor != null) cursor.close();
                            }

                            return 0;
                        } catch (JSONException jE){
                            Log.w(TAG, "Erro no JSON de GetGroup");
                            jE.printStackTrace();
                            return ERROR_RESPONSE_CODE;
                        }
                    } else {
                        Log.w(TAG, "Response is null");
                        return ERROR_RESPONSE_CODE;
                    }
                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200 (GetGroup)");
                    return ERROR_RESPONSE_CODE;
                }
            } else {
                Log.w(TAG, "Sem conexão com a internet");
                return ERROR_NO_CONNECTION;
            }
        } catch (Exception e){
            Log.w(TAG, "Problema no HTTP Request (getCurrentBungieAccount)");
            e.printStackTrace();
            return ERROR_HTTP_REQUEST;
        }
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

    private void sendError(ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        Log.w(TAG,"Algum problema ocorreu, avisando o usuário");
        bundle.clear();
        bundle.putInt(ERROR_TAG, error);
        receiver.send(STATUS_ERROR, bundle);
    }

    private int getBungieAccount(ResultReceiver receiver){

        String myURL = BASE_URL + USER_PREFIX + GET_CURRENT_ACCOUNT;
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty(KEY_HEADER, BuildConfig.API_KEY);
                urlConnection.setRequestProperty(XCSRF_HEADER, xcsrf);
                urlConnection.setRequestProperty(COOKIE_HEADER, cookie);
                urlConnection.setRequestMethod(GET_METHOD);

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    getCurrentBungieAccountResponse = convertInputStreamToString(inputStream);
                    return NO_ERROR;
                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200 (GetCurrentBungieAccount");
                    return ERROR_RESPONSE_CODE;
                }

            } else {
                Log.w(TAG, "Sem conexão com a internet");
                return ERROR_NO_CONNECTION;
            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getCurrentBungieAccount)");
            e.printStackTrace();
            return ERROR_HTTP_REQUEST;
        }

    }

    private int parseCurrentBungieAccount(ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        try {
            JSONObject jObject = new JSONObject(getCurrentBungieAccountResponse);
            //Log.w(TAG, getCurrentBungieAccountResponse);

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");

            if (jDestinyAccounts.length() > 0) {

                JSONObject obj = jDestinyAccounts.getJSONObject(0);
                JSONObject userInfo = obj.getJSONObject("userInfo");

                membershipId = userInfo.getString("membershipId");
                displayName = userInfo.getString("displayName");

                bundle.clear();
                bundle.putString("bungieId", membershipId);
                bundle.putString("userName", displayName);
                bundle.putInt("platform", platformId);
                receiver.send(STATUS_DOCS, bundle);

                try {
                    JSONArray jClans = jResponse.getJSONArray("clans");
                    JSONObject clanObj = jClans.getJSONObject(0);
                    clanId = clanObj.getString("groupId");
                    Log.w(TAG,"Clan ID: " + clanId);

                    JSONObject jRelatedGroups = jResponse.getJSONObject("relatedGroups");
                    JSONObject jGroup = jRelatedGroups.getJSONObject(clanId);

                    clanName = jGroup.getString("name");
                    motto = jGroup.getString("motto");
                    clanBanner = jGroup.getString("bannerPath");
                    clanIcon = jGroup.getString("avatarPath");

                    int err = checkInServer(receiver);
                    if (err != NO_ERROR) { return err; }

                    bundle.clear();
                    bundle.putString("clanId", clanId);
                    receiver.send(STATUS_FRIENDS, bundle);
                    return NO_ERROR;
                } catch (JSONException e){
                    Log.w(TAG, "Erro no JSON do getGroup");
                    e.printStackTrace();
                    return ERROR_NO_CLAN;
                }

            } else {
                Log.w(TAG, "destinyAccount tag not found");
                return ERROR_CURRENT_USER;
            }
        } catch (JSONException e) {

            try {
                JSONObject jObject = new JSONObject(getCurrentBungieAccountResponse);
                String errorCode = jObject.getString("ErrorCode");
                if (errorCode.equals("99")){
                    return ERROR_AUTH;
                } else return ERROR_CURRENT_USER;

            } catch (JSONException jE){
                Log.w(TAG, "Erro no JSON de getCurrentBungieAccount");
                jE.printStackTrace();
                return ERROR_CURRENT_USER;
            }

        }

    }

    private HttpURLConnection createLoginRequest(HttpURLConnection urlConnection) throws Exception {
        urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
        if (urlConnection == null) return null;
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

    private JSONObject createLoginJSON() throws JSONException {
        JSONObject jLogin = new JSONObject();
        jLogin.put("username", membershipId);
        String pass;
        CipherUtils cUtils = new CipherUtils();
        try{
            pass = cUtils.encrypt(membershipId);
            Log.w(TAG, "password: " + pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        jLogin.put("password", pass);
        return jLogin;
    }

    private HttpURLConnection getDefaultHeaders(HttpURLConnection urlConnection, String method) throws Exception {
        urlConnection.setRequestProperty(SERVER_MEMBER_HEADER, membershipId);
        urlConnection.setRequestProperty(CLAN_EXTRA, clanId);
        urlConnection.setRequestProperty(SERVER_PLATFORM_HEADER, String.valueOf(platformId));
        urlConnection.setRequestProperty(TIMEZONE_HEADER, TimeZone.getDefault().getID());
        String authKey = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).getString(DrawerActivity.KEY_PREF,"");
        try{
            if (!authKey.isEmpty()){
                CipherUtils cipher = new CipherUtils();
                urlConnection.setRequestProperty(AUTH_HEADER, cipher.decrypt(authKey));
            } else urlConnection.setRequestProperty(AUTH_HEADER, authKey);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        urlConnection.setRequestMethod(method);
        return urlConnection;
    }

    private int checkInServer(ResultReceiver receiver) {
        String myURL = SERVER_BASE_URL + LOGIN_ENDPOINT;
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        Log.w(TAG, "Login initiaded. Getting authorization...");

        try {

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection = createLoginRequest(urlConnection);
                int statusCode;
                if (urlConnection != null){
                    statusCode = urlConnection.getResponseCode();
                } else return ERROR_SERVER;

                if (statusCode == 200) {
                    String authKey = urlConnection.getHeaderField("Authorization");
                    //Log.w(TAG, "authKey: " + authKey);
                    CipherUtils cipher = new CipherUtils();
                    SharedPreferences.Editor editor = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).edit();
                    editor.putString(DrawerActivity.KEY_PREF, cipher.encrypt(authKey));
                    editor.apply();

                    receiver.send(STATUS_VERIFY, Bundle.EMPTY);
                    return NO_ERROR;
                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200 (" + statusCode + " - Server Login)");
                    return ERROR_SERVER;
                }
            } else {
                Log.w(TAG, "Sem conexão com a internet");
                return ERROR_NO_CONNECTION;
            }
        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (Server Login)");
            e.printStackTrace();
            return ERROR_SERVER;
        }
    }

    private int getMembersOfClan(ResultReceiver receiver){

        String myURL = SERVER_BASE_URL + API_SERVER_ENDPOINT + CLAN_ENDPOINT + "/" + clanId + "/" + MEMBERS_ENDPOINT;

        try{

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);
                int statusCode;
                if (urlConnection != null) {
                    statusCode = urlConnection.getResponseCode();
                } else return ERROR_HTTP_REQUEST;

                if (statusCode == 200){
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    Log.w(TAG, "getMembersOfClan: " + response);

                    if (response != null){
                        ArrayList<String> memberList = new ArrayList<>();
                        JSONArray jArray = new JSONArray(response);
                        memberList.add(membershipId);
                        for (int i=0;i<jArray.length();i++){
                            memberList.add(jArray.getString(i));
                            //Log.w(TAG, "added member: " + jArray.getString(i));
                        }
                        int error;
                        if (actualMemberList != null){
                            memberList.add(membershipId);
                            //Log.w(TAG, "added member: " + membershipId);
                            error = parseNewMembers(receiver, memberList);
                        } else error = parseMembersOfClan(receiver, memberList);
                        if (error == NO_ERROR){
                            receiver.send(STATUS_PARTY, Bundle.EMPTY);
                            return NO_ERROR;
                        } else return error;
                    } else {
                        Log.w(TAG, "Null response from getClanMembers");
                        error = ERROR_RESPONSE_CODE;
                        return ERROR_RESPONSE_CODE;
                    }
                } else {
                    Log.w(TAG, "responseCode different than 200");
                    if (statusCode == 500 || statusCode == 403){
                        int err = checkInServer(receiver);
                        if (err != NO_ERROR) getMembersOfClan(receiver);
                        return err;
                    }
                    Log.w(TAG, "Response Code do JSON diferente de 200 (...clan/" + clanId + "/members)");
                    error = ERROR_RESPONSE_CODE;
                    return ERROR_RESPONSE_CODE;
                }

            } else {
                Log.w(TAG, "Sem conexão com a Internet");
                error = ERROR_NO_CONNECTION;
                return ERROR_NO_CONNECTION;
            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getMembersOfClan)");
            e.printStackTrace();
            error = ERROR_HTTP_REQUEST;
            return ERROR_HTTP_REQUEST;
        }
    }

    private void getNewEvents(ResultReceiver receiver) {
        SharedPreferences.Editor sharedEditor = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).edit();
        sharedEditor.putInt(DrawerActivity.EVENT_PREF, getDBEventsCount());
        sharedEditor.putInt(DrawerActivity.TYPE_PREF, getDBTypesCount());
        sharedEditor.apply();

        String myURL = SERVER_BASE_URL + API_SERVER_ENDPOINT + EVENTS_ENDPOINT + INITIAL_PARAM + getDBEventsCount();
        Log.w(TAG, "New Events URL: " + myURL);
        receiver.send(STATUS_EVENTS, Bundle.EMPTY);

        try{
            URL url = new URL(myURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection = getDefaultHeaders(urlConnection, GET_METHOD);

            int statusCode;
            if (urlConnection != null) {
                statusCode = urlConnection.getResponseCode();
            } else throw new Exception();

            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);

                if (response != null) {
                    parseNewEvents(response);
                }
            } else {
                if (statusCode == 500 || statusCode == 403){
                    int err = checkInServer(receiver);
                    if (err != NO_ERROR) getNewEvents(receiver);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
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

    private int parseNewMembers(ResultReceiver receiver, ArrayList<String> memberList) {

        int error = parseMembersOfClan(receiver, memberList);
        if (error != NO_ERROR){
            return error;
        } else {
            for (int i=0;i<membersModelList.size();i++){
                for (int x=0;x<actualMemberList.size();x++){
                    if (actualMemberList.get(x).equals(membersModelList.get(i).getMembershipId())){
                        membersModelList.get(i).setInsert(false);
                        break;
                    }
                }
                insertClanMember(membersModelList.get(i));
            }

            Cursor iCursor = null;
            ArrayList<String> savedIcons = new ArrayList<>();
            try {
                iCursor = getContentResolver().query(DataProvider.SAVED_IMAGES_URI,SavedImagesTable.ALL_COLUMNS,null,null,null);
                if (iCursor != null && iCursor.moveToFirst()){
                    for (int i=0;i<iCursor.getCount();i++){
                        savedIcons.add(iCursor.getString(iCursor.getColumnIndexOrThrow(SavedImagesTable.COLUMN_PATH)));
                        iCursor.moveToNext();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (iCursor != null) iCursor.close();
            }

            getIconList(false);
            iconsList.removeAll(savedIcons);
            downloadImages();
            return NO_ERROR;
        }

    }

    private int parseMembersOfClan(ResultReceiver receiver, ArrayList<String> memberList) {

        String myURL = SERVER_BASE_URL + API_SERVER_ENDPOINT + MEMBERLIST_ENDPOINT;

        try {
            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection = getDefaultHeaders(urlConnection, POST_METHOD);
                if (urlConnection == null) return ERROR_AUTH;
                urlConnection.setDoOutput(true);
                JSONArray jsonList = createJSONMemberList(memberList);
                urlConnection.setRequestProperty("Accept-Charset", "ISO-8859-1");
                urlConnection.setRequestProperty("Accept-Language", "ISO-8859-1");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=ISO-8859-1");
                urlConnection.setRequestProperty("Accept", "application/json;charset=ISO-8859-1");

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(jsonList.toString());
                writer.flush();

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    if (response != null){
                        Log.w(TAG, "getMemberList response: " + response);
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
                                member.setTitle(jTitle.getString(getLanguageString()));
                            }
                            //Log.w(TAG, "Member Title: " + member.getTitle());
                            member.setInsert(true);
                            membersModelList.add(member);
                        }
                        return NO_ERROR;
                    } else {
                        Log.w(TAG, "Null response from getMemberList");
                        error = ERROR_RESPONSE_CODE;
                        return ERROR_RESPONSE_CODE;
                    }
                } else {
                    if (statusCode == 500 || statusCode == 403){
                        int err = checkInServer(receiver);
                        if (err != NO_ERROR) parseMembersOfClan(receiver, memberList);
                    }
                    Log.w(TAG, "Response Code do JSON diferente de 200 (Server MemberList)");
                    error = ERROR_RESPONSE_CODE;
                    return ERROR_RESPONSE_CODE;
                }
            } else {
                Log.w(TAG, "Sem conexão com a Internet");
                error = ERROR_NO_CONNECTION;
                return ERROR_NO_CONNECTION;
            }
        } catch (Exception e) {
                    Log.w(TAG, "Problema no HTTP Request (getMembersOfClan)");
                    e.printStackTrace();
                    error = ERROR_HTTP_REQUEST;
                    return ERROR_HTTP_REQUEST;
        }

    }

    private String getLanguageString() {
        getResources();
        String lang = Resources.getSystem().getConfiguration().locale.getLanguage();
        switch (lang) {
            case "pt":
            case "es":
                return lang;
            default:
                return "en";
        }
    }

    private JSONArray createJSONMemberList(ArrayList<String> memberList) {
        JSONArray json = new JSONArray();
        for (int i=0;i<memberList.size();i++){
            json.put(memberList.get(i));
        }
        Log.w(TAG, "jsonMemberList: " + json.toString());
        return json;
    }

    private void getIconList(boolean withClan) {
        int notAdd = 0;
        for(int x=0;x<membersModelList.size();x++){
            if (iconsList.size()==0){
                iconsList.add(membersModelList.get(x).getIconPath());
            } else {
                for (int i=0; i<iconsList.size();i++){
                    if (iconsList.get(i) != null && iconsList.get(i).equals(membersModelList.get(x).getIconPath())){
                        notAdd = 1;
                        //Log.w(TAG, "Number of Images: " + i);
                        break;
                    } else notAdd = 0;
                }
                if (notAdd != 1){
                    iconsList.add(membersModelList.get(x).getIconPath());
                }
            }
        }
        if (withClan){
            iconsList.add(clanIcon);
            iconsList.add(clanBanner);
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();
        result = Html.fromHtml(result).toString();

        return result;
    }

    private void insertClanMember(MemberModel member) {

        ContentValues values = new ContentValues();
        values.put(MemberTable.COLUMN_NAME, member.getName());
        values.put(MemberTable.COLUMN_MEMBERSHIP, member.getMembershipId());
        values.put(MemberTable.COLUMN_CLAN, clanId);
        String imageName = member.getIconPath().substring(member.getIconPath().lastIndexOf("/")+1, member.getIconPath().length());
        values.put(MemberTable.COLUMN_ICON, imageName);
        values.put(MemberTable.COLUMN_PLATFORM, member.getPlatformId());
        values.put(MemberTable.COLUMN_TITLE, member.getTitle());
        values.put(MemberTable.COLUMN_LIKES, member.getLikes());
        values.put(MemberTable.COLUMN_DISLIKES, member.getDislikes());
        values.put(MemberTable.COLUMN_CREATED, member.getGamesCreated());
        values.put(MemberTable.COLUMN_PLAYED, member.getGamesPlayed());
        if (member.isInsert()){
            Log.w(TAG, "Inserting member " + member.getName() + " ...");
            getContentResolver().insert(DataProvider.MEMBER_URI, values);
        } else {
            Log.w(TAG, "Updating member " + member.getName() + " ...");
            getContentResolver().update(DataProvider.MEMBER_URI, values,MemberTable.COLUMN_MEMBERSHIP + "=" + member.getMembershipId(),null);
        }

    }

    private void downloadImages(){
        ArrayList<String> notDownloaded = new ArrayList<>();

        ContentValues values = new ContentValues();
        for (int x=0;x<iconsList.size();x++){
            int error = ImageUtils.downloadImage(getApplicationContext(), BASE_IMAGE_URL + iconsList.get(x));
            if (error == ImageUtils.DOWNLOAD_ERROR){
                notDownloaded.add(BASE_IMAGE_URL + iconsList.get(x));
            } else {
                values.put(SavedImagesTable.COLUMN_PATH, iconsList.get(x));
                getContentResolver().insert(DataProvider.SAVED_IMAGES_URI, values);
            }
            values.clear();
        }
        if (notDownloaded.size() > 0){
            SharedPreferences prefs = getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Set<String> set = new HashSet<>();
            set.addAll(notDownloaded);
            editor.putStringSet(DrawerActivity.DOWNLOAD_PREF, set);
            editor.apply();
        }
    }


    private void insertClan() {
        Log.w(TAG, "Inserting clan data");

        ContentValues values = new ContentValues();
        values.put(ClanTable.COLUMN_BUNGIE_ID, clanId);
        values.put(ClanTable.COLUMN_NAME, clanName);
        String iconName = clanIcon.substring(clanIcon.lastIndexOf("/")+1, clanIcon.length());
        values.put(ClanTable.COLUMN_ICON, iconName);
        String bannerName = clanBanner.substring(clanBanner.lastIndexOf("/")+1, clanBanner.length());
        values.put(ClanTable.COLUMN_BACKGROUND, bannerName);
        values.put(ClanTable.COLUMN_DESC, motto);

        getContentResolver().insert(DataProvider.CLAN_URI, values);

        //Log.w(TAG, "Clan table criado com sucesso!");
    }

    private void insertLoggedUser() {
        Log.w(TAG, "Inserting loggedUser data");

        ContentValues values = new ContentValues();
        values.put(LoggedUserTable.COLUMN_NAME, displayName);
        values.put(LoggedUserTable.COLUMN_MEMBERSHIP, membershipId);
        values.put(LoggedUserTable.COLUMN_CLAN, clanId);
        values.put(LoggedUserTable.COLUMN_PLATFORM, platformId);

        getContentResolver().insert(DataProvider.LOGGED_USER_URI, values);

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
                SharedPreferences.Editor editor = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).edit();
                editor.putInt(DrawerActivity.EVENT_PREF,getDBEventsCount());
                editor.apply();
            }
            values.clear();
            return NO_ERROR;
        }catch (Exception exc){
            exc.printStackTrace();
            return ERROR_RESPONSE_CODE;
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
            SharedPreferences.Editor editor = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).edit();
            editor.putInt(DrawerActivity.TYPE_PREF,getDBTypesCount());
            editor.apply();
        }
        values.clear();
    }

    private class TypeComparator implements java.util.Comparator<EventTypeModel> {
        @Override
        public int compare(EventTypeModel et1, EventTypeModel et2) {
            return et2.getTypeId() - et1.getTypeId();
        }
    }

}