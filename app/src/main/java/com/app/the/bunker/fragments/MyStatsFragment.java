package com.app.the.bunker.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.adapters.ChartLegendAdapter;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.models.ChartLegendModel;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.utils.ImageUtils;
import com.app.the.bunker.utils.StringUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.util.ArrayList;

public class MyStatsFragment extends Fragment{

    private static final String TAG = "MyStatsFragment";

    ImageView profilePic;
    TextView userName;
    TextView titleText;
    TextView memberLevel;
    ProgressBar progressBar;
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

    LinearLayout likesLayout;
    LinearLayout gamesLayout;
    LinearLayout typesLayout;

    private MemberModel member;

    PieChart eventsChart;
    PieChart likesChart;
    PieChart gamesChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        if (bundle != null){
            member = (MemberModel) bundle.getSerializable("member");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_stats_layout, container, false);

        profilePic = (ImageView) v.findViewById(R.id.profile_pic);
        userName = (TextView) v.findViewById(R.id.primary_text);
        memberLevel = (TextView) v.findViewById(R.id.member_level);
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        titleText = (TextView) v.findViewById(R.id.title_text);
        favIcon = (ImageView) v.findViewById(R.id.game_icon);
        favTitle = (TextView) v.findViewById(R.id.game_title);
        favType = (TextView) v.findViewById(R.id.game_type);
        favCount = (TextView) v.findViewById(R.id.fav_game_played);
        favEmpty = (TextView) v.findViewById(R.id.empty_fav);
        favLayout = (RelativeLayout) v.findViewById(R.id.fav_layout);
        likesLayout = (LinearLayout) v.findViewById(R.id.likes_layout);
        gamesLayout = (LinearLayout) v.findViewById(R.id.games_layout);
        typesLayout = (LinearLayout) v.findViewById(R.id.type_layout);

        evaluationLegends = (ListView) v.findViewById(R.id.evaluation_list);
        evaluationLegends.setFocusable(false);
        evaluationLegends.setClickable(false);
        evaluationLegends.setEnabled(false);
        emptyEval = (TextView) v.findViewById(R.id.empty_eval_chart);
        eventsLegends = (ListView) v.findViewById(R.id.events_list);
        eventsLegends.setFocusable(false);
        eventsLegends.setClickable(false);
        eventsLegends.setEnabled(false);
        emptyEvents = (TextView) v.findViewById(R.id.empty_events_chart);
        gameLegends = (ListView) v.findViewById(R.id.games_list);
        gameLegends.setFocusable(false);
        gameLegends.setClickable(false);
        gameLegends.setEnabled(false);
        emptyGame = (TextView) v.findViewById(R.id.empty_game_chart);

