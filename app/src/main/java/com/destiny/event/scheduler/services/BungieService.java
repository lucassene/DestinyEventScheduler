package com.destiny.event.scheduler.services;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.models.MembersModel;
import com.destiny.event.scheduler.provider.DataProvider;

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

public class BungieService extends IntentService {

    private static final String API_KEY = "4788fecc8fc04393984ff76619b7501f";
    private static final String BASE_URL = "https://www.bungie.net/Platform/";

    private static final String USER_PREFIX = "User/";
    private static final String GROUP_PREFIX = "Group/";

    private static final String CURRENT_PAGE = "currentPage=";
    private static final String MEMBER_TYPE = "memberType=";
    private static final String PLATFORM_TYPE = "platformType=";
    private static final String SORT = "sort=";
    private static final String BUNGIE_TYPE = "/254/";

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

    private String iconPath;
    private String displayName;
    private String membershipType;
    private String membershipId;
    private String bungieId;
    private String platformId;
    private String approvalDate;

    private String clanId;
    private String clanName;
    private String motto;
    private String clanIcon;
    private String clanBanner;

    private ArrayList<MembersModel> membersModelList;

    public static final String ICON = "icon";
    public static final String NAME = "name";
    public static final String PLATFORM = "platform";
    public static final String MEMBER_ID = "memberId";

    private static final String TAG = "BungieService";

