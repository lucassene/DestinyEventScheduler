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
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;

public class MyClanFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MyClanFragment";

    private static final int LOADER_CLAN = 40;
    private static final int LOADER_MEMBERS = 50;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_SINCE, MemberTable.COLUMN_CREATED, MemberTable.COLUMN_PLAYED, MemberTable.COLUMN_LIKES, MemberTable.COLUMN_DISLIKES};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points};

    TextView clanName;
    TextView clanDesc;
    ImageView clanLogo;

    TextView totalMembers;
    ListView memberList;

    CustomCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.my_clan);

        View v = inflater.inflate(R.layout.my_clan_layout, container, false);
        View myClanLayout = v.findViewById(R.id.myclan_layout);

        clanName = (TextView) myClanLayout.findViewById(R.id.clan_name);
        clanDesc = (TextView) myClanLayout.findViewById(R.id.clan_desc);
        clanLogo = (ImageView) myClanLayout.findViewById(R.id.clan_logo);

        totalMembers = (TextView) v.findViewById(R.id.total_members);
        memberList = (ListView) v.findViewById(R.id.clan_list);

        getClanData();

        return v;
    }

    private void getClanData() {
        getLoaderManager().initLoader(LOADER_CLAN, null, this);
        getLoaderManager().initLoader(LOADER_MEMBERS, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, LOADER_MEMBERS);
        memberList.setAdapter(adapter);
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
            case LOADER_CLAN:
                projection = ClanTable.ALL_COLUMNS;
                return new CursorLoader(
                        getContext(),
                        DataProvider.CLAN_URI,
                        projection,
                        null,
                        null,
                        null
                );
            case LOADER_MEMBERS:
                projection = MemberTable.ALL_COLUMNS;
                return new CursorLoader(
                        getContext(),
                        DataProvider.MEMBER_URI,
                        projection,
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

        data.moveToFirst();

        switch (loader.getId()){
            case LOADER_CLAN:
                clanName.setText(data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_NAME)));
                clanDesc.setText(data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_DESC)));
                try {
                    clanLogo.setImageBitmap(ImageUtils.loadImage(getContext(),data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_ICON))));
                } catch (IOException e){
                    Log.w(TAG, "Clan Logo not found");
                    e.printStackTrace();
                }

                break;
            case LOADER_MEMBERS:
                adapter.swapCursor(data);
                totalMembers.setText(String.valueOf(data.getCount()));
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.w("MyClan Loader: ", "O Loader entrou no método onLoaderReset");

    }
}
