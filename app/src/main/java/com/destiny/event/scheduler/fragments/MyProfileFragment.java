package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;

public class MyProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_MEMBER = 50;
    private static final String TAG = "MyProfileFragment";

    public static final int TYPE_USER = 1;
    public static final int TYPE_MEMBER = 2;

    //private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_SINCE, MemberTable.COLUMN_CREATED, MemberTable.COLUMN_PLAYED, MemberTable.COLUMN_LIKES, MemberTable.COLUMN_DISLIKES};
    //private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.profile_date, R.id.profile_created, R.id.profile_played, R.id.profile_likes, R.id.profile_dislikes, R.id.profile_points};

    ImageView profilePic;
    TextView userName;
    TextView joinDate;
    TextView gamesCreated;
    TextView gamesPlayed;
    TextView likes;
    TextView dislikes;
    TextView points;

    TextView joinedText;
    TextView createdText;
    TextView playedText;

    private String bungieId;
    private int type;
    private String clanName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.my_profile);
        View v = inflater.inflate(R.layout.my_profile_layout, container, false);

        Bundle bundle = getArguments();

        bungieId = bundle.getString("bungieId");
        type = bundle.getInt("type");
        clanName = bundle.getString("clanName");

        profilePic = (ImageView) v.findViewById(R.id.profile_pic);
        userName = (TextView) v.findViewById(R.id.primary_text);
        joinDate = (TextView) v.findViewById(R.id.profile_date);
        gamesCreated = (TextView) v.findViewById(R.id.profile_created);
        gamesPlayed = (TextView) v.findViewById(R.id.profile_played);
        likes = (TextView) v.findViewById(R.id.profile_likes);
        dislikes = (TextView) v.findViewById(R.id.profile_dislikes);
        points = (TextView) v.findViewById(R.id.profile_points);

        joinedText = (TextView) v.findViewById(R.id.join_text);
        createdText = (TextView) v.findViewById(R.id.created_text);
        playedText = (TextView) v.findViewById(R.id.played_text);

        getMemberData();

        return v;
    }

    private void getMemberData() {
        getLoaderManager().initLoader(LOADER_MEMBER, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection;
        String[] selectionArgs;

        switch (id){
            case LOADER_MEMBER:
                projection = MemberTable.ALL_COLUMNS;
                selectionArgs = new String[] {bungieId};
                return new CursorLoader(
                        getContext(),
                        DataProvider.MEMBER_URI,
                        projection,
                        MemberTable.COLUMN_MEMBERSHIP + "=?",
                        selectionArgs,
                        null
                );
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();

        switch (loader.getId()){
            case LOADER_MEMBER:

                try {
                    profilePic.setImageBitmap(ImageUtils.loadImage(getContext(), data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
                } catch (IOException e) {
                    Log.w(TAG, "Image not found");
                    e.printStackTrace();
                }
                String name = data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME));
                userName.setText(name);

                //String date = DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE)));
                //joinDate.setText(date);

                int created = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED));
                gamesCreated.setText(String.valueOf(created));

                int played = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED));
                gamesPlayed.setText(String.valueOf(played+created));

                int totalLikes = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES));
                likes.setText(String.valueOf(totalLikes));

                int totalDislikes = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES));
                dislikes.setText(String.valueOf(totalDislikes));

                String totalPoints = getPoints(created, played, totalLikes, totalDislikes);
                points.setText(totalPoints);

                String j = "";
                String c = "";
                String p = "";

                switch (type){
                    case TYPE_MEMBER:
                        j = getContext().getResources().getString(R.string.date_member_join1) + " " + name + " " + getContext().getResources().getString(R.string.date_member_join2) + " " + clanName;
                        c =  getContext().getResources().getString(R.string.games_created_by_member) + " " + name + ".";
                        p = getContext().getResources().getString(R.string.games_played_by_member) + " " + name + ".";
                        break;
                    case TYPE_USER:
                        j = getContext().getResources().getString(R.string.date_you_join) + " " + clanName;
                        c = getContext().getResources().getString(R.string.games_created_by_you);
                        p = getContext().getResources().getString(R.string.games_played_you);
                        break;
                }

                joinedText.setText(j);
                createdText.setText(c);
                playedText.setText(p);

        }

    }

    private String getPoints(int created, int played, int totalLikes, int totalDislikes) {

        double xp = (double) (totalLikes*Integer.parseInt(MemberTable.LIKE_MODIFIER) + (created*Integer.parseInt(MemberTable.CREATOR_MODIFIER)) + (played*Integer.parseInt(MemberTable.PLAYED_MODIFIER)) - (totalDislikes*Integer.parseInt(MemberTable.DISLIKE_MODIFIER)));
        double delta = 1 + 8*xp;
        double lvl = (-1 + Math.sqrt(delta))/2;
        int mLvl = (int) lvl;

        if (Math.round(mLvl) >= 100) {
            return "99";
        } else if (Math.round(mLvl) <= 0){
            return "00";
        } else if (Math.round(mLvl) < 10){
            String finalPoint = "0" + String.valueOf(mLvl);
            return finalPoint;
        } else return String.valueOf(String.valueOf(mLvl));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
