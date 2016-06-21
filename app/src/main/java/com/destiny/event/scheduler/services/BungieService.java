package com.destiny.event.scheduler.services;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.destiny.event.scheduler.BuildConfig;
import com.destiny.event.scheduler.R;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class BungieService extends IntentService {

    private static final String BASE_URL = "https://www.bungie.net/Platform/";
    private static final String BASE_IMAGE_URL = "http://www.bungie.net";

    private static final String USER_PREFIX = "User/";
    private static final String GROUP_PREFIX = "Group/";

    private static final String CURRENT_PAGE = "currentPage=";
    private static final String MEMBER_TYPE = "memberType=";
    private static final String PLATFORM_TYPE = "platformType=";
    private static final String SORT = "sort=";

    public static final String GET_CURRENT_ACCOUNT = "GetCurrentBungieAccount/";
    public static final String GET_MEMBERS_OF_CLAN = "ClanMembers/";
    public static final String GET_BUNGIE_ACCOUNT = "GetBungieAccount/";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 10;
    public static final int STATUS_ERROR = 404;
    public static final int STATUS_DOCS = 1;
    public static final int STATUS_VERIFY = 2;
    public static final int STATUS_PICTURE = 3;
    public static final int STATUS_FRIENDS = 4;
    public static final int STATUS_PARTY = 5;

    public static final String COOKIE_EXTRA = "cookies";
    public static final String REQUEST_EXTRA = "request";
    public static final String RECEIVER_EXTRA = "receiver";
    public static final String XCSRF_EXTRA = "x-csrf";
    public static final String PLATFORM_EXTRA = "platform";

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

    private int error = 0;

    private String iconPath;
    private String displayName;
    private String membershipType;
    private String membershipId;
    private String platformId;

    private String clanId;
    private String clanName;
    private String motto;
    private String clanIcon;
    private String clanBanner;

    private ArrayList<MembersModel> membersModelList;
    private ArrayList<String> iconsList;

    public static final String ICON = "icon";
    public static final String NAME = "name";

    private static final String TAG = "BungieService";

    private String cookie;
    private String request;
    private String xcsrf;

    private String getCurrentBungieAccountResponse;

    public BungieService() {
        super(BungieService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "HTTP Service started!");

        cookie = intent.getStringExtra(COOKIE_EXTRA);
        request = intent.getStringExtra(REQUEST_EXTRA);
        xcsrf = intent.getStringExtra(XCSRF_EXTRA);
        platformId = intent.getStringExtra(PLATFORM_EXTRA);

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
                        error = getMembersOfClan(receiver, 1);
                        Log.w(TAG, "getMembersOfClan error: " + error);
                        if (error != NO_ERROR){
                            sendError(receiver);
                        } else {
                            error = parseBungieAccount(receiver);
                            Log.w(TAG, "parseBungieAccount error: " + error);
                            if (error == NO_ERROR){
                                insertLoggedUser();
                                insertClan();
                                insertClanMembers();
                                insertFakeEvents(receiver);
                                receiver.send(STATUS_FINISHED, Bundle.EMPTY);
                            } else {
                                sendError(receiver);
                            }
                        }
                    }
                }
                break;
        }

        this.stopSelf();

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
                    Log.w(TAG, "API Key: " + BuildConfig.API_KEY);
                    Log.w(TAG, "X-CSRF: " + xcsrf);
                    Log.w(TAG, "Cookies: " + cookie);
                    Log.w(TAG, "getCurrentBungieAccount JSON unparsed: " + getCurrentBungieAccountResponse);

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

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");

            if (jDestinyAccounts.length() > 0) {

                JSONObject obj = jDestinyAccounts.getJSONObject(0);
                JSONObject userInfo = obj.getJSONObject("userInfo");

                membershipId = userInfo.getString("membershipId");
                membershipType = userInfo.getString("membershipType");
                displayName = userInfo.getString("displayName");

                bundle.clear();
                bundle.putString("bungieId", membershipId);
                bundle.putString("userName", displayName);
                receiver.send(STATUS_DOCS, bundle);

                //Verificar se o usuário já tem cadastro no servidor
                receiver.send(STATUS_VERIFY, null);

                iconPath = "/img/profile/avatars/Destiny04.jpg";

                try {
                    JSONObject jBungieNetUser = jResponse.getJSONObject("bungieNetUser");
                    iconPath = jBungieNetUser.getString("profilePicturePath");
                    receiver.send(STATUS_PICTURE, null);
                } catch (JSONException e){
                    //Log.w(TAG, "BungieNetUser TAG not found");
                    receiver.send(STATUS_PICTURE, null);
                    e.printStackTrace();
                }

                try {
                    JSONArray jClans = jResponse.getJSONArray("clans");
                    JSONObject clanObj = jClans.getJSONObject(0);
                    clanId = clanObj.getString("groupId");
                    //Log.w(TAG,"Clan ID: " + clanId);

                    JSONObject jRelatedGroups = jResponse.getJSONObject("relatedGroups");
                    JSONObject jGroup = jRelatedGroups.getJSONObject(clanId);

                    clanName = jGroup.getString("name");
                    motto = jGroup.getString("motto");
                    clanBanner = jGroup.getString("bannerPath");
                    clanIcon = jGroup.getString("avatarPath");
                    receiver.send(STATUS_FRIENDS, null);
                    return NO_ERROR;
                } catch (JSONException e){
                    Log.w(TAG, "Erro no JSON do getGroup");
                    e.printStackTrace();
                    return ERROR_NO_CLAN;
                }

            }
        } catch (JSONException e) {
            Log.w(TAG, "Erro no JSON de getCurrentBungieAccount");
            e.printStackTrace();
            return ERROR_CURRENT_USER;
        }
        return NO_ERROR;
    }

    private int getMembersOfClan(ResultReceiver receiver, int page){

        String myURL = BASE_URL + GROUP_PREFIX + clanId + "/" + GET_MEMBERS_OF_CLAN + "?" + CURRENT_PAGE + String.valueOf(page) + "&" + MEMBER_TYPE + "-1&" + PLATFORM_TYPE + platformId + "&" + SORT + "0";
        //Log.w(TAG,myURL);

        try{

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
                        int clanError = parseMembersOfClan(receiver,response);
                        if (clanError != NO_ERROR) {
                            return clanError;
                        } else return NO_ERROR;
                    }

                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200");
                    return ERROR_RESPONSE_CODE;
                }

            } else {
                Log.w(TAG, "Sem conexão com a Internet");
                return ERROR_NO_CONNECTION;
            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getMembersOfClan)");
            e.printStackTrace();
            return ERROR_HTTP_REQUEST;
        }

        receiver.send(STATUS_FRIENDS, null);
        return NO_ERROR;
    }

    private int parseMembersOfClan(ResultReceiver receiver, String response){

        Log.w(TAG, "getMembersOfClan JSON unparsed :" + response);

        try{
            JSONObject jObject = new JSONObject(response);
            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jResults = jResponse.getJSONArray("results");

            for (int i=0;i<jResults.length();i++){
                JSONObject memberJSON = jResults.getJSONObject(i);
                JSONObject bungieInfo = memberJSON.getJSONObject("destinyUserInfo");

                MembersModel member = new MembersModel();
                member.setMembershipId(bungieInfo.getString("membershipId"));
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
            Log.w(TAG, "Erro no JSON de getBungieAccount");
            e.printStackTrace();
            return ERROR_MEMBERS_OF_CLAN;
        }

    }

    private int parseBungieAccount(ResultReceiver receiver) {

        for (int i = 0; i< membersModelList.size(); i++){
            int clanError = getClanMemberAccount(receiver, membersModelList.get(i).getMembershipId(), platformId, i);
            if (clanError != NO_ERROR){
                return clanError;
            }
        }

        receiver.send(STATUS_PARTY, null);
        return NO_ERROR;
            //bundle.putInt(ERROR_TAG, ERROR_NO_ICON);
    }

    private int getClanMemberAccount(ResultReceiver receiver, String clanMember, String memberType, int position){

        String myURL = BASE_URL + USER_PREFIX + GET_BUNGIE_ACCOUNT + clanMember + "/" + memberType + "/";
        //Log.w(TAG, myURL);
        int notAdd = 0;

        Bundle bundle = new Bundle();

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

                    Log.w(TAG, "getClanMemberAccount JSON unparsed: " + response);

                    if (response != null){
                        bundle.clear();
                        bundle = parseClanMember(response);
                        if (bundle != Bundle.EMPTY){
                            membersModelList.get(position).setName(bundle.getString(NAME));
                            membersModelList.get(position).setIconPath(bundle.getString(ICON));
                            membersModelList.get(position).setPlatformId(platformId);
                            membersModelList.get(position).setClanId(clanId);

                            if (iconsList.size()==0){
                                iconsList.add(membersModelList.get(position).getIconPath());
                            } else {
                                for (int i=0; i<iconsList.size();i++){
                                    if (iconsList.get(i) != null && iconsList.get(i).equals(membersModelList.get(position).getIconPath())){
                                        notAdd = 1;
                                        //Log.w(TAG, "Number of Images: " + i);
                                        break;
                                    } else break;
                                }
                                if (notAdd != 1){
                                    iconsList.add(membersModelList.get(position).getIconPath());
                                }
                            }

                            return NO_ERROR;

                        } else{
                            return ERROR_CLAN_MEMBER;
                        }
                    } else {
                        Log.w(TAG, "Response vazia");
                        return ERROR_CLAN_MEMBER;
                    }

                } else {
                    Log.w(TAG, "Response Code do JSON diferente de 200");
                    return ERROR_RESPONSE_CODE;
                }
            } else {
                Log.w(TAG, "Sem conexão com a Internet");
                return ERROR_NO_CONNECTION;
            }

        }catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getBungieAccount)");
            e.printStackTrace();
            return ERROR_HTTP_REQUEST;
        }

    }

    private Bundle parseClanMember(String response){

        Bundle bundle = new Bundle();

        String memberDisplayName;
        String memberIconPath = "/img/profile/avatars/Destiny04.jpg";

        JSONObject jBungieNetUser;

        try {
            JSONObject jObject = new JSONObject(response);

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");
            try{
                jBungieNetUser = jResponse.getJSONObject("bungieNetUser");
                memberIconPath = jBungieNetUser.getString("profilePicturePath");
            } catch (JSONException e){
                Log.w(TAG, "bungieNetUser TAG not found.");
            }

            JSONObject jO = jDestinyAccounts.getJSONObject(0);
            JSONObject jUserInfo = jO.getJSONObject("userInfo");
            memberDisplayName = jUserInfo.getString("displayName");

            bundle.putString(NAME, memberDisplayName);
            bundle.putString(ICON, memberIconPath);

        } catch (JSONException e) {
            Log.w(TAG, "Erro no JSON de getBungieAccount");
            e.printStackTrace();
            bundle.clear();
            return bundle;
        }

        return bundle;

    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    private void insertFakeEvents(ResultReceiver receiver) {

        Random random = new Random();

        for (int i=0; i<10; i++){
            int member = random.nextInt(membersModelList.size()-1);
            String id = membersModelList.get(member).getMembershipId();
            int event = random.nextInt(56);
            int insc = random.nextInt(5)+1;

            ContentValues values = new ContentValues();
            values.put(GameTable.COLUMN_CREATOR, id);
            values.put(GameTable.COLUMN_CREATOR_NAME, membersModelList.get(member).getName());
            values.put(GameTable.COLUMN_EVENT_ID, event);
            values.put(GameTable.COLUMN_TIME, "2016-05-10T18:13:26");
            values.put(GameTable.COLUMN_LIGHT, 320);
            values.put(GameTable.COLUMN_INSCRIPTIONS, insc);
            values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_NEW);
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

        Log.w(TAG, "Iniciando TitleService...");
        Intent intent = new Intent(getApplicationContext(),TitleService.class);
        intent.putStringArrayListExtra("membershipList",memberIdList);
        startService(intent);

        receiver.send(STATUS_FINISHED, Bundle.EMPTY);

    }

    private void insertClanMembers() {

        Random random = new Random();

        for (int i=0; i<membersModelList.size();i++){
            ContentValues values = new ContentValues();
            values.put(MemberTable.COLUMN_NAME, membersModelList.get(i).getName());
            values.put(MemberTable.COLUMN_MEMBERSHIP, membersModelList.get(i).getMembershipId());
            values.put(MemberTable.COLUMN_CLAN, clanId);

            String imageSubURL = membersModelList.get(i).getIconPath();
            //Log.w(TAG, "Total of icons: " + iconsList.size());

            //ImageUtils.downloadImage(getApplicationContext(), imagePath);

            String imageName = imageSubURL.substring(imageSubURL.lastIndexOf("/")+1, imageSubURL.length());
            //Log.w(TAG, "Image Name: " + imageName);

            values.put(MemberTable.COLUMN_ICON, imageName);
            values.put(MemberTable.COLUMN_PLATFORM, platformId);
            values.put(MemberTable.COLUMN_TITLE, getResources().getString(R.string.default_title));
            int created = (random.nextInt(5));
            int played = (random.nextInt(10)+1);
            int likes = (random.nextInt(created+played));
            int dislikes = (created+played)-likes;
            values.put(MemberTable.COLUMN_LIKES, likes);
            values.put(MemberTable.COLUMN_DISLIKES, dislikes);
            values.put(MemberTable.COLUMN_CREATED, created);
            values.put(MemberTable.COLUMN_PLAYED, played);
            //String dateBefore = membersModelList.get(i).getMemberSince();
            //String dateAfter = dateBefore.substring(0,dateBefore.length()-1);
            //values.put(MemberTable.COLUMN_SINCE, dateAfter);
            getContentResolver().insert(DataProvider.MEMBER_URI, values);

            //Log.w(TAG, membersModelList.get(i).getName() + ": Likes: " + likes + ", Dislikes: " + dislikes + ", Created: " + created + ", Played: " + played);

        }

        //Quando estiver funcionando com o servidor, tentar fazer o download antes de inserir no BD
        //Inserir a imagem correta se conseguir fazer o download, ou então inserir a imagem padrão caso falhe.
        for (int x=0;x<iconsList.size();x++){
            ImageUtils.downloadImage(getApplicationContext(), BASE_IMAGE_URL + iconsList.get(x));
        }

        //Log.w(TAG, "Icon images downloaded succesfully");

    }

    private void insertClan() {

        ContentValues values = new ContentValues();
        values.put(ClanTable.COLUMN_BUNGIE_ID, clanId);
        values.put(ClanTable.COLUMN_NAME, clanName);

        String iconURL = BASE_IMAGE_URL + clanIcon;
        ImageUtils.downloadImage(getApplicationContext(), iconURL);

        String bannerURL = BASE_IMAGE_URL + clanBanner;
        ImageUtils.downloadImage(getApplicationContext(), bannerURL);

        String iconName = clanIcon.substring(clanIcon.lastIndexOf("/")+1, clanIcon.length());
        String bannerName = clanBanner.substring(clanBanner.lastIndexOf("/")+1, clanBanner.length());

        values.put(ClanTable.COLUMN_ICON, iconName);
        values.put(ClanTable.COLUMN_BACKGROUND, bannerName);
        values.put(ClanTable.COLUMN_DESC, motto);

        getContentResolver().insert(DataProvider.CLAN_URI, values);

        //Log.w(TAG, "Clan table criado com sucesso!");
    }

    private void insertLoggedUser() {

        ContentValues values = new ContentValues();
        values.put(LoggedUserTable.COLUMN_NAME, displayName);
        values.put(LoggedUserTable.COLUMN_MEMBERSHIP, membershipId);
        values.put(LoggedUserTable.COLUMN_CLAN, clanId);
        values.put(LoggedUserTable.COLUMN_PLATFORM, platformId);

        getContentResolver().insert(DataProvider.LOGGED_USER_URI, values);

        //Log.w(TAG, "Logged User criado com sucesso!");

    }

}