    private String cookie;
    private String request;
    private String xcsrf;

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

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_EXTRA);

        switch (request) {
            case GET_CURRENT_ACCOUNT:
                getBungieAccount(receiver);
                insertLoggedUser();
                insertClan();
                insertClanMembers();
                break;
        }

        this.stopSelf();

    }

    private void insertClanMembers() {

        for (int i=0; i<membersModelList.size();i++){
            ContentValues values = new ContentValues();
            values.put(MemberTable.COLUMN_NAME, membersModelList.get(i).getName());
            values.put(MemberTable.COLUMN_MEMBERSHIP, membersModelList.get(i).getMembershipId());
            values.put(MemberTable.COLUMN_CLAN, clanId);
            values.put(MemberTable.COLUMN_ICON, membersModelList.get(i).getIconPath());
            values.put(MemberTable.COLUMN_PLATFORM, platformId);
            values.put(MemberTable.COLUMN_LIKES, 0);
            values.put(MemberTable.COLUMN_DISLIKES, 0);
            values.put(MemberTable.COLUMN_CREATED, 0);
            values.put(MemberTable.COLUMN_PLAYED, 0);
            values.put(MemberTable.COLUMN_SINCE, membersModelList.get(i).getMemberSince());
            getContentResolver().insert(DataProvider.MEMBER_URI, values);

            Log.w(TAG, "Clan members criados com sucesso!");
        }

    }

    private void insertClan() {

        ContentValues values = new ContentValues();
        values.put(ClanTable.COLUMN_BUNGIE_ID, clanId);
        values.put(ClanTable.COLUMN_NAME, clanName);
        values.put(ClanTable.COLUMN_ICON, clanIcon);
        values.put(ClanTable.COLUMN_BACKGROUND, clanBanner);
        values.put(ClanTable.COLUMN_DESC, motto);

        getContentResolver().insert(DataProvider.CLAN_URI, values);

        Log.w(TAG, "Clan table criado com sucesso!");
    }

    private void insertLoggedUser() {

        ContentValues values = new ContentValues();
        values.put(LoggedUserTable.COLUMN_NAME, displayName);
        values.put(LoggedUserTable.COLUMN_MEMBERSHIP, membershipId);
        values.put(LoggedUserTable.COLUMN_CLAN, clanId);
        values.put(LoggedUserTable.COLUMN_ICON, iconPath);
        values.put(LoggedUserTable.COLUMN_PLATFORM, platformId);
        values.put(LoggedUserTable.COLUMN_LIKES, 0);
        values.put(LoggedUserTable.COLUMN_DISLIKES, 0);
        values.put(LoggedUserTable.COLUMN_CREATED, 0);
        values.put(LoggedUserTable.COLUMN_PLAYED, 0);
        values.put(LoggedUserTable.COLUMN_SINCE, approvalDate);

        getContentResolver().insert(DataProvider.LOGGED_USER_URI, values);

        Log.w(TAG, "Logged User criado com sucesso!");

    }


    private void getBungieAccount(ResultReceiver receiver){

        String myURL = BASE_URL + USER_PREFIX + GET_CURRENT_ACCOUNT;
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {
            URL url = new URL(myURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty(KEY_HEADER, API_KEY);
            urlConnection.setRequestProperty(XCSRF_HEADER, xcsrf);
            urlConnection.setRequestProperty(COOKIE_HEADER, cookie);
            urlConnection.setRequestMethod(GET_METHOD);

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                //Log.w(TAG, "X-CSRF: " + xcsrf);
                //Log.w(TAG, "Cookies: " + cookie);
                //Log.w(TAG, "JSON unparsed: " + response);

                if (response != null) {

                    Bundle bundle;

                    bundle = parseBungieAccount(receiver,response,STATUS_DOCS);
                    receiver.send(STATUS_DOCS, bundle);

                    bundle.clear();
                    bundle = parseBungieAccount(receiver,response,STATUS_VERIFY);
                    receiver.send(STATUS_VERIFY, Bundle.EMPTY);

                    bundle.clear();
                    bundle = parseBungieAccount(receiver,response,STATUS_PICTURE);
                    receiver.send(STATUS_PICTURE, bundle);

                    bundle.clear();
                    bundle = parseBungieAccount(receiver,response,STATUS_FRIENDS);
                    receiver.send(STATUS_FRIENDS, bundle);

                    bundle.clear();
                    bundle = parseBungieAccount(receiver,response,STATUS_PARTY);
                    receiver.send(STATUS_PARTY, Bundle.EMPTY);

                    receiver.send(STATUS_FINISHED, Bundle.EMPTY);

                }
            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getCurrentBungieAccount)");
            receiver.send(STATUS_ERROR, Bundle.EMPTY);
        }

    }

    private Bundle parseBungieAccount(ResultReceiver receiver, String response, int status) {

        Bundle bundle = new Bundle();

        try {
            JSONObject jObject = new JSONObject(response);

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");

            if (jDestinyAccounts.length()>0){

                switch (status){
                    case STATUS_DOCS:

                        JSONObject obj = jDestinyAccounts.getJSONObject(0);
                        JSONObject userInfo = obj.getJSONObject("userInfo");
                        JSONObject bungieInfo = jResponse.getJSONObject("bungieNetUser");

                        membershipId = bungieInfo.getString("membershipId");
                        membershipType = userInfo.getString("membershipType");
                        //membershipId = userInfo.getString("membershipId");
                        displayName = userInfo.getString("displayName");

                        Log.w(TAG,"Results: " + displayName + " " + membershipId );

                       /* bundle.clear();
                        bundle.putString(PLATFORM, membershipType);
                        bundle.putString(MEMBER_ID, membershipId);
                        bundle.putString(NAME, displayName);*/

                        break;

                    case STATUS_VERIFY:

                        //Verifica se o usuário já tem cadastrado no BD do servidor
                        break;

                    case STATUS_PICTURE:

                        JSONObject jBungieNetUser = jResponse.getJSONObject("bungieNetUser");
                        iconPath = jBungieNetUser.getString("profilePicturePath");

                        Log.w(TAG,"IconPath: " + iconPath);

                        /*bundle.clear();
                        bundle.putString(ICON, iconPath);*/

                        break;

                    case STATUS_FRIENDS:

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

                        Log.w(TAG, "Clan Info: " + clanName + " - " + motto);

                        /*bundle.clear();
                        bundle.putString(CLAN_ID, clanId);
                        bundle.putString(CLAN_NAME, clanName);
                        bundle.putString(CLAN_DESC, motto);
                        bundle.putString(CLAN_ICON, clanIcon);
                        bundle.putString(CLAN_BACKGROUND, clanBanner);*/

                        break;

                    case STATUS_PARTY:
                        getMembersOfClan(receiver, 1);

                        for (int i = 0; i< membersModelList.size(); i++){
                            getClanMemberAccount(receiver, membersModelList.get(i).getMembershipId(), BUNGIE_TYPE, i);
                        }

                        /*Log.w(TAG,"Name: " + membersModelList.get(1).getName());
                        Log.w(TAG,"Bungie Id: " + membersModelList.get(1).getBungieId());
                        Log.w(TAG,"Membership Id: " + membersModelList.get(1).getMembershipId());
                        Log.w(TAG,"Clan Id: " + membersModelList.get(1).getClanId());
                        Log.w(TAG,"Icon Path: " + membersModelList.get(1).getIconPath());
                        Log.w(TAG,"Platform Id: " + membersModelList.get(1).getPlatformId());
                        Log.w(TAG,"Member Since: " + membersModelList.get(1).getMemberSince());*/

                        break;

                    default:
                        bundle.clear();
                        break;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bundle;
    }

    private void getMembersOfClan(ResultReceiver receiver, int page){

        String myURL = BASE_URL + GROUP_PREFIX + clanId + "/" + GET_MEMBERS_OF_CLAN + "?" + CURRENT_PAGE + String.valueOf(page) + "&" + MEMBER_TYPE + "-1&" + PLATFORM_TYPE + platformId + "&" + SORT + "0";
        //Log.w(TAG,myURL);
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try{

            URL url = new URL(myURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty(KEY_HEADER, API_KEY);
            urlConnection.setRequestMethod(GET_METHOD);

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200){
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);

                if (response != null){
                    parseMembersOfClan(receiver,response);
                }

            }

        } catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getMembersOfClan)");
            receiver.send(STATUS_ERROR, Bundle.EMPTY);
        }
    }

    private void parseMembersOfClan(ResultReceiver receiver, String response){

        try{
            JSONObject jObject = new JSONObject(response);
            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jResults = jResponse.getJSONArray("results");

            for (int i=0;i<jResults.length();i++){
                JSONObject memberJSON = jResults.getJSONObject(i);
                JSONObject bungieInfo = memberJSON.getJSONObject("bungieNetUserInfo");

                if (!bungieInfo.getString("membershipId").equals(membershipId)){
                    MembersModel member = new MembersModel();
                    member.setMemberSince(memberJSON.getString("approvalDate"));
                    member.setMembershipId(bungieInfo.getString("membershipId"));
                    membersModelList.add(i, member);
                    //Log.w(TAG, "Clan member: " + membersModelList.get(i));
                    //Log.w(TAG, "Approval Date: " + member.getMemberSince());
                } else {
                    approvalDate = memberJSON.getString("approvalDate");
                }
            }

            String hasMore = jResponse.getString("hasMore");
            //Log.w(TAG,"hasMore = " + hasMore);

            JSONObject jQuery = jResponse.getJSONObject("query");
            int currentPage = Integer.parseInt(jQuery.getString("currentPage"));

            //Log.w(TAG,"currentPage = " + jQuery.getString("currentPage"));

            if (hasMore.equals("true")){
                currentPage++;
                getMembersOfClan(receiver, currentPage);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getClanMemberAccount(ResultReceiver receiver, String clanMember, String memberType, int position){

        String myURL = BASE_URL + USER_PREFIX + GET_BUNGIE_ACCOUNT + clanMember + memberType;
        Log.w(TAG, myURL);

        try {
            URL url = new URL(myURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty(KEY_HEADER, API_KEY);
            urlConnection.setRequestMethod(GET_METHOD);

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200){
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);

                if (response != null){
                    Bundle bundle = parseClanMember(response);
                    if (bundle != Bundle.EMPTY){
                        membersModelList.get(position).setName(bundle.getString(NAME));
                        membersModelList.get(position).setIconPath(bundle.getString(ICON));
                        membersModelList.get(position).setPlatformId(platformId);
                        membersModelList.get(position).setClanId(clanId);
                        Log.w(TAG, "Clan member: " + membersModelList.get(position).getMembershipId() + ": " + membersModelList.get(position).getName());
                    }
                }
            }

        }catch (Exception e) {
            Log.w(TAG, "Problema no HTTP Request (getBungieAccount)");
            receiver.send(STATUS_ERROR, Bundle.EMPTY);
        }

    }

    private Bundle parseClanMember(String response){

        Bundle bundle = new Bundle();

        String memberMembershipType;
        String memberMembershipId;
        String memberDisplayName;
        String memberIconPath;

        try {
            JSONObject jObject = new JSONObject(response);

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");
            JSONObject jBungieNetUser = jResponse.getJSONObject("bungieNetUser");

            if (jDestinyAccounts.length()!=0){
                JSONObject jO = jDestinyAccounts.getJSONObject(0);
                JSONObject jUserInfo = jO.getJSONObject("userInfo");
                memberDisplayName = jUserInfo.getString("displayName");
            } else {
                memberDisplayName = jBungieNetUser.getString("displayName");
            }

            memberIconPath = jBungieNetUser.getString("profilePicturePath");
            memberMembershipId = jBungieNetUser.getString("membershipId");

            bundle.putString(MEMBER_ID, memberMembershipId);
            bundle.putString(NAME, memberDisplayName);
            bundle.putString(ICON, memberIconPath);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bundle;

    }


    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

}