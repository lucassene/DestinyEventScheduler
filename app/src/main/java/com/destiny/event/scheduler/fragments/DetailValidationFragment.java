package com.destiny.event.scheduler.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.ValidationAdapter;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.EntryModel;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailValidationFragment extends ListFragment implements FromDialogListener{

    private static final String TAG = "DetailValidateFragment";

    private static final int TYPE_ONLY_CREATOR = 1;
    private static final int TYPE_NO_EVALUATIONS = 2;
    private static final int TYPE_OK = 3;
    private static final int TYPE_DELETE = 4;

    private int selectedType;
    private String origin;

    private List<EntryModel> memberList;

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
    Button validateButton;

    private ToActivityListener callback;

    ValidationAdapter adapter;

    MyAlertDialog dialog;

    int status;

    int actualUserLevel;
    String actualTitle;

    private static final int STATUS_WAITING_CREATOR = 1;
    private static final int STATUS_WAITING = 0;
    private static final int STATUS_VALIDATED = 2;
    private static final int STATUS_EVALUATED = 3;

    ArrayList<String> evalMemberList;

    GameModel game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callback = (ToActivityListener) getActivity();
        if (callback.getFmBackStackCount()>=1){
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

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

        validateButton = (Button) footerView.findViewById(R.id.btn_join);

        validateButton.setText(R.string.validate);

        Bundle bundle = getArguments();
        if (bundle != null){
            game = (GameModel) bundle.getSerializable("game");
            origin = bundle.getString("origin");
        }

        prepareViews();

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.checkConnection(getContext())){
                    if (getCheckedMembers()==1){
                        showAlertDialog(TYPE_ONLY_CREATOR);
                    } else if (getEvaluatedMembers()==0){
                        showAlertDialog(TYPE_NO_EVALUATIONS);
                    } else showAlertDialog(TYPE_OK);
                } else Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeListStatus();
            }
        });

        if (origin.equals(SearchFragment.TAG) || origin.equals(MyEventsFragment.TAG)){
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        return v;
    }

    private void prepareViews() {
        switch (game.getStatus()){
            case GameTable.STATUS_WAITING:
                if (game.getCreatorId().equals(callback.getBungieId())){
                    validateButton.setVisibility(View.VISIBLE);
                    checkLayout.setVisibility(View.VISIBLE);
                    validateButton.setEnabled(true);
                    status = STATUS_WAITING_CREATOR;
                } else{
                    checkLayout.setVisibility(View.GONE);
                    validateButton.setText(R.string.waiting_validation);
                    validateButton.setEnabled(false);
                    status = STATUS_WAITING;
                }
                callback.setToolbarTitle(getString(R.string.validate_event_title));
                break;
            case GameTable.STATUS_VALIDATED:
                validateButton.setEnabled(true);
                validateButton.setText(R.string.evaluate);
                validateButton.setVisibility(View.VISIBLE);
                checkLayout.setVisibility(View.GONE);
                callback.setToolbarTitle(getString(R.string.evaluate_match));
                status = STATUS_VALIDATED;
                break;
            case GameTable.STATUS_EVALUATED:
                validateButton.setEnabled(true);
                validateButton.setVisibility(View.GONE);
                checkLayout.setVisibility(View.GONE);
                checkBox.setChecked(true);
                callback.setToolbarTitle(getString(R.string.event_details));
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

            validateButton.setText(R.string.delete);

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

            validateButton.setText(R.string.validate);

        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (headerView != null){
            this.getListView().addHeaderView(headerView, null, false);
        }

        memberList = new ArrayList<>();
        adapter = new ValidationAdapter(getContext(), memberList);
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
                if (status == STATUS_WAITING || status == STATUS_WAITING_CREATOR){
                    bundle.putString(msg, getResources().getString(R.string.no_evaluations_dialog_msg));
                    bundle.putString(posButton, getResources().getString(R.string.validate));
                } else if (status == STATUS_VALIDATED){
                    bundle.putString(msg, getString(R.string.no_evaluation));
                    bundle.putString(posButton, getString(R.string.evaluate));
                }
                bundle.putString(negButton, getResources().getString(R.string.nevermind));
                break;
            case TYPE_OK:
                if (status == STATUS_WAITING || status == STATUS_WAITING_CREATOR){
                    bundle.putString(title, getResources().getString(R.string.validation));
                    bundle.putString(msg, getResources().getString(R.string.validation_dialog_msg));
                    bundle.putString(posButton, getResources().getString(R.string.validate)); 
                } else if (status == STATUS_VALIDATED){
                    bundle.putString(title, getString(R.string.evaluate));
                    bundle.putString(msg, getString(R.string.confirm_evaluation));
                    bundle.putString(posButton, getString(R.string.evaluate));
                }
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
                callProfileFragment(position);
                break;
            case STATUS_VALIDATED:
                changeListItem(v, position, status);
                break;
            case STATUS_EVALUATED:
                callProfileFragment(position);
                break;
        }

    }

    private void callProfileFragment(int position) {
        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", memberList.get(position-1).getMembershipId());
        bundle.putInt("type", MyNewProfileFragment.TYPE_DETAIL);

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void changeListItem(View v, int position, int status) {

        if (listStatus){
            ImageView img = (ImageView) v.findViewById(R.id.rate_img);

            int newpos = position -1;
            EntryModel member = memberList.get(newpos);
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
            adapter.notifyDataSetChanged();
        }

    }

    private void getGameData() {
        eventIcon.setImageResource(getContext().getResources().getIdentifier(game.getEventIcon(),"drawable",getContext().getPackageName()));
        eventName.setText(getContext().getResources().getIdentifier(game.getEventName(),"string",getContext().getPackageName()));
        eventType.setText(getContext().getResources().getIdentifier(game.getTypeName(),"string",getContext().getPackageName()));
        String date = DateUtils.onBungieDate(game.getTime());
        String hour = DateUtils.getTime(game.getTime());
        String timeString = date + getResources().getString(R.string.at) + hour;
        time.setText(timeString);
        getMembers(game.getGameId());
    }

    private void getMembers(int gameId) {
        callback.getGameEntries(gameId);
    }

    public void onEntriesLoaded(List<EntryModel> entryList){
        Log.w(TAG, "entryList size: " + entryList.size());
        for (int i=0;i<entryList.size();i++){
            entryList.get(i).setChecked(true);
            entryList.get(i).setRating(0);
        }
        memberList.addAll(entryList);
        if (footerView != null){
            this.getListView().addFooterView(footerView);
        }
        setAdapter(memberList);
    }

    private void setAdapter(List<EntryModel> entryList) {
        adapter = new ValidationAdapter(getContext(), entryList);
        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.validate_event_title));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPositiveClick(String input, int type) {

        /*ContentValues values = new ContentValues();
        String uriString = DataProvider.GAME_URI + "/" + game.getGameId();
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

        values.clear();*/

    }

    private void evaluateGame(ContentValues values, Uri uri) {

        for (int i=0;i<memberList.size();i++){
            //Log.w(TAG, "Membro " + memberList.get(i).getName() + " está vai entrar no loop de avaliação...");
            if (!memberList.get(i).getMembershipId().equals(callback.getBungieId())){
                values.put(EvaluationTable.COLUMN_GAME, game.getGameId());
                values.put(EvaluationTable.COLUMN_MEMBERSHIP_A, callback.getBungieId());
                values.put(EvaluationTable.COLUMN_MEMBERSHIP_B, memberList.get(i).getMembershipId());
                values.put(EvaluationTable.COLUMN_EVALUATION, memberList.get(i).getRating());
                getContext().getContentResolver().insert(DataProvider.EVALUATION_URI, values);
                values.clear();
                    //Log.w(TAG, "Membro " + memberList.get(i).getName() + " foi avaliado em " + memberList.get(i).getRating());
            } //else Log.w(TAG, "Membro " + memberList.get(i).getName() + " não foi avaliado pois é o criador da partida");
        }
        callback.closeFragment();

    }

    /*private void validateGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_EVALUATED); //inserir STATUS_VALIDATED no servidor
        getContext().getContentResolver().update(uri, values, null, null);

        for (int i=0; i<memberList.size(); i++) {
            //Log.w(TAG, "Membro " + memberList.get(i).getName() + " está marcado como " + memberList.get(i).isChecked());
            if (!memberList.get(i).isChecked()) {
                String selection = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID) + "=" + memberList.get(i).getEntryId();
                getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);
                //Log.w(TAG, "Membro " + memberList.get(i).getName() + " foi removido!");
                memberList.remove(i);
                game.setInscriptions(game.getInscriptions()-1);
                i--;
            } else {
                //Log.w(TAG, "Membro " + memberList.get(i).getName() + " terá seus status atualizados...");
                updateMemberStatuses(i);
            }
        }

        Intent intent = new Intent(getContext(),TitleService.class);
        ArrayList<String> memberIdList = new ArrayList<>();
        for (int i=0;i<memberList.size();i++){
            memberIdList.add(memberList.get(i).getMembershipId());
        }
        intent.putStringArrayListExtra("membershipList",memberIdList);
        getContext().startService(intent);

        Intent levelIntent = new Intent(getContext(), LevelCheckService.class);
        //Log.w(TAG, "actualTitle: " + actualTitle);
        levelIntent.putExtra("level", actualUserLevel);
        levelIntent.putExtra("title", actualTitle);
        getContext().startService(levelIntent);

        values.clear();

    }

    private void updateMemberStatuses(int position) {

        ContentValues memberValues = new ContentValues();

        switch (memberList.get(position).getRating()){
            case -1:
                int newValue = memberList.get(position).getDislikes() + 1;
                memberValues.put(MemberTable.COLUMN_DISLIKES,newValue);
                //Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Dislikes atualizado de " + memberList.get(position).getDislikes() + " para " + newValue);
                break;
            case 1:
                newValue = memberList.get(position).getLikes() + 1;
                memberValues.put(MemberTable.COLUMN_LIKES,newValue);
                //Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Likes atualizado de " + memberList.get(position).getLikes() + " para " + newValue);
                break;
        }

        if (memberList.get(position).getMembershipId().equals(callback.getBungieId())){
            int newValue = memberList.get(position).getGamesCreated() + 1;
            memberValues.put(MemberTable.COLUMN_CREATED,newValue);
            //Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Created atualizado de " + memberList.get(position).getGamesCreated() + " para " + newValue);
        } else {
            int newValue = memberList.get(position).getGamesPlayed() + 1;
            memberValues.put(MemberTable.COLUMN_PLAYED, newValue);
            //Log.w(TAG, "Membro " + memberList.get(position).getName() + " teve seu campo Played atualizado de " + memberList.get(position).getGamesPlayed() + " para " + newValue);
        }

        String where = MemberTable.COLUMN_MEMBERSHIP + "=" + memberList.get(position).getMembershipId();
        getContext().getContentResolver().update(DataProvider.MEMBER_URI, memberValues, where, null);
        memberValues.clear();

    }

    private void deleteGame(Uri uri) {
        getContext().getContentResolver().delete(uri, null, null);
        String selection = GameTable.getQualifiedColumn(GameTable.COLUMN_ID) + "=" + game.getGameId();
        getContext().getContentResolver().delete(DataProvider.GAME_URI, selection, null);
        callback.closeFragment();
    }*/

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