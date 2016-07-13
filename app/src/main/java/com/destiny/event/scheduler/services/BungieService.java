package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.destiny.event.scheduler.BuildConfig;
import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.models.MembersModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.ImageUtils;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BungieService extends IntentService {

    private static final String BASE_URL = "https://www.bungie.net/Platform/";
    private static final String BASE_IMAGE_URL = "http://www.bungie.net";

    private static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    private static final String LOGIN_ENDPOINT = "login";
    private static final String CLAN_ENDPOINT = "clan";
    private static final String MEMBERS_ENDPOINT = "members";
    private static final String MEMBERLIST_ENDPOINT = "member/list";

    private static final String SERVER_MEMBER_HEADER = "membership";
    private static final String SERVER_PLATFORM_HEADER = "platform";

    private static final String USER_PREFIX = "User/";

    public static final String GET_CURRENT_ACCOUNT = "GetCurrentBungieAccount/";
    public static final String GET_BUNGIE_ACCOUNT = "GetBungieAccount/";
    public static final String GET_CLAN_MEMBERS = "GetClanMembers/";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 10;
    public static final int STATUS_ERROR = 404;
    public static final int STATUS_DOCS = 1;
    public static final int STATUS_VERIFY = 2;
    public static final int STATUS_FRIENDS = 4;
    public static final int STATUS_PARTY = 5;

    public static final String COOKIE_EXTRA = "cookies";
    public static final String REQUEST_EXTRA = "request";
    public static final String RECEIVER_EXTRA = "receiver";
    public static final String XCSRF_EXTRA = "x-csrf";
    public static final String PLATFORM_EXTRA = "platform";
    public static final String MEMBERSHIP_EXTRA = "membershipId";

    private static final String KEY_HEADER = "x-api-key";
    private static final String COOKIE_HEADER = "cookie";
    private static final String XCSRF_HEADER = "x-csrf";

    public static final String ERROR_TAG = "error";
    public static final int NO_ERROR = 0;
    public static final int ERROR_NO_CONNECTION = 100;
    public static final int ERROR_HTTP_REQUEST = 110;
    public static final int ERROR_NO_ICON = 120;
    public static final int ERROR_CURRENT_USER = 130;
    public static final int ERROR_RESPONSE_CODE = 140;
    public static final int ERROR_NO_CLAN = 150;
    public static final int ERROR_MEMBERS_OF_CLAN = 160;
    public static final int ERROR_CLAN_MEMBER = 170;

    private static final String DEFAULT_ICON = "/img/profile/avatars/Destiny26.jpg";

    private int error = 0;

    private String displayName = "";
    private String membershipId = "";
    private String platformId;

    private String clanId = "";
    private String clanName = "";
    private String motto = "";
    private String clanIcon = "";
    private String clanBanner = "";

    private int userCreated;
    private int userPlayed;
    private int userLikes;
    private int userDislikes;

    private ArrayList<MembersModel> membersModelList;
    private ArrayList<String> iconsList;

    private static final String TAG = "BungieService";

    private String cookie;
    private String xcsrf;

    private String getCurrentBungieAccountResponse;

    private ArrayList<String> actualMemberList;
    private String userMembership;

    public static final String RUNNING_SERVICE = "bungieRunning";

    public BungieService() {
        super(BungieService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Log.d(TAG, "HTTP Service started!");

        String request = intent.getStringExtra(REQUEST_EXTRA);
        if (intent.hasExtra(COOKIE_EXTRA)) cookie = intent.getStringExtra(COOKIE_EXTRA);
        if (intent.hasExtra(XCSRF_EXTRA)) xcsrf = intent.getStringExtra(XCSRF_EXTRA);
        if (intent.hasExtra(PLATFORM_EXTRA)) platformId = intent.getStringExtra(PLATFORM_EXTRA);
        if (intent.hasExtra(MEMBERSHIP_EXTRA)) membershipId = intent.getStringExtra(MEMBERSHIP_EXTRA);

        membersModelList = new ArrayList<>();
        iconsList = new ArrayList<>();

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_EXTRA);

        switch (request) {
            case GET_CURRENT_ACCOUNT:
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
                            Log.w(TAG, "Inserting clanMembers data");
                            for (int i=0;i<membersModelList.size();i++){
                                insertClanMember(membersModelList.get(i));
                            }
                            //insertFakeEvents(receiver);
                            insertLoggedUser();

                            ArrayList<String> memberIdList = new ArrayList<>();

                            for (int i=0;i<membersModelList.size();i++){
                                memberIdList.add(membersModelList.get(i).getMembershipId());
                            }

                            //Log.w(TAG, "Iniciando TitleService...");
                            Intent titleIntent = new Intent(getApplicationContext(),TitleService.class);
                            titleIntent.putStringArrayListExtra("membershipList",memberIdList);
                            startService(titleIntent);

                            receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                        }
                    }
                }
                break;
            case GET_BUNGIE_ACCOUNT:
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                error = isClanMember(membershipId, platformId);
                if (error != NO_ERROR){
                    sendError(receiver);
                } else {
                    receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                }
                break;
            case GET_CLAN_MEMBERS:
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                clanId = intent.getStringExtra("clanId");
                actualMemberList = intent.getStringArrayListExtra("memberList");
                membershipId = intent.getStringExtra("userMembership");
                platformId = intent.getStringExtra("platformId");
                error = getMembersOfClan(receiver);
                if (error != NO_ERROR){
                    sendError(receiver);
                } else {
                    receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                }
        }

        this.stopSelf();

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

    private int isClanMember(String membershipId, String platformId) {

        String myURL = BASE_URL + USER_PREFIX + GET_BUNGIE_ACCOUNT + membershipId + "/" + platformId + "/";

        try {

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty(KEY_HEADER, BuildConfig.API_KEY);
                urlConnection.setRequestMethod(GET_METHOD);

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200){
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    if (response != null){

                        JSONObject jObject = new JSONObject(response);
                        JSONObject jResponse = jObject.getJSONObject("Response");

                        try {
                            JSONArray jClans = jResponse.getJSONArray("clans");
                            JSONObject clanObj = jClans.getJSONObject(0);
                            clanId = clanObj.getString("groupId");
                            //Log.w(TAG,"Clan ID: " + clanId);
                            return NO_ERROR;
                        } catch (JSONException e){
                            Log.w(TAG, "Erro no JSON do getBungieAccount (clans Tag)");
                            e.printStackTrace();
                            return ERROR_NO_CLAN;
                        }

                    } else {
                        Log.w(TAG, "Response vazia");
                        return NO_ERROR;
                    }

                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200");
                    return NO_ERROR;
                }
            } else {
                Log.w(TAG, "Sem conexão com a Internet");
                return NO_ERROR;
            }

        }catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getBungieAccount)");
            e.printStackTrace();
            return NO_ERROR;
        }
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
                    //Log.w(TAG, "API Key: " + BuildConfig.API_KEY);
                    //Log.w(TAG, "X-CSRF: " + xcsrf);
                    //Log.w(TAG, "Cookies: " + cookie);
                    //Log.w(TAG, "getCurrentBungieAccount: " + getCurrentBungieAccountResponse);

                    if (getCurrentBungieAccountResponse != null) {
                        return NO_ERROR;
                    }
                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200");
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

        return NO_ERROR;

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
                receiver.send(STATUS_DOCS, bundle);

                String userIconPath;
                try {
                    JSONObject jBungie = jResponse.getJSONObject("bungieNetUser");
                    userIconPath = jBungie.getString("profilePicturePath");
                } catch (JSONException e){
                    Log.w(TAG, "User has no bungie picture available");
                    userIconPath = DEFAULT_ICON;
                }

                checkInServer(receiver);

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

                    MembersModel user = new MembersModel();
                    user.setName(displayName);
                    user.setMembershipId(membershipId);
                    user.setClanId(clanId);
                    user.setIconPath(userIconPath);
                    user.setPlatformId(platformId);
                    user.setLikes(userLikes);
                    user.setDislikes(userDislikes);
                    user.setGamesCreated(userCreated);
                    user.setGamesPlayed(userPlayed);
                    user.setInsert(true);

                    membersModelList.add(user);
                    receiver.send(STATUS_FRIENDS, null);
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
            Log.w(TAG, "Erro no JSON de getCurrentBungieAccount");
            e.printStackTrace();
            return ERROR_CURRENT_USER;
        }
    }

    private void checkInServer(ResultReceiver receiver) {
        String myURL = SERVER_BASE_URL + LOGIN_ENDPOINT;
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty(SERVER_MEMBER_HEADER, membershipId);
                urlConnection.setRequestProperty(SERVER_PLATFORM_HEADER, platformId);
                urlConnection.setRequestMethod(POST_METHOD);

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    if (response != null) {
                        JSONObject jObject = new JSONObject(response);
                        //Log.w(TAG, "JSON uparsed do Server: " + response);
                        userLikes = jObject.getInt("likes");
                        userDislikes = jObject.getInt("dislikes");
                        userCreated = jObject.getInt("gamesCreated");
                        userPlayed = jObject.getInt("gamesPlayed");
                        receiver.send(STATUS_VERIFY, Bundle.EMPTY);
                    }
                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200");
                }

            } else {
                Log.w(TAG, "Sem conexão com a internet");
            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getCurrentBungieAccount)");
            e.printStackTrace();
        }
    }

    private int getMembersOfClan(ResultReceiver receiver){

        //String myURL = BASE_URL + GROUP_PREFIX + clanId + "/" + GET_MEMBERS_OF_CLAN + "?" + CURRENT_PAGE + String.valueOf(page) + "&" + MEMBER_TYPE + "-1&" + PLATFORM_TYPE + platformId + "&" + SORT + "0";
        String myURL = SERVER_BASE_URL + CLAN_ENDPOINT + "/" + clanId + "/" + MEMBERS_ENDPOINT;
        //Log.w(TAG,myURL);

        try{

            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //urlConnection.setRequestProperty(KEY_HEADER, BuildConfig.API_KEY);
                urlConnection.setRequestProperty(SERVER_MEMBER_HEADER, membershipId);
                urlConnection.setRequestProperty(SERVER_PLATFORM_HEADER, platformId);
                urlConnection.setRequestMethod(GET_METHOD);

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200){
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    //Log.w(TAG, "getMembersOfClan: " + response);

                    if (response != null){
                        ArrayList<String> memberList = new ArrayList<>();
                        JSONArray jArray = new JSONArray(response);
                        for (int i=0;i<jArray.length();i++){
                            memberList.add(jArray.getString(i));
                            Log.w(TAG, "added member: " + jArray.getString(i));
                        }
                        int error = NO_ERROR;
                        if (actualMemberList != null){
                            memberList.add(membershipId);
                            Log.w(TAG, "added member: " + membershipId);
                            error = parseNewMembers(memberList);
                        } else error = parseMembersOfClan(memberList);
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

    private int parseNewMembers(ArrayList<String> memberList) {

        int error = parseMembersOfClan(memberList);
        if (error != NO_ERROR){
            return error;
        } else {
            for (int i=0;i<membersModelList.size();i++){
                for (int x=0;x<actualMemberList.size();x++){
                    Log.w(TAG, "Is " + membersModelList.get(i).getMembershipId() + " equal to " + actualMemberList.get(x) + " ?");
                    if (actualMemberList.get(x).equals(membersModelList.get(i).getMembershipId())){
                        membersModelList.get(i).setInsert(false);
                        Log.w(TAG, "True");
                        break;
                    } else Log.w(TAG, "False");
                }
                insertClanMember(membersModelList.get(i));
            }
            return NO_ERROR;
        }

    }

    private int parseMembersOfClan(ArrayList<String> memberList) {

        String myURL = SERVER_BASE_URL + MEMBERLIST_ENDPOINT;

        try {
            if (NetworkUtils.checkConnection(getApplicationContext())){

                URL url = new URL(myURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty(SERVER_MEMBER_HEADER, membershipId);
                urlConnection.setRequestProperty(SERVER_PLATFORM_HEADER, platformId);
                urlConnection.setDoOutput(true);
                JSONArray jsonList = createJSONMemberList(memberList);
                urlConnection.setRequestMethod(POST_METHOD);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

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
                            MembersModel member = new MembersModel();
                            member.setMembershipId(jMember.getString("membership"));
                            member.setName(jMember.getString("name"));
                            member.setIconPath(jMember.getString("icon"));
                            member.setPlatformId(jMember.getString("platform"));
                            member.setLikes(jMember.getInt("likes"));
                            member.setDislikes(jMember.getInt("dislikes"));
                            member.setGamesCreated(jMember.getInt("gamesCreated"));
                            member.setGamesPlayed(jMember.getInt("gamesPlayed"));
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
                    Log.w(TAG, "Response Code do JSON diferente de 200");
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

    private JSONArray createJSONMemberList(ArrayList<String> memberList) {
        JSONArray json = new JSONArray();
        for (int i=0;i<memberList.size();i++){
            json.put(memberList.get(i));
        }
        Log.w(TAG, "jsonMemberList: " + json.toString());
        return json;
    }

    /*private int parseMembersOfClan(ResultReceiver receiver, String response) {

        //Log.w(TAG, "getMembersOfClan JSON unparsed :" + response);

        try{
            JSONObject jObject = new JSONObject(response);
            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jResults = jResponse.getJSONArray("results");

            for (int i=0;i<jResults.length();i++){
                JSONObject memberJSON = jResults.getJSONObject(i);
                JSONObject destinyInfo = memberJSON.getJSONObject("destinyUserInfo");

                MembersModel member = new MembersModel();
                member.setMembershipId(destinyInfo.getString("membershipId"));
                member.setName(destinyInfo.getString("displayName"));
                member.setPlatformId(platformId);

                try {
                    JSONObject bungieInfo = memberJSON.getJSONObject("bungieNetUserInfo");
                    member.setIconPath(bungieInfo.getString("iconPath"));
                } catch (JSONException e){
                    Log.w(TAG, "bungieNetUserInfo tag not found");
                    member.setIconPath(DEFAULT_ICON);
                }
                membersModelList.add(i, member);
            }

            String hasMore = jResponse.getString("hasMore");

            JSONObject jQuery = jResponse.getJSONObject("query");
            int currentPage = Integer.parseInt(jQuery.getString("currentPage"));

            if (hasMore.equals("true")){
                currentPage++;
                getMembersOfClan(receiver, currentPage);
            }

            return NO_ERROR;

        } catch (JSONException e) {
            Log.w(TAG, response);
            Log.w(TAG, "Erro no JSON de getMembersOfClan");
            e.printStackTrace();
            return ERROR_MEMBERS_OF_CLAN;
        }
    }*/

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

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();

        return result;
    }

    private void insertFakeEvents(ResultReceiver receiver) {
        Log.w(TAG, "Inserting fakeEvents data");

        Random random = new Random();

        for (int i=0; i<10; i++){
            int member = random.nextInt(membersModelList.size()-1);
            String id = membersModelList.get(member).getMembershipId();
            int event = random.nextInt(57)+1;
            int insc = random.nextInt(5)+1;

            ContentValues values = new ContentValues();
            values.put(GameTable.COLUMN_CREATOR, id);
            values.put(GameTable.COLUMN_CREATOR_NAME, membersModelList.get(member).getName());
            values.put(GameTable.COLUMN_EVENT_ID, event);
            values.put(GameTable.COLUMN_TIME, "2016-05-10T18:13:26");
            values.put(GameTable.COLUMN_LIGHT, 320);
            values.put(GameTable.COLUMN_INSCRIPTIONS, insc);
            values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_NEW);
            values.put(GameTable.COLUMN_PLATFORM, platformId);
            getContentResolver().insert(DataProvider.GAME_URI, values);
            values.clear();

            ContentValues first = new ContentValues();
            first.put(EntryTable.COLUMN_GAME,i+1);
            first.put(EntryTable.COLUMN_MEMBERSHIP, id);
            first.put(EntryTable.COLUMN_TIME, DateUtils.getCurrentTime());
            getContentResolver().insert(DataProvider.ENTRY_URI, first);
            first.clear();

            for (int x=0; x<insc-1; x++){
                ContentValues entries = new ContentValues();
                entries.put(EntryTable.COLUMN_GAME, i+1);
                String mid = membersModelList.get(random.nextInt(membersModelList.size()-1)).getMembershipId();
                entries.put(EntryTable.COLUMN_MEMBERSHIP, mid);
                entries.put(EntryTable.COLUMN_TIME, DateUtils.getCurrentTime());
                getContentResolver().insert(DataProvider.ENTRY_URI, entries);
                entries.clear();
            }

        }

        ArrayList<String> memberIdList = new ArrayList<>();

        for (int i=0;i<membersModelList.size();i++){
            memberIdList.add(membersModelList.get(i).getMembershipId());
        }

        //Log.w(TAG, "Iniciando TitleService...");
        Intent intent = new Intent(getApplicationContext(),TitleService.class);
        intent.putStringArrayListExtra("membershipList",memberIdList);
        startService(intent);

        receiver.send(STATUS_FINISHED, Bundle.EMPTY);

    }

    private void insertClanMember(MembersModel member) {

        ContentValues values = new ContentValues();
        values.put(MemberTable.COLUMN_NAME, member.getName());
        values.put(MemberTable.COLUMN_MEMBERSHIP, member.getMembershipId());
        values.put(MemberTable.COLUMN_CLAN, clanId);

        String imageSubURL = member.getIconPath();
        //Log.w(TAG, "Total of icons: " + iconsList.size());
        String imageName = imageSubURL.substring(imageSubURL.lastIndexOf("/")+1, imageSubURL.length());
        //Log.w(TAG, "Image Name: " + imageName);

        values.put(MemberTable.COLUMN_ICON, imageName);
        values.put(MemberTable.COLUMN_PLATFORM, member.getPlatformId());
        values.put(MemberTable.COLUMN_TITLE, getResources().getString(R.string.default_title));
        values.put(MemberTable.COLUMN_LIKES, member.getLikes());
        values.put(MemberTable.COLUMN_DISLIKES, member.getDislikes());
        values.put(MemberTable.COLUMN_CREATED, member.getGamesCreated());
        values.put(MemberTable.COLUMN_PLAYED, member.getGamesPlayed());
        if (member.isInsert()){
            Log.w(TAG, "Inserting member " + member.getMembershipId() + " ...");
            getContentResolver().insert(DataProvider.MEMBER_URI, values);
        } else {
            Log.w(TAG, "Updating member " + member.getMembershipId() + " ...");
            getContentResolver().update(DataProvider.MEMBER_URI, values,MemberTable.COLUMN_MEMBERSHIP + "=" + member.getMembershipId(),null);
        }

}

    private void downloadImages(){
        ArrayList<String> notDownloaded = new ArrayList<>();

        for (int x=0;x<iconsList.size();x++){
            int error = ImageUtils.downloadImage(getApplicationContext(), BASE_IMAGE_URL + iconsList.get(x));
            if (error == ImageUtils.DOWNLOAD_ERROR){
                notDownloaded.add(BASE_IMAGE_URL + iconsList.get(x));
            }
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

        //String iconURL = BASE_IMAGE_URL + clanIcon;
        //ImageUtils.downloadImage(getApplicationContext(), iconURL);

        //String bannerURL = BASE_IMAGE_URL + clanBanner;
        //ImageUtils.downloadImage(getApplicationContext(), bannerURL);

        String iconName = clanIcon.substring(clanIcon.lastIndexOf("/")+1, clanIcon.length());
        String bannerName = clanBanner.substring(clanBanner.lastIndexOf("/")+1, clanBanner.length());

        values.put(ClanTable.COLUMN_ICON, iconName);
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

        //Log.w(TAG, "Logged User criado com sucesso!");

    }

}