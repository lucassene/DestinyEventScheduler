package com.destiny.event.scheduler.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

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

public class DestinyService extends IntentService {

    private static final String API_KEY = "4788fecc8fc04393984ff76619b7501f";
    private static final String BASE_URL = "https://www.bungie.net/Platform/";

    public static final String GET_BUNGIE_ACCOUNT = "User/GetCurrentBungieAccount/";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public static final String COOKIE_EXTRA = "cookies";
    public static final String REQUEST_EXTRA = "request";
    public static final String RECEIVER_EXTRA = "receiver";
    public static final String XCSRF_EXTRA = "x-csrf";

    private static final String KEY_HEADER = "x-api-key";
    private static final String COOKIE_HEADER = "cookie";
    private static final String XCSRF_HEADER = "x-csrf";

    private String iconPath;
    private String displayName;
    private String membershipType;
    private String membershipId;

    private String clanName;
    private String motto;
    private String clanIcon;
    private String clanBanner;

    public static final String ICON = "icon";
    public static final String NAME = "name";
    public static final String PLATFORM = "platform";
    public static final String MEMBER_ID = "memberId";

    private static final String TAG = "DestinyService";

    public DestinyService() {
        super(DestinyService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "HTTP Service started!");

        String cookie = intent.getStringExtra(COOKIE_EXTRA);
        String request = intent.getStringExtra(REQUEST_EXTRA);
        String xcsrf = intent.getStringExtra(XCSRF_EXTRA);

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_EXTRA);

        Bundle bundle = new Bundle();

        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        int result = 0;

        switch (request) {
            case GET_BUNGIE_ACCOUNT:
                String myURL = BASE_URL + GET_BUNGIE_ACCOUNT;
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                try {
                    URL url = new URL(myURL);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestProperty(KEY_HEADER, API_KEY);
                    urlConnection.setRequestProperty(XCSRF_HEADER, xcsrf);
                    urlConnection.setRequestProperty(COOKIE_HEADER, cookie);

                    urlConnection.setRequestMethod(GET_METHOD);

                    int statusCode = urlConnection.getResponseCode();

                    if (statusCode == 200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        Log.w(TAG, "X-CSRF: " + xcsrf);
                        Log.w(TAG, "Cookies: " + cookie);
                        Log.w(TAG, "JSON unparsed: " + response);

                        if (response != null) {
                            parseResult(response);
                            bundle.putString(ICON, iconPath);
                            bundle.putString(PLATFORM, membershipType);
                            bundle.putString(MEMBER_ID, membershipId);
                            bundle.putString(NAME, displayName);
                            receiver.send(STATUS_FINISHED, bundle);
                        }

                        result = 1;
                    } else result = 0;

                } catch (Exception e) {
                    Log.w(TAG, "Problema no HTTP Request");
                    receiver.send(STATUS_ERROR, Bundle.EMPTY);
                }
        }

        this.stopSelf();

    }

    private void parseResult(String response) {

        try {
            JSONObject jObject = new JSONObject(response);

            JSONObject jResponse = jObject.getJSONObject("Response");
            JSONArray jDestinyAccounts = jResponse.getJSONArray("destinyAccounts");

            JSONObject obj = jDestinyAccounts.getJSONObject(0);
            JSONObject userInfo = obj.getJSONObject("userInfo");

            //iconPath = userInfo.getString("iconPath");
            membershipType = userInfo.getString("membershipType");
            membershipId = userInfo.getString("membershipId");
            displayName = userInfo.getString("displayName");

            Log.w(TAG,"Results: " + displayName + " " + membershipId + " / " + membershipType );

            JSONObject jBungieNetUser = jResponse.getJSONObject("bungieNetUser");
            iconPath = jBungieNetUser.getString("profilePicturePath");
            Log.w(TAG,"IconPath: " + iconPath);

            JSONArray jClans = jResponse.getJSONArray("clans");
            JSONObject clanObj = jClans.getJSONObject(0);
            String clanId = clanObj.getString("groupId");
            Log.w(TAG,"Clan ID: " + clanId);

            JSONObject jRelatedGroups = jResponse.getJSONObject("relatedGroups");
            JSONObject jGroup = jRelatedGroups.getJSONObject(clanId);

            clanName = jGroup.getString("name");
            motto = jGroup.getString("motto");
            clanBanner = jGroup.getString("bannerPath");
            clanIcon = jGroup.getString("avatarPath");
            Log.w(TAG, "Clan Info: " + clanName + " - " + motto);


        } catch (JSONException e) {
            e.printStackTrace();
        }


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