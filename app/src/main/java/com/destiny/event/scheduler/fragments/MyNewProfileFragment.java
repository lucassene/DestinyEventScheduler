package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;
import com.destiny.event.scheduler.utils.StringUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.util.ArrayList;

public class MyNewProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MyNewProfileFragment";

    private static final int LOADER_MEMBER = 50;
    private static final int LOADER_PROFILE = 52;

    ImageView profilePic;
    TextView userName;
    TextView titleText;
    TextView memberLevel;
    ProgressBar progressBar;
    TextView xpText;

    private String memberId;

    PieChart eventsChart;
    PieChart likesChart;

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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.my_profile);
        View v = inflater.inflate(R.layout.my_new_profile_layout, container, false);

        profilePic = (ImageView) v.findViewById(R.id.profile_pic);
        userName = (TextView) v.findViewById(R.id.primary_text);
        memberLevel = (TextView) v.findViewById(R.id.member_level);
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        xpText = (TextView) v.findViewById(R.id.xp_text);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int chartHeight = metrics.widthPixels - 32;

        eventsChart = (PieChart) v.findViewById(R.id.events_chart);
        eventsChart.setMinimumHeight(chartHeight);
        eventsChart.setUsePercentValues(true);
        eventsChart.setDescription("");
        eventsChart.setExtraOffsets(5, 10, 5, 5);
        eventsChart.setDrawHoleEnabled(false);
        eventsChart.setRotationEnabled(false);
        Legend eventsLeg = eventsChart.getLegend();
        eventsLeg.setEnabled(false);

        likesChart = (PieChart) v.findViewById(R.id.evaluation_chart);
        likesChart.setMinimumHeight(chartHeight);
        likesChart.setUsePercentValues(true);
        likesChart.setDescription("");
        likesChart.setExtraOffsets(5, 10, 5, 5);
        likesChart.setDrawHoleEnabled(false);
        likesChart.setRotationEnabled(false);
        Legend likesLeg = likesChart.getLegend();
        likesLeg.setEnabled(false);

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

            Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

            switch (loader.getId()){
                case LOADER_MEMBER:
                    try {
                        profilePic.setImageBitmap(ImageUtils.loadImage(getContext(), data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
                    } catch (IOException e) {
                        Log.e(TAG, "Image Not Found");
                        e.printStackTrace();
                    }
                    userName.setText(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                    int xp = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
                    int lvl = MemberTable.getMemberLevel(xp);
                    memberLevel.setText(StringUtils.parseString(lvl));
                    progressBar.setMax(MemberTable.getExpNeeded(xp));
                    progressBar.setProgress(xp);
                    String xpTxt = xp + " / " + MemberTable.getExpNeeded(xp);
                    xpText.setText(xpTxt);
                    //Log.w(TAG, "Player XP: " + progressBar.getProgress() + "/" + progressBar.getMax());

                    int created = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED));
                    int played = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED));
                    //Log.w(TAG, "Created: " + created + "; Played: " + played);

                    int likes = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES));
                    int dislikes = data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES));
                    Log.w(TAG, "Likes: " + likes + "; Dislikes: " + dislikes);

                    setEventChart(created, played);
                    setLikesChart(likes, dislikes);

                    break;
            }
        }
    }

    private void setLikesChart(int likes, int dislikes) {
        int index = 0;
        ArrayList<String> labels = new ArrayList<>();

        if (likes>0){
            labels.add(getResources().getString(R.string.likes));
            index++;
        }
        if (dislikes>0) {
            labels.add(getResources().getString(R.string.dislikes));
            index++;
        }

        if (index>0){
            ArrayList<Entry> yValues = new ArrayList<>();
            if (likes>0) yValues.add(new Entry((float)likes, 0));
            if (dislikes>0) yValues.add(new Entry((float)dislikes, index-1));

            PieDataSet dataSet = new PieDataSet(yValues, getResources().getString(R.string.evaluations));
            if (index>1) {
                dataSet.setSliceSpace(6f);
            } else dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(12f);
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

            PieData data = new PieData(labels, dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(13f);
            Highlight h = new Highlight(getBigger(likes, dislikes),0);
            likesChart.highlightValues(new Highlight[] {h});
            likesChart.setData(data);
            likesChart.invalidate();
        }

    }

    private void setEventChart(int created, int played) {
        int index = 0;
        ArrayList<String> labels = new ArrayList<>();

        if (created>0){
            labels.add(getResources().getString(R.string.created));
            index++;
        }
        if (played>0) {
            labels.add(getResources().getString(R.string.played));
            index++;
        }

        if (index>0){
            ArrayList<Entry> yValues = new ArrayList<>();
            if (created>0) yValues.add(new Entry((float)created, 0));
            if (played>0) yValues.add(new Entry((float)played, index-1));

            PieDataSet dataSet = new PieDataSet(yValues, getResources().getString(R.string.events));
            if (index>1){
                dataSet.setSliceSpace(6f);
            } else dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(12f);
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

            PieData data = new PieData(labels, dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(13f);
            Highlight h = new Highlight(getBigger(created, played),0);
            eventsChart.highlightValues(new Highlight[] {h});
            eventsChart.setData(data);
            eventsChart.invalidate();
        }

    }

    public int getBigger(int value1, int value2){
        if (value1 >= value2){
            return 0;
        } else return 1;
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

}
