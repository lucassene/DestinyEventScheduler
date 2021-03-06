package com.app.the.bunker.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.dialogs.MultiChoiceDialog;
import com.app.the.bunker.dialogs.SingleChoiceDialog;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.models.MultiChoiceItemModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.services.UpdateNotificationsService;
import com.app.the.bunker.utils.StringUtils;
import com.app.the.bunker.utils.SyncUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainSettingsFragment extends Fragment implements FromDialogListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainSettingsFragment";
    private static final int LOADER_TYPE = 10;

    private SwitchCompat scheduledSwitch;
    private LinearLayout scheduledListLayout;
    private TextView timeText;
    private LinearLayout soundLayout;
    private TextView soundText;
    private CheckBox soundCheckBox;

    private SwitchCompat newSwitch;
    private LinearLayout newListLayout;
    private TextView listText;

    private TextView newTimeText;
    private LinearLayout newTimeLayout;

    private SwitchCompat doneSwitch;

    private ToActivityListener callback;

    private SharedPreferences sharedPrefs;

    private int previousScheduleTime;
    private int newScheduleTime;
    private long newNotificationInterval;

    private boolean checkChanged;
    private boolean previousCheck;

    private List<MultiChoiceItemModel> typeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_layout, container, false);

        scheduledSwitch = (SwitchCompat) v.findViewById(R.id.scheduled_switch);
        scheduledListLayout = (LinearLayout) v.findViewById(R.id.scheduled_list_layout);
        timeText = (TextView) v.findViewById(R.id.scheduled_list_text);
        soundLayout = (LinearLayout) v.findViewById(R.id.scheduled_check_layout);
        soundText = (TextView) v.findViewById(R.id.scheduled_check_text);
        soundCheckBox = (CheckBox) v.findViewById(R.id.scheduled_check);

        newSwitch = (SwitchCompat) v.findViewById(R.id.new_switch);
        newListLayout = (LinearLayout) v.findViewById(R.id.new_list_layout);
        listText = (TextView) v.findViewById(R.id.new_list_text);

        newTimeText = (TextView) v.findViewById(R.id.new_list_time_text);
        newTimeLayout = (LinearLayout) v.findViewById(R.id.new_time_list_layout);

        doneSwitch = (SwitchCompat) v.findViewById(R.id.done_switch);

        scheduledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeScheduledStatus();
            }
        });

        scheduledListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title",getResources().getString(R.string.time));
                bundle.putInt("selectedItem", getSelectedTime());
                bundle.putString("fragTag",getTag());
                bundle.putString("type","scheduled");

                SingleChoiceDialog dialog = new SingleChoiceDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"singleChoiceDialog");
            }
        });

        newTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title",getResources().getString(R.string.time));
                bundle.putInt("selectedItem", getNewSelectedTime());
                bundle.putString("fragTag",getTag());
                bundle.putString("type","new");

                SingleChoiceDialog dialog = new SingleChoiceDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"singleChoiceDialog");
            }
        });

        soundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeCheckText(soundCheckBox, soundText);
                saveSoundPref(Constants.SOUND_PREF, soundCheckBox.isChecked());
            }
        });

        newSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeNewStatus();
            }
        });

        newListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeList != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", getResources().getString(R.string.game_type_label));
                    bundle.putSerializable("itemList", (Serializable) typeList);
                    bundle.putString("fragTag", getTag());

                    MultiChoiceDialog dialog = new MultiChoiceDialog();
                    dialog.setArguments(bundle);
                    dialog.show(getFragmentManager(), "multiChoiceDialog");
                }
            }
        });

        doneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeDoneStatus();
            }
        });

        sharedPrefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        previousScheduleTime = sharedPrefs.getInt(Constants.SCHEDULED_TIME_PREF, 15);
        newScheduleTime = sharedPrefs.getInt(Constants.SCHEDULED_TIME_PREF, 15);

        newNotificationInterval = sharedPrefs.getLong(Constants.NEW_NOTIFY_TIME_PREF, SyncUtils.DEFAULT_INTERVAL);

        checkChanged = sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, false);
        previousCheck = sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, false);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.w(TAG, "MainSettings resumed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.w(TAG, "MainSettings destroyed");
        updateNotifications();
        checkChanged = false;
    }

    private void updateNotifications() {
        //Log.w(TAG, "checkChanged: " + checkChanged);
        //Log.w(TAG, "previousTime: " + previousScheduleTime + " / newNotificationInterval: " + newScheduleTime);
        if (newScheduleTime != previousScheduleTime || checkChanged != scheduledSwitch.isChecked()){
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE).edit();
            editor.putInt(Constants.SCHEDULED_TIME_PREF, newScheduleTime);
            editor.apply();

            if (!sharedPrefs.getBoolean(UpdateNotificationsService.NOTIFY_RUNNING,false)){
                Log.w(TAG, "Calling UpdateNotificationService");
                Intent intent = new Intent(getContext(), UpdateNotificationsService.class);
                intent.putExtra("previousTime",previousScheduleTime);
                intent.putExtra("previousCheck", previousCheck);
                //Log.w(TAG, "previousCheck: " + previousCheck);
                getContext().startService(intent);
            } else{
                Toast.makeText(getContext(), "Atualizando notificações ainda, por favor aguarde!", Toast.LENGTH_SHORT).show();
            }
        } else Log.w(TAG, "New notifyTime equals to previsous notifyTime. No need to change");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.w(TAG, "MainSettings paused");
    }

    private int getSelectedTime() {
        switch (newScheduleTime){
            case 0:
                return 4;
            case 5:
                return 3;
            case 10:
                return 2;
            case 15:
                return 1;
            case 30:
                return 0;
            default:
                return 1;
        }
    }

    private int getNewSelectedTime(){
        switch ((int)newNotificationInterval){
            case 600:
                return 4;
            case 1800:
                return 3;
            case 3600:
                return 2;
            case 7200:
                return 1;
            case 21600:
                return 0;
            default:
                return 4;
        }
    }

    private void saveSoundPref(String scheduledSoundPref, boolean checked) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        editor.putBoolean(scheduledSoundPref, checked);
        editor.apply();
    }

    private void updateViews() {
        scheduledSwitch.setChecked(sharedPrefs.getBoolean(Constants.SCHEDULED_NOTIFY_PREF, true));

        int time = sharedPrefs.getInt(Constants.SCHEDULED_TIME_PREF, 10);
        String text;
        if (time == 0){
            text = getResources().getString(R.string.on_time);
        } else {
            text = time + " " + getResources().getString(R.string.minutes_before);
        }
        timeText.setText(text);
        long newTime = sharedPrefs.getLong(Constants.NEW_NOTIFY_TIME_PREF, SyncUtils.DEFAULT_INTERVAL);
        newTimeText.setText(getNewTimeText((int)newTime));
        soundCheckBox.setChecked(sharedPrefs.getBoolean(Constants.SOUND_PREF, true));
        if (SyncUtils.isSyncEnabled(getContext())){
            newSwitch.setChecked(sharedPrefs.getBoolean(Constants.NEW_NOTIFY_PREF, false));
            doneSwitch.setChecked(sharedPrefs.getBoolean(Constants.DONE_NOTIFY_PREF, false));
        } else {
            newSwitch.setChecked(false);
            doneSwitch.setChecked(false);
        }
        updateListText();
    }

    private void changeCheckText(CheckBox checkBox, TextView textView) {
        if (checkBox.isChecked()){
            textView.setText(R.string.on);
        } else {
            textView.setText(R.string.off);
        }
    }

    private void changeScheduledStatus() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        if (scheduledSwitch.isChecked()){
            editor.putBoolean(Constants.SCHEDULED_NOTIFY_PREF, true);
            AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            scheduledListLayout.startAnimation(anim);
            scheduledListLayout.setClickable(true);
            scheduledListLayout.setFocusable(true);
            changeSoundStatus();
        } else {
            editor.putBoolean(Constants.SCHEDULED_NOTIFY_PREF, false);
            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            scheduledListLayout.startAnimation(anim);
            scheduledListLayout.setClickable(false);
            scheduledListLayout.setFocusable(false);
            changeSoundStatus();
        }
        editor.apply();
    }

    private void changeDoneStatus() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
        if (doneSwitch.isChecked()){
            editor.putBoolean(Constants.DONE_NOTIFY_PREF, true);
        } else{
            editor.putBoolean(Constants.DONE_NOTIFY_PREF, false);
        }
        changeSoundStatus();
        editor.apply();
    }

    private void changeNewStatus() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Constants.NEW_NOTIFY_PREF, newSwitch.isChecked());
        editor.apply();
        if (newSwitch.isChecked()){
            AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            newListLayout.startAnimation(anim);
            newListLayout.setClickable(true);
            newListLayout.setFocusable(true);
            newTimeLayout.startAnimation(anim);
            newTimeLayout.setClickable(true);
            newTimeLayout.setFocusable(true);
            changeSoundStatus();
            SyncUtils.toogleSync(getContext(), true, newNotificationInterval);
        } else {
            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            newListLayout.startAnimation(anim);
            newListLayout.setClickable(false);
            newListLayout.setFocusable(false);
            newTimeLayout.startAnimation(anim);
            newTimeLayout.setClickable(false);
            newTimeLayout.setFocusable(false);
            changeSoundStatus();
            SyncUtils.toogleSync(getContext(), true, SyncUtils.SECS_IN_ONE_DAY);
        }
    }

    private void changeSoundStatus(){
        if (!newSwitch.isChecked() && !scheduledSwitch.isChecked() && !doneSwitch.isChecked()){
            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            soundLayout.startAnimation(anim);
            soundLayout.setClickable(false);
            soundLayout.setFocusable(false);
            soundCheckBox.setEnabled(false);
        } else if (newSwitch.isChecked() || scheduledSwitch.isChecked() || doneSwitch.isChecked()){
            if (!soundCheckBox.isEnabled()){
                AlphaAnimation anim = new AlphaAnimation(0.3f, 1.0f);
                anim.setDuration(250);
                anim.setFillAfter(true);
                soundLayout.startAnimation(anim);
                soundLayout.setClickable(true);
                soundLayout.setFocusable(true);
                soundCheckBox.setEnabled(true);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(LOADER_TYPE, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.settings));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onPositiveClick(String input, int type) {

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
        String text;
        switch(type){
            case "scheduled":
                if (value == 0){
                    text = getResources().getString(R.string.on_time);
                } else {
                    text = value + " " + getResources().getString(R.string.minutes_before);
                }
                newScheduleTime = value;
                timeText.setText(text);
                break;
            case "new":
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putLong(Constants.NEW_NOTIFY_TIME_PREF, value);
                editor.apply();
                if (!newSwitch.isChecked()){ value = SyncUtils.SECS_IN_ONE_DAY; }
                newNotificationInterval = value;
                SyncUtils.toogleSync(getContext(), true, value);
                Log.w(TAG, "syncAdapter interval changed to " + value + " secs");
                newTimeText.setText(getNewTimeText(value));
                break;
        }
    }

    public String getNewTimeText(int value){
        String[] names = getContext().getResources().getStringArray(R.array.pref_new_time_list_entries);
        int[] values = getContext().getResources().getIntArray(R.array.pref_new_time_list_values);
        for (int i=1; i<values.length;i++){
            if (values[i] == value){
                return names[i];
            }
        }
        return names[2];
    }


    @Override
    public void onMultiItemSelected(boolean[] items) {
    }

    @Override
    public void onListChecked(List<MultiChoiceItemModel> list) {
        if (list != null){
            typeList = list;
            saveCheckedItems();
            updateListText();
        }
    }

    private void saveCheckedItems() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        for (int i=0;i<typeList.size();i++){
            editor.putBoolean(String.valueOf(typeList.get(i).getId()),typeList.get(i).isChecked());
            Log.w(TAG, typeList.get(i).getText() + " marcado como " + typeList.get(i).isChecked());
        }
        editor.apply();
    }

    private void updateListText(){
        int count = 0;
        String selectedEvent = "";
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).isChecked()) {
                count++;
                selectedEvent = typeList.get(i).getText();
            }
        }
        String text;
        if (count == 0){
            text = getResources().getString(R.string.no_game_selected);
        } else if (count == 1){
            text = selectedEvent;
        } else if (count == typeList.size()){
            text = getString(R.string.all_items_selected);
        } else {
            text = String.valueOf(count) + " " + getResources().getString(R.string.games_selected);
        }
        listText.setText(text);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                DataProvider.EVENT_TYPE_URI,
                EventTypeTable.ALL_COLUMNS,
                null,
                null,
                StringUtils.getLanguageString() + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            typeList = new ArrayList<>();
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE);
            for (int i=0;i<data.getCount();i++){
                MultiChoiceItemModel item = new MultiChoiceItemModel();
                int id = data.getInt(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ID));
                item.setId(id);
                item.setText(data.getString(data.getColumnIndexOrThrow(StringUtils.getLanguageString())));
                item.setChecked(prefs.getBoolean(String.valueOf(id),false));
                typeList.add(item);
                data.moveToNext();
            }
            updateViews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
