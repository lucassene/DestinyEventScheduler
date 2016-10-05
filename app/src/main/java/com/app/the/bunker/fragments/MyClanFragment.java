package com.app.the.bunker.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.adapters.CustomCursorAdapter;
import com.app.the.bunker.data.ClanTable;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.interfaces.SwipeListener;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.ImageUtils;
import com.app.the.bunker.utils.NetworkUtils;
import com.app.the.bunker.views.CustomSwipeLayout;

import java.io.IOException;
import java.util.ArrayList;

public class MyClanFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, SwipeListener {

    private static final String TAG = "MyClanFragment";

    private static final int LOADER_MEMBERS = 50;
    private static final int LOADER_CLAN = 40;

    private static final String NAME_ORDER_BY = MemberTable.COLUMN_NAME + " COLLATE NOCASE ASC";
    private static final String POINTS_ORDER_BY = MemberTable.COLUMN_EXP + " DESC, " + MemberTable.COLUMN_NAME + " COLLATE NOCASE ASC";
    private String orderBy;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_EXP, MemberTable.COLUMN_TITLE};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points, R.id.secondary_text};

    private ArrayList<String> bungieIdList;

    TextView clanNameTxt;
    TextView clanDesc;
    ImageView clanLogo;
    ImageView clanBanner;
    CustomSwipeLayout swipeLayout;
    Spinner orderSpinner;
    View headerView;
    TextView totalMembers;
    CustomCursorAdapter adapter;

    private ToActivityListener callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_clan_layout, container, false);

        headerView = inflater.inflate(R.layout.my_clan_header_layout, null);
        swipeLayout = (CustomSwipeLayout) v.findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        bungieIdList = new ArrayList<>();
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        return v;
    }

    private void refreshList() {
        if (NetworkUtils.checkConnection(getContext())){
            if (bungieIdList != null) callback.updateClan(bungieIdList);
            swipeLayout.setRefreshing(true);
        } else {
            swipeLayout.setRefreshing(false);
            Toast.makeText(getContext(), R.string.check_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View myClanLayout = headerView.findViewById(R.id.myclan_layout);

        clanNameTxt = (TextView) myClanLayout.findViewById(R.id.clan_name);
        clanDesc = (TextView) myClanLayout.findViewById(R.id.clan_desc);
        clanLogo = (ImageView) myClanLayout.findViewById(R.id.clan_logo);
        clanBanner = (ImageView) myClanLayout.findViewById(R.id.clan_banner);

        totalMembers = (TextView) headerView.findViewById(R.id.total_members);
        orderSpinner = (Spinner) headerView.findViewById(R.id.order_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.clan_order_by));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        orderSpinner.setOnItemSelectedListener(this);
        orderSpinner.setAdapter(spinnerAdapter);

        if (callback.getOrderBy() != null){
            orderBy = callback.getOrderBy();
        } else orderBy = POINTS_ORDER_BY;

        switch (orderBy){
            case NAME_ORDER_BY:
                orderSpinner.setSelection(0);
                break;
            case POINTS_ORDER_BY:
                orderSpinner.setSelection(1);
                break;
        }

        setClanData();
    }

    private void setClanData() {

        Bundle bundle = getArguments();

        clanNameTxt.setText(bundle.getString("clanName"));
        clanDesc.setText(bundle.getString("clanDesc"));
        try {
            clanLogo.setImageBitmap(ImageUtils.loadImage(getContext(),bundle.getString("clanIcon")));
            clanBanner.setImageBitmap(ImageUtils.loadImage(getContext(), bundle.getString("clanBanner")));
        } catch (IOException e){
            Log.w(TAG, "Clan Logo not found");
            e.printStackTrace();
        }

        initMemberLoader();
        adapter = new CustomCursorAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, LOADER_MEMBERS);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.my_clan));
        getActivity().getMenuInflater().inflate(R.menu.share_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        String bungieId = bungieIdList.get(position - 1);

        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putInt("type", MyNewProfileFragment.TYPE_DETAIL);

        callback.loadNewFragment(fragment, bundle, "profile");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        callback.onLoadingData();

        switch (id){
            case LOADER_MEMBERS:
                return new CursorLoader(
                        getContext(),
                        DataProvider.MEMBER_URI,
                        MemberTable.ALL_COLUMNS,
                        null,
                        null,
                        orderBy
                );
            case LOADER_CLAN:
                return new CursorLoader(
                        getContext(),
                        DataProvider.CLAN_URI,
                        ClanTable.ALL_COLUMNS,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_MEMBERS:
                    totalMembers.setText(String.valueOf(data.getCount()));
                    for (int i=0; i<data.getCount(); i++){
                        bungieIdList.add(i,data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                        data.moveToNext();
                    }
                    adapter.swapCursor(data);
                    break;
                case LOADER_CLAN:
                    clanNameTxt.setText(data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_NAME)));
                    clanDesc.setText(data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_DESC)));
                    Log.w(TAG, "clanDesc: " + clanDesc.getText());
                    try {
                        clanLogo.setImageBitmap(ImageUtils.loadImage(getContext(),data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_ICON))));
                        clanBanner.setImageBitmap(ImageUtils.loadImage(getContext(), data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_BACKGROUND))));
                    } catch (IOException e){
                        Log.w(TAG, "Clan Logo not found");
                        e.printStackTrace();
                    }
                    break;
            }
        }
        callback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                orderBy = NAME_ORDER_BY;
                getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
                callback.setClanOrderBy(orderBy);
                break;
            case 1:
                orderBy = POINTS_ORDER_BY;
                getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
                callback.setClanOrderBy(orderBy);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void initMemberLoader(){
        if (getLoaderManager().getLoader(LOADER_MEMBERS) != null){
            getLoaderManager().destroyLoader(LOADER_MEMBERS);
        }
        getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
    }

    private void initClanLoader(){
        if (getLoaderManager().getLoader(LOADER_CLAN) != null){
            getLoaderManager().destroyLoader(LOADER_CLAN);
        }
        getLoaderManager().restartLoader(LOADER_CLAN, null, this);
    }


    public ArrayList<String> getBungieIdList(){
        return bungieIdList;
    }

    public void refreshData(){
        initClanLoader();
        adapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void toggleSwipeProgress(boolean b) {
        swipeLayout.setRefreshing(b);
    }
}
