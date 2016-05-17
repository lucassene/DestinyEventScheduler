package com.destiny.event.scheduler.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.SimpleMemberAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.SimpleMemberModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ValidateFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener{

    private static final String TAG = "ValidateFragment";

    private static final int LOADER_GAME = 60;
    private static final int LOADER_ENTRY_MEMBERS = 72;

    private String gameId;
    private String origin;
    private String gameStatus;
    private String creator;
    private int inscriptions;
    private int maxGuardians;

    private ArrayList<String> bungieIdList;

    private List<SimpleMemberModel> memberList;

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

    View footerView;
    Button joinButton;

    private ToActivityListener callback;

    private String[] gameProjection;
    private String[] membersProjection;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_EXP};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points};

    SimpleMemberAdapter adapter;

    MyAlertDialog dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callback = (ToActivityListener) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Validar Partida");
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        headerView = inflater.inflate(R.layout.detail_header_layout, null);
        footerView = inflater.inflate(R.layout.detail_footer_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        date = (TextView) headerView.findViewById(R.id.date);
        time = (TextView) headerView.findViewById(R.id.time);
        light = (TextView) headerView.findViewById(R.id.light);
        guardians = (TextView) headerView.findViewById(R.id.guardians);
        sectionTitle = (TextView) headerView.findViewById(R.id.section_guardians);

        joinButton = (Button) footerView.findViewById(R.id.btn_join);

        Bundle bundle = getArguments();
        if (bundle != null){
            gameId = bundle.getString("gameId");
            creator = bundle.getString("creator");
        }

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (origin){
                    case NewEventsListFragment.TAG:
                        showAlertDialog(MyAlertDialog.JOIN_DIALOG);
                        break;
                    case ScheduledListFragment.TAG:
                        if (creator.equals(callback.getBungieId())){
                            showAlertDialog(MyAlertDialog.DELETE_DIALOG);
                        } else showAlertDialog(MyAlertDialog.LEAVE_DIALOG);
                        break;
                    case SearchFragment.TAG:
                        showAlertDialog(MyAlertDialog.JOIN_DIALOG);
                        break;
                    case MyEventsFragment.TAG:
                        showAlertDialog(MyAlertDialog.LEAVE_DIALOG);
                        break;
                }
            }
        });

        bungieIdList = new ArrayList<>();
        memberList = new ArrayList<>();

        sectionTitle.setText("Guardiões participantes");

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView, null, false);
            this.getListView().addFooterView(footerView);
        }

        getGameData();

    }

    private void showAlertDialog(int dialogType) {

        final String title = "title";
        final String msg = "msg";
        final String posButton = "posButton";

        Bundle bundle = new Bundle();
        switch (dialogType){
            case MyAlertDialog.LEAVE_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.leave_game_title));
                bundle.putString(msg, getContext().getResources().getString(R.string.leave_dialog_msg));
                bundle.putString(posButton, getContext().getResources().getString(R.string.leave));
                break;
            case MyAlertDialog.DELETE_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.delete_dialog_title));
                bundle.putString(msg, getContext().getResources().getString(R.string.delete_dialog_msg));
                bundle.putString(posButton, getContext().getResources().getString(R.string.delete));
                break;
            case MyAlertDialog.JOIN_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.join_dialog_title));
                bundle.putString(posButton, getContext().getResources().getString(R.string.join));
                if (inscriptions > maxGuardians){
                    bundle.putString(msg, getContext().getResources().getString(R.string.join_full_dialog_msg));
                } else bundle.putString(msg, getContext().getResources().getString(R.string.join_dialog_msg));
                break;
        }

        bundle.putInt("type", dialogType);

        dialog = new MyAlertDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(),"dialog");

    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        ImageView img = (ImageView) v.findViewById(R.id.rate_img);

        int newpos = position -1;
        SimpleMemberModel member = adapter.getItem(newpos);
        int newRating = 0;

        if (!member.getMembershipId().equals(callback.getBungieId())){
            if (member.isChecked()){
                switch (member.getRating()){
                    case -1:
                        newRating = 0;
                        member.setChecked(false);
                        img.setImageResource(R.drawable.ic_error);
                        img.setVisibility(View.VISIBLE);
                        AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
                        anim.setDuration(250);
                        anim.setFillAfter(true);
                        v.startAnimation(anim);
                        break;
                    case 0:
                        newRating = 1;
                        img.setImageResource(R.drawable.ic_like);
                        img.setVisibility(View.VISIBLE);
                        //img.setColorFilter(R.color.psnColor, PorterDuff.Mode.SRC_IN);
                        break;
                    case 1:
                        newRating = -1;
                        img.setImageResource(R.drawable.ic_dislike);
                        img.setVisibility(View.VISIBLE);
                        //img.setColorFilter(R.color.redFilter, PorterDuff.Mode.SRC_IN);
                        break;
                }
            } else {
                member.setChecked(true);
                img.setVisibility(View.GONE);
                AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
                anim.setDuration(250);
                anim.setFillAfter(true);
                v.startAnimation(anim);
            }
        }

        adapter.setRating(newpos, newRating);

    }

    private void getGameData() {

        callback.onLoadingData();
        prepareGameStrings();
        getLoaderManager().initLoader(LOADER_GAME, null, this);
    }

    private void prepareGameStrings() {

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c2 = GameTable.COLUMN_EVENT_ID;
        String c6 = GameTable.COLUMN_CREATOR;
        String c9 = GameTable.COLUMN_TIME;
        String c10 = GameTable.COLUMN_LIGHT;
        String c12 = GameTable.COLUMN_INSCRIPTIONS;
        String c14 = GameTable.COLUMN_CREATOR_NAME;

        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.COLUMN_NAME;
        String c11 = EventTable.COLUMN_GUARDIANS;
        String c15 = EventTable.COLUMN_TYPE;

        String c7 = MemberTable.COLUMN_MEMBERSHIP;
        String c8 = MemberTable.COLUMN_NAME;

        String c16 = EventTypeTable.COLUMN_ICON;
        String c17 = EventTypeTable.COLUMN_NAME;

        gameProjection = new String[] {c1, c2, c4, c5, c6, c7, c8, c9, c10, c11, c12,  c14, c15, c16, c17};

    }

    private void prepareMemberStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.COLUMN_GAME;
        String c3 = EntryTable.COLUMN_MEMBERSHIP;
        String c4 = EntryTable.COLUMN_TIME;

        String c5 = MemberTable.COLUMN_MEMBERSHIP;
        String c6 = MemberTable.COLUMN_NAME;
        String c7 = MemberTable.COLUMN_ICON;
        String c8 = MemberTable.COLUMN_LIKES;
        String c9 = MemberTable.COLUMN_DISLIKES;
        String c10 = MemberTable.COLUMN_CREATED;
        String c11 = MemberTable.COLUMN_PLAYED;

        String c14 = MemberTable.COLUMN_EXP;

        membersProjection = new String[] {c1, c2, c3, c4, c5, c6, c14, c7, c8, c9, c10, c11};

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

        String[] selectionArgs = {gameId};

        switch (id){
            case LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
                        gameProjection,
                        GameTable.getQualifiedColumn(GameTable.COLUMN_ID)+ "=?",
                        selectionArgs,
                        null
                );
            case LOADER_ENTRY_MEMBERS:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_MEMBERS_URI,
                        membersProjection,
                        EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME) + "=?",
                        selectionArgs,
                        "datetime(" + EntryTable.getQualifiedColumn(EntryTable.COLUMN_TIME) + ") ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_GAME:
                    eventIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", getContext().getPackageName() ));

                    String gameEventName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventName.setText(gameEventName);

                    String gameEventTypeName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventType.setText(gameEventTypeName);

                    date.setText(DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    time.setText(DateUtils.getTime(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    light.setText(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_LIGHT)));

                    maxGuardians = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS));
                    inscriptions = data.getInt(data.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));
                    String sg = inscriptions + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
                    guardians.setText(sg);

                    prepareMemberStrings();
                    getLoaderManager().initLoader(LOADER_ENTRY_MEMBERS, null, this);

                    break;
                case LOADER_ENTRY_MEMBERS:

                    memberList.clear();
                    data.moveToFirst();
                    for (int i=0; i < data.getCount();i++){
                        SimpleMemberModel memberModel = new SimpleMemberModel();
                        memberModel.setMembershipId(data.getString(data.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                        memberModel.setName(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                        memberModel.setIcon(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                        memberModel.setRating(0);
                        memberModel.setChecked(true);
                        memberList.add(memberModel);
                        bungieIdList.add(i, data.getString(data.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                        data.moveToNext();
                    }

                    adapter = new SimpleMemberAdapter(getContext(), memberList);
                    getListView().setAdapter(adapter);
                    break;
            }
        }

        callback.onDataLoaded();

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onPositiveClick(String input, int type) {

        ContentValues values = new ContentValues();
        String uriString = DataProvider.GAME_URI + "/" + gameId;
        Uri uri = Uri.parse(uriString);

    }

    @Override
    public void onDateSent(Calendar date) {

    }

    @Override
    public void onTimeSent(int hour, int minute) {

    }

    @Override
    public void onLogoff() {

    }
}