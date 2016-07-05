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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.ChartLegendAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.ChartLegendModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;
import com.destiny.event.scheduler.utils.StringUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.util.ArrayList;

public class MyNewProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MyNewProfileFragment";

    private static final int LOADER_MEMBER = 50;
    private static final int LOADER_PROFILE = 75;
    private static final int LOADER_FAVORITE = 76;

    public static final int TYPE_MENU = 1;
    public static final int TYPE_DETAIL = 2;

    private int type;

    ImageView profilePic;
    TextView userName;
    TextView titleText;
    TextView memberLevel;
    ProgressBar progressBar;
    TextView xpText;
    ListView evaluationLegends;
    ListView eventsLegends;
    ListView gameLegends;
    TextView emptyGame;
    TextView emptyEval;
    TextView emptyEvents;

    ImageView favIcon;
    TextView favTitle;
    TextView favType;
    TextView favCount;
    TextView favEmpty;
    RelativeLayout favLayout;

    ChartLegendAdapter evalAdapter;
    ChartLegendAdapter eventsAdapter;
    ChartLegendAdapter gamesAdapter;
    LinearLayout likeHeaderLayout;
    LinearLayout eventHeaderLayout;
    LinearLayout gameHeaderLayout;

    private String memberId;

    PieChart eventsChart;
    PieChart likesChart;
    PieChart gamesChart;

    private ToActivityListener callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();

        if (bundle != null){
            memberId = bundle.getString("bungieId");
            type = bundle.getInt("type");
        }

        Log.w(TAG, "Menu (1) and Detail (2) | Type: " + type);
        callback = (ToActivityListener) getActivity();
        if (type == TYPE_MENU) {
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        setHasOptionsMenu(true);
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
        titleText = (TextView) v.findViewById(R.id.title_text);
        favIcon = (ImageView) v.findViewById(R.id.game_icon);
        favTitle = (TextView) v.findViewById(R.id.game_title);
        favType = (TextView) v.findViewById(R.id.game_type);
        favCount = (TextView) v.findViewById(R.id.fav_game_played);
        favEmpty = (TextView) v.findViewById(R.id.empty_fav);
        favLayout = (RelativeLayout) v.findViewById(R.id.fav_layout);

        evaluationLegends = (ListView) v.findViewById(R.id.evaluation_list);
        evaluationLegends.setFocusable(false);
        emptyEval = (TextView) v.findViewById(R.id.empty_eval_chart);
        eventsLegends = (ListView) v.findViewById(R.id.events_list);
        eventsLegends.setFocusable(false);
        emptyEvents = (TextView) v.findViewById(R.id.empty_events_chart);
        gameLegends = (ListView) v.findViewById(R.id.games_list);
        gameLegends.setFocusable(false);
        emptyGame = (TextView) v.findViewById(R.id.empty_game_chart);

        likeHeaderLayout = (LinearLayout) v.findViewById(R.id.like_chart_header);
        eventHeaderLayout = (LinearLayout) v.findViewById(R.id.event_chart_header);
        gameHeaderLayout = (LinearLayout) v.findViewById(R.id.game_chart_header);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int chartHeight = metrics.widthPixels - 164;

        eventsChart = (PieChart) v.findViewById(R.id.events_chart);
        eventsChart.setMinimumHeight(chartHeight);
        eventsChart.setUsePercentValues(true);
        eventsChart.setDescription("");
        eventsChart.setExtraOffsets(5, 10, 5, 5);
        eventsChart.setDrawHoleEnabled(false);
        eventsChart.setRotationEnabled(false);
        eventsChart.setNoDataText(getString(R.string.no_events_played));
        Legend eventsLeg = eventsChart.getLegend();
        eventsLeg.setEnabled(false);

        likesChart = (PieChart) v.findViewById(R.id.evaluation_chart);
        likesChart.setMinimumHeight(chartHeight);
        likesChart.setUsePercentValues(true);
        likesChart.setDescription("");
        likesChart.setExtraOffsets(5, 10, 5, 5);
        likesChart.setDrawHoleEnabled(false);
        likesChart.setRotationEnabled(false);
        likesChart.setNoDataText(getString(R.string.no_events_played));
        Legend likesLeg = likesChart.getLegend();
        likesLeg.setEnabled(false);

        gamesChart = (PieChart) v.findViewById(R.id.game_chart);
        gamesChart.setMinimumHeight(chartHeight);
        gamesChart.setUsePercentValues(true);
        gamesChart.setDescription("");
        gamesChart.setExtraOffsets(5, 10, 5, 5);
        gamesChart.setDrawHoleEnabled(false);
        gamesChart.setRotationEnabled(false);
        gamesChart.setNoDataText(getString(R.string.no_events_played));
        Legend gamesLeg = gamesChart.getLegend();
        gamesLeg.setEnabled(false);

        getMemberData();

        return v;
    }

    private void getMemberData() {

        getLoaderManager().initLoader(LOADER_MEMBER, null, this);
        getLoaderManager().initLoader(LOADER_PROFILE, null, this);
        getLoaderManager().initLoader(LOADER_FAVORITE, null, this);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        callback.onLoadingData();

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
            case LOADER_PROFILE:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_PROFILE_URI,
                        getProfileProjection(),
                        EntryTable.COLUMN_MEMBERSHIP + "=" + memberId + " AND(" + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED + " OR " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_EVALUATED + ")",
                        null,
                        "type_total DESC"
                );
            case LOADER_FAVORITE:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_FAVORITE_URI,
                        getFavoriteProjection(),
                        EntryTable.COLUMN_MEMBERSHIP + "=" + memberId + " AND(" + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED + " OR " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_EVALUATED + ")",
                        null,
                        "total DESC"
                );
        }

        return null;
    }

    private String[] getFavoriteProjection() {

        String c1 = "COUNT(*) AS total";
        String c2 = EventTable.COLUMN_NAME;
        String c3 = EventTypeTable.COLUMN_NAME;
        String c4 = EventTable.COLUMN_ICON;

        return new String[] {c1, c2, c3, c4};

    }

    private String[] getProfileProjection() {

        //String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = "COUNT(*) AS type_total";
        String c3 = EventTypeTable.COLUMN_NAME;

        return new String[] {c2, c3};
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
        String c9 = MemberTable.COLUMN_TITLE;

        return new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //if (data != null && data.moveToFirst()){

            //Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

            switch (loader.getId()){
                case LOADER_MEMBER:
                    if (data != null && data.moveToFirst()){
                        emptyEval.setVisibility(View.GONE);
                        emptyEvents.setVisibility(View.GONE);
                        eventsChart.setVisibility(View.VISIBLE);
                        likesChart.setVisibility(View.VISIBLE);
                        likeHeaderLayout.setVisibility(View.VISIBLE);
                        eventHeaderLayout.setVisibility(View.VISIBLE);
                        try {
                            profilePic.setImageBitmap(ImageUtils.loadImage(getContext(), data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
                        } catch (IOException e) {
                            Log.e(TAG, "Image Not Found");
                            e.printStackTrace();
                        }
                        userName.setText(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                        titleText.setText(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));
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
                        //Log.w(TAG, "Likes: " + likes + "; Dislikes: " + dislikes);

                        setEventChart(created, played);
                        setLikesChart(likes, dislikes);
                    } else {
                        emptyEval.setVisibility(View.VISIBLE);
                        emptyEvents.setVisibility(View.VISIBLE);
                        eventsChart.setVisibility(View.GONE);
                        likesChart.setVisibility(View.GONE);
                        likeHeaderLayout.setVisibility(View.GONE);
                        eventHeaderLayout.setVisibility(View.GONE);
                    }
                    break;
                case LOADER_PROFILE:
                    if (data != null && data.moveToFirst()){
                        //Log.w(TAG, DatabaseUtils.dumpCursorToString(data));
                        emptyGame.setVisibility(View.GONE);
                        gamesChart.setVisibility(View.VISIBLE);
                        gameHeaderLayout.setVisibility(View.VISIBLE);
                        ArrayList<String> labels = new ArrayList<>();
                        ArrayList<Integer> values = new ArrayList<>();

                        data.moveToFirst();
                        for(int i=0;i<data.getCount();i++){
                            String text = getResources().getString(getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)),"string",getContext().getPackageName()));
                            labels.add(text);
                            int value = data.getInt(data.getColumnIndexOrThrow("type_total"));
                            values.add(value);
                            data.moveToNext();
                        }
                        setGamesChart(labels, values);
                    } else {
                        emptyGame.setVisibility(View.VISIBLE);
                        gamesChart.setVisibility(View.GONE);
                        gameHeaderLayout.setVisibility(View.GONE);
                    }
                    break;
                case LOADER_FAVORITE:
                    if (data != null && data.moveToFirst()){
                        favEmpty.setVisibility(View.GONE);
                        favLayout.setVisibility(View.VISIBLE);
                        Log.w(TAG, DatabaseUtils.dumpCursorToString(data));
                        favIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)),"drawable",getContext().getPackageName()));
                        int title = getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)),"string",getContext().getPackageName());
                        favTitle.setText(title);
                        title = getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)),"string",getContext().getPackageName());
                        favType.setText(title);
                        String count = " " + StringUtils.parseString(data.getInt(data.getColumnIndexOrThrow("total"))) + " ";
                        favCount.setText(count);
                    } else {
                        favEmpty.setVisibility(View.VISIBLE);
                        favLayout.setVisibility(View.GONE);
                    }

                    break;
            }

        callback.onDataLoaded();
        //}
    }

    private void setGamesChart(ArrayList<String> labels, ArrayList<Integer> values) {

        ArrayList<Entry> yValues = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        for(int i=0;i<values.size();i++){
            yValues.add(new Entry((float)values.get(i),i));
            titles.add("");
        }

        PieDataSet dataSet = new PieDataSet(yValues, "Games");
        if(values.size()>1) {
            dataSet.setSliceSpace(6f);
        } else dataSet.setSliceSpace(0f);;
        dataSet.setSelectionShift(12f);
        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

        PieData data = new PieData(titles,dataSet);
        data.setDrawValues(false);
        Highlight h = new Highlight(0,0);
        gamesChart.highlightValues(new Highlight[] {h});
        gamesChart.setData(data);
        gamesChart.invalidate();

        ArrayList<ChartLegendModel> legendList = new ArrayList<>();

        for (int i=0; i<yValues.size(); i++){
            ChartLegendModel model = new ChartLegendModel();
            model.setColor(dataSet.getColor(i));
            model.setTitle(labels.get(i));
            model.setValue((int) yValues.get(i).getVal());
            model.setPercent(getPercent(i,yValues));
            legendList.add(model);
        }

        gamesAdapter = new ChartLegendAdapter(getContext(), legendList);
        gameLegends.setAdapter(gamesAdapter);
        setListHeight(gameLegends);

    }

    private void setLikesChart(int likes, int dislikes) {
        int total = 0;
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        if (likes>0){
            labels.add("");
            titles.add(getResources().getString(R.string.likes));
            total++;
        }
        if (dislikes>0) {
            labels.add("");
            titles.add(getResources().getString(R.string.dislikes));
            total++;
        }

        if (total>0){
            ArrayList<Entry> yValues = new ArrayList<>();
            if (likes>0) yValues.add(new Entry((float)likes, 0));
            if (dislikes>0) yValues.add(new Entry((float)dislikes, total-1));

            PieDataSet dataSet = new PieDataSet(yValues, getResources().getString(R.string.evaluations));
            if (total>1) {
                dataSet.setSliceSpace(6f);
            } else dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(12f);
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

            PieData data = new PieData(labels, dataSet);
            data.setDrawValues(false);
            Highlight h = new Highlight(getBiggest(likes, dislikes),0);
            likesChart.highlightValues(new Highlight[] {h});
            likesChart.setData(data);
            likesChart.invalidate();

            ArrayList<ChartLegendModel> legendList = new ArrayList<>();

            for (int i=0; i<total; i++){
                ChartLegendModel model = new ChartLegendModel();
                model.setColor(dataSet.getColor(i));
                model.setTitle(titles.get(i));
                model.setValue((int) yValues.get(i).getVal());
                model.setPercent(getPercent(i,yValues));
                legendList.add(model);
            }

            evalAdapter = new ChartLegendAdapter(getContext(), legendList);
            evaluationLegends.setAdapter(evalAdapter);
            setListHeight(evaluationLegends);

        }

    }

    private void setListHeight(ListView list) {
        int numberOfItems = list.getAdapter().getCount();
        int dividersHeight = list.getDividerHeight() * numberOfItems - 1;

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = (numberOfItems * (int) getResources().getDimension(R.dimen.smallItemHeight)) + dividersHeight;
        list.setLayoutParams(params);
        list.requestLayout();
    }

    private int getPercent(int position, ArrayList<Entry> yValues) {

        int total =0;

        for (int i=0;i<yValues.size();i++){
            total = total + (int) yValues.get(i).getVal();
        }

        float result = (100 * yValues.get(position).getVal())/((float)total);

        return Math.round(result);

    }


    private void setEventChart(int created, int played) {
        int total = 0;
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        if (created>0){
            labels.add("");
            titles.add(getResources().getString(R.string.created));
            total++;
        }
        if (played>0) {
            labels.add("");
            titles.add(getResources().getString(R.string.played));
            total++;
        }

        if (total>0){
            ArrayList<Entry> yValues = new ArrayList<>();
            if (created>0) yValues.add(new Entry((float)created, 0));
            if (played>0) yValues.add(new Entry((float)played, total-1));

            PieDataSet dataSet = new PieDataSet(yValues, getResources().getString(R.string.events));
            if (total>1){
                dataSet.setSliceSpace(6f);
            } else dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(12f);
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

            PieData data = new PieData(labels, dataSet);
            data.setDrawValues(false);
            Highlight h = new Highlight(getBiggest(created, played),0);
            eventsChart.highlightValues(new Highlight[] {h});
            eventsChart.setData(data);
            eventsChart.invalidate();

            ArrayList<ChartLegendModel> legendList = new ArrayList<>();

            for (int i=0; i<total; i++){
                ChartLegendModel model = new ChartLegendModel();
                model.setColor(dataSet.getColor(i));
                model.setTitle(titles.get(i));
                model.setValue((int) yValues.get(i).getVal());
                model.setPercent(getPercent(i,yValues));
                legendList.add(model);
            }

            eventsAdapter = new ChartLegendAdapter(getContext(), legendList);
            eventsLegends.setAdapter(eventsAdapter);
            setListHeight(eventsLegends);

        }

    }

    public int getBiggest(int value1, int value2){
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
        callback.setToolbarTitle(getString(R.string.my_profile));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

}
