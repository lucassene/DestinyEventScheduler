package com.destiny.event.scheduler.fragments;

import android.content.Context;
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
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.EvaluationModel;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;
import com.destiny.event.scheduler.utils.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailValidationFragment extends ListFragment implements FromDialogListener, UserDataListener{

    public static final String TAG = "DetailValidateFragment";

    private static final int TYPE_ONLY_CREATOR = 1;
    private static final int TYPE_NO_EVALUATIONS = 2;
    private static final int TYPE_OK = 3;
    private static final int TYPE_DELETE = 4;

    private String origin;

    private List<MemberModel> memberList;

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;
    LinearLayout commentLayout;
    TextView comment;
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

    private static final int STATUS_WAITING_CREATOR = 1;
    private static final int STATUS_WAITING = 0;
    private static final int STATUS_VALIDATED = 2;
    private static final int STATUS_EVALUATED = 3;

    ArrayList<String> validatedEntryList;
    ArrayList<EvaluationModel> evaluationList;
    private ArrayList<MemberModel> entryList;

    int selectedType;

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

        callback.registerUserDataListener(this);

        headerView = inflater.inflate(R.layout.validate_header_layout, null);
        footerView = inflater.inflate(R.layout.detail_footer_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        commentLayout = (LinearLayout) headerView.findViewById(R.id.comment_layout);
        comment = (TextView) headerView.findViewById(R.id.comment_text);
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
        Log.w(TAG, "Game Status: " + game.getStatus());
        switch (game.getStatus()){
            case GameModel.STATUS_WAITING:
                Log.w(TAG, "getBungieId: " + callback.getBungieId());
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
            case GameModel.STATUS_VALIDATED:
                validateButton.setEnabled(true);
                validateButton.setText(R.string.evaluate);
                validateButton.setVisibility(View.VISIBLE);
                checkLayout.setVisibility(View.GONE);
                callback.setToolbarTitle(getString(R.string.evaluate_match));
                status = STATUS_VALIDATED;
                break;
        }
        if (game.getComment() == null || game.getComment().length() == 0){
            commentLayout.setVisibility(View.GONE);
        } else commentLayout.setVisibility(View.VISIBLE);
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

        entryList = new ArrayList<>();

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
            MemberModel member = memberList.get(newpos);
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
        setViewIcon(eventIcon, getContext().getResources().getIdentifier(game.getEventIcon(),"drawable",getContext().getPackageName()),game.getTypeIcon());
        eventName.setText(game.getEventName());
        eventType.setText(game.getTypeName());
        if (game.getComment() != null && !StringUtils.isEmptyOrWhiteSpaces(game.getComment())){
            comment.setText(game.getComment());
        }
        String date = DateUtils.onBungieDate(game.getTime());
        String hour = DateUtils.getTime(game.getTime());
        String timeString = date + getResources().getString(R.string.at) + hour;
        time.setText(timeString);
        if (entryList.size()==0){
            getMembers(game.getGameId());
        } else onEntriesLoaded(entryList, false, game.getGameId());
    }

    private void setViewIcon(ImageView view, int resId, String typeIcon){
        if (resId != 0){
            view.setImageResource(resId);
        } else {
            int typeRes = getContext().getResources().getIdentifier(typeIcon,"drawable",getContext().getPackageName());
            if (typeRes != 0){
                Log.w(TAG, "Event icon not found. Using Type icon instead");
                view.setImageResource(typeRes);
            } else{
                Log.w(TAG, "Drawable resource not found.");
                view.setImageResource(R.drawable.ic_missing);
            }
        }
    }

    private void getMembers(int gameId) {
        callback.getGameEntries(gameId);
    }

    @Override
    public void onUserDataLoaded() {

    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {

    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded, int gameId){
        if (gameId == game.getGameId()){
            if (entryList != null){
                Log.w(TAG, "historyEntries size: " + entryList.size());
                this.entryList = (ArrayList<MemberModel>) entryList;
                for (int i=0;i<entryList.size();i++){
                    entryList.get(i).setChecked(true);
                    entryList.get(i).setRating(0);
                }
                memberList.addAll(entryList);
                if (footerView != null){
                    this.getListView().addFooterView(footerView);
                }
                if (isUpdateNeeded) { callback.updateGameEntries(GameModel.STATUS_DONE, game.getGameId(), memberList.size()); }
                setAdapter(memberList);
            }
        } else {
            callback.getGameEntries(game.getGameId());
        }
    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {

    }

    @Override
    public void onMembersUpdated() {
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("historyEntries",entryList);
    }

    private void setAdapter(List<MemberModel> entryList) {
        adapter = new ValidationAdapter(getContext(), entryList);
        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
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
        callback.setToolbarTitle(getString(R.string.validate_event_title));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPositiveClick(String input, int type) {

        switch (selectedType){
            case TYPE_DELETE:
                deleteGame();
                break;
            case TYPE_NO_EVALUATIONS:
                if (status == STATUS_WAITING_CREATOR){
                    validateGame();
                } else evaluateGame();
                break;
            case TYPE_OK:
                if (status == STATUS_WAITING_CREATOR){
                    validateGame();
                } else evaluateGame();
                break;
            case TYPE_ONLY_CREATOR:
                deleteGame();
                break;
        }

    }

    private void evaluateGame() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_EVALUATE_GAME);
        evaluationList = new ArrayList<>();
        for (int i=0;i<memberList.size();i++){
            if (memberList.get(i).isChecked() && !memberList.get(i).getMembershipId().equals(callback.getBungieId())){
                Log.w(TAG, "memberB: " + memberList.get(i).getMembershipId() + " rate: " + memberList.get(i).getRating());
                EvaluationModel eval = new EvaluationModel();
                eval.setMembershipId(memberList.get(i).getMembershipId());
                eval.setRate(memberList.get(i).getRating());
                evaluationList.add(eval);
            }
        }
        bundle.putParcelableArrayList(ServerService.EVALUATIONS_TAG, evaluationList);
        callback.runServerService(bundle);
    }

    private void validateGame() {
        Log.w(TAG, "Validating game " + game.getGameId());
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_VALIDATE_GAME);

        validatedEntryList = new ArrayList<>();
        evaluationList = new ArrayList<>();
        for (int i=0;i<memberList.size();i++){
            if (memberList.get(i).isChecked()){
                validatedEntryList.add(memberList.get(i).getMembershipId());
                if (!memberList.get(i).getMembershipId().equals(callback.getBungieId())){
                    Log.w(TAG, "memberB: " + memberList.get(i).getMembershipId() + " rate: " + memberList.get(i).getRating());
                    EvaluationModel eval = new EvaluationModel();
                    eval.setMembershipId(memberList.get(i).getMembershipId());
                    eval.setRate(memberList.get(i).getRating());
                    evaluationList.add(eval);
                }
            }
        }
        Log.w(TAG, "validatedEntryList size: " + validatedEntryList.size());

        bundle.putStringArrayList(ServerService.ENTRY_TAG, validatedEntryList);
        bundle.putParcelableArrayList(ServerService.EVALUATIONS_TAG, evaluationList);
        callback.runServerService(bundle);
    }

    private void deleteGame() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_DELETE_GAME);
        callback.runServerService(bundle);
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
    public void onItemSelected(String type, String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }
}