        likeHeaderLayout = (LinearLayout) v.findViewById(R.id.like_chart_header);
        eventHeaderLayout = (LinearLayout) v.findViewById(R.id.event_chart_header);
        gameHeaderLayout = (LinearLayout) v.findViewById(R.id.game_chart_header);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int chartHeight;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            chartHeight = metrics.widthPixels - 264;
        } else {
            chartHeight = metrics.widthPixels - 694;
        }

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

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (member != null){
            userName.setText(member.getName());
            try {
                String iconName = member.getIconPath().substring(member.getIconPath().lastIndexOf("/")+1,member.getIconPath().length());
                profilePic.setImageBitmap(ImageUtils.loadImage(getContext(),iconName));
            } catch (IOException e) {
                Log.e(TAG, "Image Not Found");
                e.printStackTrace();
            }
            int xp = MemberTable.getMemberXP(member.getLikes(), member.getDislikes(), member.getGamesPlayed(), member.getGamesCreated());
            memberLevel.setText(StringUtils.parseString(MemberTable.getMemberLevel(xp)));
            progressBar.setMax(MemberTable.getExpNeeded(xp));
            progressBar.setProgress(xp);
            titleText.setText(member.getTitle());

            int likes = member.getLikes();
            int dislikes = member.getDislikes();

            if (likes != 0 || dislikes != 0){
                emptyEval.setVisibility(View.GONE);
                likesChart.setVisibility(View.VISIBLE);
                likeHeaderLayout.setVisibility(View.VISIBLE);
                likesLayout.setVisibility(View.VISIBLE);
                setLikesChart(likes, dislikes);
            } else {
                emptyEval.setVisibility(View.VISIBLE);
                likesChart.setVisibility(View.GONE);
                likeHeaderLayout.setVisibility(View.GONE);
                likesLayout.setVisibility(View.GONE);
            }

            int created = member.getGamesCreated();
            int played = member.getGamesPlayed();

            if (created != 0 || played != 0){
                emptyEvents.setVisibility(View.GONE);
                eventsChart.setVisibility(View.VISIBLE);
                eventHeaderLayout.setVisibility(View.VISIBLE);
                gamesLayout.setVisibility(View.VISIBLE);
                setEventChart(created, played);
            } else {
                emptyEvents.setVisibility(View.VISIBLE);
                eventsChart.setVisibility(View.GONE);
                eventHeaderLayout.setVisibility(View.GONE);
                gamesLayout.setVisibility(View.GONE);
            }

            if (member.getTypesPlayed().size() > 0){
                emptyGame.setVisibility(View.GONE);
                gamesChart.setVisibility(View.VISIBLE);
                gameHeaderLayout.setVisibility(View.VISIBLE);
                typesLayout.setVisibility(View.VISIBLE);
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Integer> values = new ArrayList<>();
                for(int i=0;i<member.getTypesPlayed().size();i++){
                    String text = member.getTypesPlayed().get(i).getTypeName();
                    labels.add(text);
                    int value = member.getTypesPlayed().get(i).getTimesPlayed();
                    values.add(value);
                }
                setGamesChart(labels, values);
            } else {
                eventHeaderLayout.setVisibility(View.GONE);
                emptyGame.setVisibility(View.VISIBLE);
                gamesChart.setVisibility(View.GONE);
                gameHeaderLayout.setVisibility(View.GONE);
                typesLayout.setVisibility(View.GONE);
            }

            Log.w(TAG, "favoriteEvent: " + member.getFavoriteEvent().getEventId());
            if (member.getFavoriteEvent().getEventId() != 0){
                favEmpty.setVisibility(View.GONE);
                favLayout.setVisibility(View.VISIBLE);
                setViewIcon(favIcon, getContext().getResources().getIdentifier(member.getFavoriteEvent().getEventIcon(),"drawable",getContext().getPackageName()));
                favTitle.setText(member.getFavoriteEvent().getEventName());
                favType.setText(member.getFavoriteEvent().getEventType().getTypeName());
                int count = member.getFavoriteEvent().getTimesPlayed();
                String countText;
                if (count == 1){
                    countText = " " + StringUtils.parseString(count) + " " + getString(R.string.one_time);
                } else countText = " " + StringUtils.parseString(count) + " " + getString(R.string.more_times);
                favCount.setText(countText);
            } else {
                favEmpty.setVisibility(View.VISIBLE);
                favLayout.setVisibility(View.GONE);
            }
        } else {
            emptyEval.setVisibility(View.VISIBLE);
            emptyEvents.setVisibility(View.VISIBLE);
            eventsChart.setVisibility(View.GONE);
            likesChart.setVisibility(View.GONE);
            likeHeaderLayout.setVisibility(View.GONE);
            eventHeaderLayout.setVisibility(View.GONE);
            emptyGame.setVisibility(View.VISIBLE);
            gamesChart.setVisibility(View.GONE);
            gameHeaderLayout.setVisibility(View.GONE);
            favEmpty.setVisibility(View.VISIBLE);
            favLayout.setVisibility(View.GONE);
        }
    }

    private void setViewText(TextView view, int resId){
        if (resId != 0){
            view.setText(resId);
        } else {
            Log.w(TAG, "String resource not found.");
            view.setText(R.string.unknown);
        }
    }

    private void setViewIcon(ImageView view, int resId){
        if (resId != 0){
            view.setImageResource(resId);
        } else {
            Log.w(TAG, "Drawable resource not found.");
            view.setImageResource(R.drawable.ic_missing);
        }
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
        } else dataSet.setSliceSpace(0f);
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
            titles.add(getString(R.string.played_plural));
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

    public int getBiggest(int value1, int value2){
        if (value1 >= value2 || (value1 == 0 || value2 == 0)){
            return 0;
        } else return 1;
    }


}
