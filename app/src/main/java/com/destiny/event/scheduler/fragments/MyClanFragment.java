package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
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

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MyClanFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private static final String TAG = "MyClanFragment";

    private static final int LOADER_MEMBERS = 50;

    private static final String DATE_ORDER_BY = MemberTable.COLUMN_SINCE + " ASC";
    private static final String NAME_ORDER_BY = MemberTable.COLUMN_NAME + " ASC";
    private static final String POINTS_ORDER_BY = MemberTable.POINTS_COLUMNS + " DESC";
    private String orderBy;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_SINCE};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points};

    private ArrayList<String> bungieIdList;
    private String bungieId;

    TextView clanNameTxt;
    TextView clanDesc;
    ImageView clanLogo;
    ImageView clanBanner;

    private String clanName;

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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.my_clan);

        View v = inflater.inflate(R.layout.my_clan_layout, container, false);

        headerView = inflater.inflate(R.layout.my_clan_header_layout, null);

        bungieIdList = new ArrayList<>();

        return v;
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
        } else orderBy = NAME_ORDER_BY;

        switch (orderBy){
            case NAME_ORDER_BY:
                orderSpinner.setSelection(0);
                break;
            case DATE_ORDER_BY:
                orderSpinner.setSelection(1);
                break;
            case POINTS_ORDER_BY:
                orderSpinner.setSelection(2);
                break;
        }

        setClanData();
    }

    private void setClanData() {

        Bundle bundle = getArguments();

        clanNameTxt.setText(bundle.getString("clanName"));
        clanName = bundle.getString("clanName");
        clanDesc.setText(bundle.getString("clanDesc"));
        try {
            clanLogo.setImageBitmap(ImageUtils.loadImage(getContext(),bundle.getString("clanIcon")));
            clanBanner.setImageBitmap(ImageUtils.loadImage(getContext(), bundle.getString("clanBanner")));
        } catch (IOException e){
            Log.w(TAG, "Clan Logo not found");
            e.printStackTrace();
        }

        getLoaderManager().initLoader(LOADER_MEMBERS, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, LOADER_MEMBERS);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
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
    public void onListItemClick(ListView l, View v, int position, long id) {

        bungieId = bungieIdList.get(position-1);

        Fragment fragment = new MyProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putInt("type", MyProfileFragment.TYPE_MEMBER);
        bundle.putString("clanName", clanName);

        callback.loadNewFragment(fragment, bundle, "profile");

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection;

        switch (id){
            case LOADER_MEMBERS:
                projection = new String[]{MemberTable.COLUMN_ID, MemberTable.COLUMN_NAME, MemberTable.COLUMN_MEMBERSHIP, MemberTable.COLUMN_CLAN, MemberTable.COLUMN_ICON, MemberTable.COLUMN_PLATFORM, MemberTable.POINTS_COLUMNS, MemberTable.COLUMN_SINCE};
                return new CursorLoader(
                        getContext(),
                        DataProvider.MEMBER_URI,
                        projection,
                        null,
                        null,
                        orderBy
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();

        switch (loader.getId()){
            case LOADER_MEMBERS:
                adapter.swapCursor(data);
                totalMembers.setText(String.valueOf(data.getCount()));
                for (int i=0; i<data.getCount(); i++){
                    bungieIdList.add(i,data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                    data.moveToNext();
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.w("MyClan Loader: ", "O Loader entrou no método onLoaderReset");

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                orderBy = NAME_ORDER_BY;
                getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
                callback.setClanOrderBy(orderBy);
                break;
            case 1:
                orderBy = DATE_ORDER_BY;
                getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
                callback.setClanOrderBy(orderBy);
                break;
            case 2:
                orderBy = POINTS_ORDER_BY;
                getLoaderManager().restartLoader(LOADER_MEMBERS, null, this);
                callback.setClanOrderBy(orderBy);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
