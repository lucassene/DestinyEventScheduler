package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.DetailHistoryAdapter;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DetailHistoryFragment extends ListFragment implements UserDataListener{

    private static final String TAG = "DetailHistoryFragment";

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;
    TextView sectionTitle;

    TextView date;
    TextView time;
    TextView light;
    TextView guardians;

    private ToActivityListener callback;

    DetailHistoryAdapter adapter;

    ArrayList<MemberModel> historyEntries;

    GameModel game;
    int inscriptions;
    int maxGuardians;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.event_details);
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        callback.registerUserDataListener(this);

        headerView = inflater.inflate(R.layout.detail_header_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        date = (TextView) headerView.findViewById(R.id.date);
        time = (TextView) headerView.findViewById(R.id.time);
        light = (TextView) headerView.findViewById(R.id.light);
        guardians = (TextView) headerView.findViewById(R.id.guardians);
        sectionTitle = (TextView) headerView.findViewById(R.id.section_guardians);
        sectionTitle.setText(R.string.participants_guardians);

        Bundle bundle = getArguments();
        if (bundle != null) {
            game = (GameModel) bundle.getSerializable("game");
        }

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historyEntries = new ArrayList<>();
        adapter = new DetailHistoryAdapter(getContext(), historyEntries);
        getListView().setAdapter(adapter);

        if (headerView != null){
            this.getListView().addHeaderView(headerView, null, false);
        }
        getGameData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", historyEntries.get(position-1).getMembershipId());
        bundle.putInt("type", MyNewProfileFragment.TYPE_DETAIL);

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void getGameData() {
        eventIcon.setImageResource(getContext().getResources().getIdentifier(game.getEventIcon(),"drawable",getContext().getPackageName()));
        eventName.setText(getContext().getResources().getIdentifier(game.getEventName(),"string",getContext().getPackageName()));
        eventType.setText(getContext().getResources().getIdentifier(game.getTypeName(),"string",getContext().getPackageName()));

        String gameTime = game.getTime();
        date.setText(DateUtils.onBungieDate(gameTime));
        time.setText(DateUtils.getTime(gameTime));
        light.setText(String.valueOf(game.getMinLight()));

        maxGuardians = game.getMaxGuardians();
        inscriptions = game.getInscriptions();
        String sg = inscriptions + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
        guardians.setText(sg);

        if (historyEntries == null || historyEntries.size() == 0){
            Log.w(TAG, "historyEntries size = 0");
            callback.getGameHistory(game.getGameId());
        } else {
            Log.w(TAG, "historyEntries size > 0");
            onEntriesLoaded(historyEntries, false);
        }

    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded) {
        if (entryList != null){
            Log.w(TAG, "historyEntries size: " + entryList.size());
            this.historyEntries = (ArrayList<MemberModel>) entryList;
            String sg = entryList.size() + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
            guardians.setText(sg);
            setAdapter(this.historyEntries);
        }
    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {

    }

    private void setAdapter(List<MemberModel> entries) {
        setListAdapter(null);
        adapter = new DetailHistoryAdapter(getContext(),entries);
        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerUserDataListener(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.event_details));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onUserDataLoaded() {

    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {

    }


}