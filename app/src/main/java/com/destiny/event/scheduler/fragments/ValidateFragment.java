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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.SimpleMemberAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.MembersModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class ValidateFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener{

    private static final String TAG = "ValidateFragment";

    private static final int LOADER_GAME = 60;
    private static final int LOADER_ENTRY_MEMBERS = 72;
    private static final int LOADER_EVALUATION = 90;

    private static final int TYPE_ONLY_CREATOR = 1;
    private static final int TYPE_NO_EVALUATIONS = 2;
    private static final int TYPE_OK = 3;
    private static final int TYPE_DELETE = 4;

    private String gameId;
    private String creator;
    private int selectedType;
    private int inscriptions;

    private List<MembersModel> memberList;

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;

    TextView time;
    CheckBox checkBox;
    LinearLayout checkLayout;

    boolean listStatus = true;

    View footerView;
    Button joinButton;

    private ToActivityListener callback;

    private String[] gameProjection;
    private String[] membersProjection;

    SimpleMemberAdapter adapter;

    MyAlertDialog dialog;

    String originStatus;
    int status;

    private static final int STATUS_WAITING_CREATOR = 1;
    private static final int STATUS_WAITING = 0;
    private static final int STATUS_VALIDATED = 2;
    private static final int STATUS_EVALUATED = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        headerView = inflater.inflate(R.layout.validate_header_layout, null);
        footerView = inflater.inflate(R.layout.detail_footer_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        time = (TextView) headerView.findViewById(R.id.time_text);
        checkBox = (CheckBox) headerView.findViewById(R.id.confirm_check);
        checkLayout = (LinearLayout) headerView.findViewById(R.id.checkbox_layout);

        joinButton = (Button) footerView.findViewById(R.id.btn_join);

        joinButton.setText(R.string.validate);

        Bundle bundle = getArguments();
        if (bundle != null){
            gameId = bundle.getString("gameId");
            creator = bundle.getString("creator");
            originStatus = bundle.getString("status");
        }

        prepareViews();

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCheckedMembers()==1){
                    showAlertDialog(TYPE_ONLY_CREATOR);
                } else if (getEvaluatedMembers()==0){
                    showAlertDialog(TYPE_NO_EVALUATIONS);
                } else showAlertDialog(TYPE_OK);
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeListStatus();
            }
        });

        memberList = new ArrayList<>();

        return v;
    }

    private void prepareViews() {
        switch (originStatus){
            case GameTable.STATUS_WAITING:
                if (creator.equals(callback.getBungieId())){
                    joinButton.setVisibility(View.VISIBLE);
                    checkLayout.setVisibility(View.VISIBLE);
                    joinButton.setEnabled(true);
                    status = STATUS_WAITING_CREATOR;
                } else{
                    checkLayout.setVisibility(View.GONE);
                    joinButton.setText(R.string.waiting_validation);
                    joinButton.setEnabled(false);
                    status = STATUS_WAITING;
                }
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.validate_event_title);
                break;
            case GameTable.STATUS_VALIDATED:
                joinButton.setEnabled(true);
                joinButton.setText(R.string.evaluate);
                joinButton.setVisibility(View.VISIBLE);
                checkLayout.setVisibility(View.GONE);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.evaluate_match);
                status = STATUS_VALIDATED;
                break;
            case GameTable.STATUS_EVALUATED:
                joinButton.setEnabled(true);
                joinButton.setVisibility(View.GONE);
                checkLayout.setVisibility(View.GONE);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.event_details);
                status = STATUS_EVALUATED;
                break;
        }
    }

    public void changeListStatus(){
        if (listStatus){

            listStatus = false;

            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);

            for (int i=1; i<=memberList.size();i++){
                View v = getListView().getChildAt(i);
                v.startAnimation(anim);
                v.setEnabled(false);
                adapter.getItem(i-1).setChecked(false);
                adapter.notifyDataSetChanged();
            }

            joinButton.setText(R.string.delete);

        } else {

            listStatus = true;

            AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);

            for (int i=1;i<=memberList.size();i++){
                View v = getListView().getChildAt(i);
                v.startAnimation(anim);
                v.setEnabled(true);
                adapter.getItem(i-1).setChecked(true);
                adapter.notifyDataSetChanged();
            }

            joinButton.setText(R.string.validate);

        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView, null, false);
            this.getListView().addFooterView(footerView);
        }

        adapter = new SimpleMemberAdapter(getContext(), memberList);
        getListView().setAdapter(adapter);

        getGameData();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    private int getCheckedMembers(){
        int result = 0;
        for (int i=0; i<memberList.size(); i++){
            if (memberList.get(i).isChecked()){
                result ++;
            }
        }
        return result;
    }

    private int getEvaluatedMembers(){
        int result = 0;
        for (int i=0; i<memberList.size();i++){
            if (memberList.get(i).getRating() != 0){
                result ++;
            }
        }
        return result;
    }

    private void showAlertDialog(int dialogType) {

        final String title = "title";
        final String msg = "msg";
        final String posButton = "posButton";
        final String negButton = "negButton";

        if (!listStatus) dialogType = TYPE_DELETE;
        selectedType = dialogType;

        Bundle bundle = new Bundle();
        switch (dialogType){
            case TYPE_ONLY_CREATOR:
                bundle.putString(title,getResources().getString(R.string.no_guardians));
                bundle.putString(msg, getResources().getString(R.string.no_guardians_dialog_msg));
                bundle.putString(posButton, getResources().getString(R.string.delete));
                bundle.putString(negButton, getResources().getString(R.string.nevermind));
                break;
            case TYPE_NO_EVALUATIONS:
                bundle.putString(title, getResources().getString(R.string.no_evaluations));
                bundle.putString(msg, getResources().getString(R.string.no_evaluations_dialog_msg));
                bundle.putString(posButton, getResources().getString(R.string.validate));
                bundle.putString(negButton, getResources().getString(R.string.nevermind));
                break;
            case TYPE_OK:
                bundle.putString(title, getResources().getString(R.string.validate_event_title));
                bundle.putString(msg, getResources().getString(R.string.validation_dialog_msg));
                bundle.putString(posButton, getResources().getString(R.string.validate));
                bundle.putString(negButton, getResources().getString(R.string.nevermind));
                break;
            case TYPE_DELETE:
                bundle.putString(title, getResources().getString(R.string.deleting_match));
                bundle.putString(msg, getResources().getString(R.string.deleting_match_dialog_msg));
                bundle.putString(posButton, getResources().getString(R.string.delete));
                bundle.putString(negButton, getResources().getString(R.string.nevermind));
                break;
        }

        bundle.putInt("type", MyAlertDialog.CONFIRM_DIALOG);

        dialog = new MyAlertDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(),"dialog");

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        switch (status){
            case STATUS_WAITING_CREATOR:
                changeListItem(v, position, status);
                break;
            case STATUS_WAITING:
                break;
            case STATUS_VALIDATED:
                changeListItem(v, position, status);
            case STATUS_EVALUATED:
                break;
        }

    }

    private void changeListItem(View v, int position, int status) {

        if (listStatus){
            ImageView img = (ImageView) v.findViewById(R.id.rate_img);

            int newpos = position -1;
            MembersModel member = adapter.getItem(newpos);
            int newRating = 0;

            if (!member.getMembershipId().equals(callback.getBungieId())){
                if (member.isChecked()){
                    switch (member.getRating()){
                        case -1:
                            if (status == STATUS_WAITING_CREATOR){
                                newRating = 0;
                                member.setChecked(false);
                                memberList.get(newpos).setChecked(false);
                                img.setImageResource(R.drawable.ic_error);
                                img.setVisibility(View.VISIBLE);
                                AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
                                anim.setDuration(250);
                                anim.setFillAfter(true);
                                v.startAnimation(anim);
                            } else {
                                newRating = 0;
                                img.setVisibility(View.GONE);
                            }
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
                    if (status == STATUS_WAITING_CREATOR) {
                        member.setChecked(true);
                        memberList.get(newpos).setChecked(true);
                        img.setVisibility(View.GONE);
                        AlphaAnimation anim = new AlphaAnimation(0.3f, 1.0f);
                        anim.setDuration(250);
                        anim.setFillAfter(true);
                        v.startAnimation(anim);
                    }
                }
            }

            memberList.get(newpos).setRating(newRating);
            adapter.setRating(newpos, newRating);
        }

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
        String c18 = GameTable.COLUMN_STATUS;

        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.COLUMN_NAME;
        String c11 = EventTable.COLUMN_GUARDIANS;
        String c15 = EventTable.COLUMN_TYPE;

        String c7 = MemberTable.COLUMN_MEMBERSHIP;
        String c8 = MemberTable.COLUMN_NAME;

        String c16 = EventTypeTable.COLUMN_ICON;
        String c17 = EventTypeTable.COLUMN_NAME;

        gameProjection = new String[] {c1, c2, c4, c5, c6, c7, c8, c9, c10, c11, c12,  c14, c15, c16, c17, c18};

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
            case LOADER_EVALUATION:
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVALUATION_URI,
                        EvaluationTable.ALL_COLUMNS,
                        EvaluationTable.COLUMN_GAME + "=" + gameId + " AND " + EvaluationTable.COLUMN_MEMBERSHIP_A + "=" + callback.getBungieId(),
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
                case LOADER_GAME:
                    eventIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", getContext().getPackageName() ));

                    String gameEventName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventName.setText(gameEventName);

                    String gameEventTypeName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventType.setText(gameEventTypeName);

                    String date = DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME)));
                    String hour = DateUtils.getTime(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME)));
                    String timeString = date + getResources().getString(R.string.at) + hour;
                    time.setText(timeString);

                    inscriptions = data.getInt(data.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));

                    prepareMemberStrings();
                    getLoaderManager().initLoader(LOADER_ENTRY_MEMBERS, null, this);

                    if (originStatus.equals(GameTable.STATUS_EVALUATED)) getLoaderManager().initLoader(LOADER_EVALUATION, null, this);

                    break;
                case LOADER_ENTRY_MEMBERS:

                    memberList.clear();
                    data.moveToFirst();
                    for (int i=0; i < data.getCount();i++){
                        MembersModel memberModel = new MembersModel();
                        memberModel.setMembershipId(data.getString(data.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                        memberModel.setName(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                        memberModel.setIconPath(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                        memberModel.setRating(0);
                        memberModel.setChecked(true);
                        memberModel.setEntryId(data.getInt(data.getColumnIndexOrThrow(EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID))));
                        memberModel.setLikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES)));
                        memberModel.setDislikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES)));
                        memberModel.setGamesCreated(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED)));
                        memberModel.setGamesPlayed(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED)));
                        memberList.add(memberModel);
                        data.moveToNext();
                    }

                    adapter.notifyDataSetChanged();

                    break;
                case LOADER_EVALUATION:

                    data.moveToFirst();
                    for (int i=0;i<memberList.size();i++){
                        for (int x=0;x<data.getCount();x++){
                            if (data.getString(data.getColumnIndexOrThrow(EvaluationTable.COLUMN_MEMBERSHIP_B)).equals(memberList.get(i).getMembershipId())){
                                memberList.get(i).setRating(data.getInt(data.getColumnIndexOrThrow(EvaluationTable.COLUMN_EVALUATION)));
                                //Log.w(TAG, "Member: " + memberList.get(i).getMembershipId() + " rated " + data.getInt(data.getColumnIndexOrThrow(EvaluationTable.COLUMN_EVALUATION)));
                                break;
                            }
                            data.moveToNext();
                        }
                        data.moveToFirst();
                    }
                    adapter.notifyDataSetChanged();
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

        switch (selectedType){
            case TYPE_DELETE:
                deleteGame(uri);
                break;
            case TYPE_NO_EVALUATIONS:
                if (status == STATUS_WAITING_CREATOR){
                    validateGame(values, uri);
                    evaluateGame(values, uri);
                } else evaluateGame(values, uri);
                break;
            case TYPE_OK:
                if (status == STATUS_WAITING_CREATOR){
                    validateGame(values, uri);
                    evaluateGame(values, uri);
                } else evaluateGame(values, uri);
                break;
            case TYPE_ONLY_CREATOR:
                deleteGame(uri);
                break;
        }

        values.clear();

    }

    private void evaluateGame(ContentValues values, Uri uri) {

        for (int i=0;i<memberList.size();i++){
            //Log.w(TAG, "Membro " + memberList.get(i).getName() + " está vai entrar no loop de avaliação...");
            if (!memberList.get(i).getMembershipId().equals(callback.getBungieId())){
                if (memberList.get(i).getRating() != 0){
                    values.put(EvaluationTable.COLUMN_GAME, gameId);
                    values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, callback.getBungieId());
                    values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, memberList.get(i).getMembershipId());
                    values.put(EvaluationTable.COLUMN_EVALUATION, memberList.get(i).getRating());
                    getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
                    values.clear();
                    //Log.w(TAG, "Membro " + memberList.get(i).getName() + " foi avaliado em " + memberList.get(i).getRating());
                } //else Log.w(TAG, "Membro " + memberList.get(i).getName() + " está com rate 0, portanto não foi criada uma entrada");
            } //else Log.w(TAG, "Membro " + memberList.get(i).getName() + " não foi avaliado pois é o criador da partida");
        }

        Random random = new Random();

        //Fake Evaluations para 4611686018446566077
        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018446566077");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, callback.getBungieId());
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018446566077");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018434509539");
        values.put(EvaluationTable.COLUMN_EVALUATION, -1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018446566077");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018444413912");
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        //Fake evaluations para 4611686018434509539
        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018434509539");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, callback.getBungieId());
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018434509539");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018446566077");
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018434509539");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018444413912");
        values.put(EvaluationTable.COLUMN_EVALUATION, -1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        //Fake evaluations para 4611686018444413912
        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018444413912");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, callback.getBungieId());
        values.put(EvaluationTable.COLUMN_EVALUATION, -1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018444413912");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018446566077");
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(EvaluationTable.COLUMN_GAME, gameId);
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, "4611686018444413912");
        values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, "4611686018434509539");
        values.put(EvaluationTable.COLUMN_EVALUATION, 1);
        getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
        values.clear();

        values.put(GameTable.COLUMN_INSCRIPTIONS, inscriptions);
        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_EVALUATED);
        getContext().getContentResolver().update(uri, values, null, null);

        callback.closeFragment();

    }

    private void validateGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_EVALUATED); //inserir STATUS_VALIDATED no servidor
        getContext().getContentResolver().update(uri, values, null, null);

        for (int i=0; i<memberList.size(); i++) {
            //Log.w(TAG, "Membro " + memberList.get(i).getName() + " está marcado como " + memberList.get(i).isChecked());
            if (!memberList.get(i).isChecked()) {
                String selection = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID) + "=" + memberList.get(i).getEntryId();
                getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);
                //Log.w(TAG, "Membro " + memberList.get(i).getName() + " foi removido!");
                memberList.remove(i);
                inscriptions--;
                i--;
            } else {
                //Log.w(TAG, "Membro " + memberList.get(i).getName() + " terá seus status atualizados...");
                updateMemberStatuses(i);
            }
        }

        values.clear();

    }

    private void updateMemberStatuses(int position) {

        ContentValues memberValues = new ContentValues();

        switch (memberList.get(position).getRating()){
            case -1:
                int newValue = memberList.get(position).getDislikes() + 1;
                memberValues.put(MemberTable.COLUMN_DISLIKES,newValue);
                Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Dislikes atualizado de " + memberList.get(position).getDislikes() + " para " + newValue);
                break;
            case 1:
                newValue = memberList.get(position).getLikes() + 1;
                memberValues.put(MemberTable.COLUMN_LIKES,newValue);
                Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Likes atualizado de " + memberList.get(position).getLikes() + " para " + newValue);
                break;
        }

        if (memberList.get(position).getMembershipId().equals(callback.getBungieId())){
            int newValue = memberList.get(position).getGamesCreated() + 1;
            memberValues.put(MemberTable.COLUMN_CREATED,newValue);
            Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Created atualizado de " + memberList.get(position).getGamesCreated() + " para " + newValue);
        } else {
            int newValue = memberList.get(position).getGamesPlayed() + 1;
            memberValues.put(MemberTable.COLUMN_PLAYED, newValue);
            Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Played atualizado de " + memberList.get(position).getGamesPlayed() + " para " + newValue);
        }

        String where = MemberTable.COLUMN_MEMBERSHIP + "=" + memberList.get(position).getMembershipId();
        getContext().getContentResolver().update(DataProvider.MEMBER_URI, memberValues, where, null);
        memberValues.clear();

    }

    private void deleteGame(Uri uri) {
        getContext().getContentResolver().delete(uri, null, null);
        String selection = GameTable.getQualifiedColumn(GameTable.COLUMN_ID) + "=" + gameId;
        getContext().getContentResolver().delete(DataProvider.GAME_URI, selection, null);
        callback.closeFragment();
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

    @Override
    public void onItemSelected(String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }
}