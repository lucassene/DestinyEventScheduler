package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;

public class MyNewProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MyNewProfileFragment";

    private static final int LOADER_MEMBER = 50;
    private static final int LOADER_PROFILE = 52;

    ImageView profilePic;
    TextView userName;
    TextView titleText;
    TextView memberLevel;
    ProgressBar progressBar;

    private String memberId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = getArguments();

        if (bundle != null){
            memberId = bundle.getString("bungieId");
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.my_new_profile_layout, container, false);

        profilePic = (ImageView) v.findViewById(R.id.profile_pic);
        userName = (TextView) v.findViewById(R.id.primary_text);

        getMemberData();

        return v;
    }

    private void getMemberData() {

        getLoaderManager().initLoader(LOADER_MEMBER, null, this);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_MEMBER:
                return new CursorLoader(
                        getContext(),
                        DataProvider.MEMBER_URI,
                        getMemberProjection(),
                        MemberTable.COLUMN_MEMBERSHIP + "=" + memberId,
                        null,
                        null
                );
        }

        return null;
    }

    private String[] getMemberProjection() {

        String c1 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_ID);
        String c2 = MemberTable.COLUMN_ICON;
        String c3 = MemberTable.COLUMN_NAME;
        String c4 = MemberTable.COLUMN_LIKES;
        String c5 = MemberTable.COLUMN_DISLIKES;
        String c6 = MemberTable.COLUMN_CREATED;
        String c7 = MemberTable.COLUMN_PLAYED;
        String c8 = MemberTable.COLUMN_EXP;

        return new String[] {c1, c2, c3, c4, c5, c6, c7, c8};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_MEMBER:
                    try {
                        profilePic.setImageBitmap(ImageUtils.loadImage(getContext(), data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
                    } catch (IOException e) {
                        Log.e(TAG, "Image Not Found");
                        e.printStackTrace();
                    }
                    userName.setText(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                    memberLevel.setText(getMemberLevel(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_EXP))));
                    progressBar.setProgress(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_EXP)));
                    progressBar.setMax(getMax());
                    break;
            }
        }
    }

    private int getMax() {



        return 0;
    }

    private String getMemberLevel(int memberXP) {
        double xp = (double) memberXP;
        double delta = 1 + 80*xp;
        double lvl = (-1 + Math.sqrt(delta))/20;
        int mLvl = (int) lvl;

        String points = "";

        if (Math.round(mLvl) >= 100) {
            points = "99";
        } else if (Math.round(mLvl) <= 0) {
            points = "00";
        } else if (Math.round(mLvl) < 10) {
            points = "0" + Math.round(mLvl);
        } else points = String.valueOf(mLvl);

        return points;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
