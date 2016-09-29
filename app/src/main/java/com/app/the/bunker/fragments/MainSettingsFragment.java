package com.app.the.bunker.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.dialogs.MultiChoiceDialog;
import com.app.the.bunker.dialogs.SingleChoiceDialog;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.services.UpdateNotificationsService;

import java.util.Calendar;
import java.util.HashMap;

public class MainSettingsFragment extends Fragment implements FromDialogListener {

    private static final String TAG = "MainSettingsFragment";

    private static final int DIVISOR = 60000;

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

    private ToActivityListener callback;

    private SharedPreferences sharedPrefs;

    private boolean[] checkedItems;

    private int switchType;

    private int previousScheduleTime;
    private int newScheduleTime;
    private boolean previousNewPrefs;
    private boolean newNewPrefs;
    private int previousNewTime;
    private int newNewTime;

    private boolean checkChanged;
    private boolean previousCheck;

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
                saveSoundPref(DrawerActivity.SOUND_PREF, soundCheckBox.isChecked());
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
                Bundle bundle = new Bundle();
                bundle.putString("title",getResources().getString(R.string.game_type_label));
                bundle.putBooleanArray("selectedItems",checkedItems);
                bundle.putString("fragTag",getTag());

                MultiChoiceDialog dialog = new MultiChoiceDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"multiChoiceDialog");
            }
        });

        sharedPrefs = getActivity().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        previousScheduleTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 15);
        newScheduleTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 15);

        previousNewPrefs = sharedPrefs.getBoolean(DrawerActivity.NEW_NOTIFY_PREF, false);
        previousNewTime = sharedPrefs.getInt(DrawerActivity.NEW_NOTIFY_TIME_PREF,DrawerActivity.DEFAULT_INTERVAL);
        newNewTime = sharedPrefs.getInt(DrawerActivity.NEW_NOTIFY_TIME_PREF,DrawerActivity.DEFAULT_INTERVAL);
        newNewPrefs = sharedPrefs.getBoolean(DrawerActivity.NEW_NOTIFY_PREF, false);

        checkChanged = sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false);
        previousCheck = sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false);
        setCheckedItems();
        updateViews();

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
        //Log.w(TAG, "previousTime: " + previousScheduleTime + " / newTime: " + newScheduleTime);
        if (newScheduleTime != previousScheduleTime || checkChanged != scheduledSwitch.isChecked()){
            SharedPreferences.Editor editor = getContext().getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE).edit();
            editor.putInt(DrawerActivity.SCHEDULED_TIME_PREF, newScheduleTime);
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
        if (previousNewPrefs != newNewPrefs){
            Log.w(TAG, "previousNew: " + previousNewPrefs + " / newNew: " + newNewPrefs);
            Log.w(TAG, "Updating new prefs");
            if (newNewPrefs){
                Log.w(TAG, "Creating New games notify alarm.");
                callback.registerNewGamesAlarm();
            } else {
                Log.w(TAG, "Deleting new games notify alarm.");
                callback.deleteNewGamesAlarm();
            }
        }
        if (previousNewTime != newNewTime){
            Log.w(TAG, "Change in newTime. Updating alarm.");
            callback.deleteNewGamesAlarm();
            callback.registerNewGamesAlarm();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.w(TAG, "MainSettings paused");
    }

    private void setCheckedItems() {
        checkedItems = new boolean[] {false, false, false, false, false, false, false, false, false};
        HashMap<Integer, Boolean> map = new HashMap<>();
        int[] ids = getResources().getIntArray(R.array.event_type_ids);
        for (int id : ids) {
            map.put(id, sharedPrefs.getBoolean(String.valueOf(id), false));
        }
        Log.w(TAG, map.toString());
        for (int i=0;i<map.size();i++){
            checkedItems[i] = map.get(ids[i]);
        }
        if (!hasSomeTrue(checkedItems)){
            checkedItems = new boolean[] {true, true, true, true, true, true, true, true, true};
        }
    }

    private boolean hasSomeTrue(boolean[] items){
        for (boolean item : items) {
            if (item) {
                return true;
            }
        }
        return false;
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
        switch (newNewTime){
            case 600000:
                return 4;
            case 1800000:
                return 3;
            case 3600000:
                return 2;
            case 7200000:
                return 1;
            case 21600000:
                return 0;
            default:
                return 2;
        }
    }

    private void saveSoundPref(String scheduledSoundPref, boolean checked) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        editor.putBoolean(scheduledSoundPref, checked);
        editor.apply();
    }

    private void updateViews() {
        scheduledSwitch.setChecked(sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, true));

        int time = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 10);
        String text;
        if (time == 0){
            text = getResources().getString(R.string.on_time);
        } else {
            text = time + " " + getResources().getString(R.string.minutes_before);
        }
        timeText.setText(text);
        int newTime = sharedPrefs.getInt(DrawerActivity.NEW_NOTIFY_TIME_PREF,DrawerActivity.DEFAULT_INTERVAL);
        newTimeText.setText(getNewTimeText(newTime));

        soundCheckBox.setChecked(sharedPrefs.getBoolean(DrawerActivity.SOUND_PREF, true));

        newSwitch.setChecked(sharedPrefs.getBoolean(DrawerActivity.NEW_NOTIFY_PREF, true));
        updateListText(checkedItems);
    }

    private void changeCheckText(CheckBox checkBox, TextView textView) {
        if (checkBox.isChecked()){
            textView.setText(R.string.on);
        } else {
            textView.setText(R.string.off);
        }
    }

    private void changeScheduledStatus() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        if (scheduledSwitch.isChecked()){
            if (newSwitch.isChecked()){
                switchType = 2;
            } else switchType = 1;
            editor.putBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, true);
            AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            scheduledListLayout.startAnimation(anim);
            scheduledListLayout.setClickable(true);
            scheduledListLayout.setFocusable(true);
            changeSoundStatus();
        } else {
            if (!newSwitch.isChecked()){
                switchType = 0;
            } else switchType = 1;
            editor.putBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false);
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

    private void changeNewStatus() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        if (newSwitch.isChecked()){
            if (scheduledSwitch.isChecked()){
                switchType = 2;
            } else switchType = 1;
            editor.putBoolean(DrawerActivity.NEW_NOTIFY_PREF, true);
            newNewPrefs = true;
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
        } else {
            if (!scheduledSwitch.isChecked()){
                switchType = 0;
            } else switchType = 1;
            editor.putBoolean(DrawerActivity.NEW_NOTIFY_PREF, false);
            newNewPrefs = false;
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
        }
        editor.apply();
    }

    private void changeSoundStatus(){
        if (switchType == 0){
            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            soundLayout.startAnimation(anim);
            soundLayout.setClickable(false);
            soundLayout.setFocusable(false);
            soundCheckBox.setEnabled(false);
        } else if (switchType == 1) {
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
                editor.putInt(DrawerActivity.NEW_NOTIFY_TIME_PREF, value);
                editor.apply();
                newNewTime = value;
                newTimeText.setText(getNewTimeText(value));
                break;
        }
    }

    public String getNewTimeText(int value){
        String text;
        if (value <= 1800000){
            text = String.valueOf(value/DIVISOR) + getString(R.string.minutes);
        } else if (value == 3600000){
            text = String.valueOf((value/DIVISOR)/60) + getString(R.string.hour);
        } else {
            text = String.valueOf((value/DIVISOR)/60) + getString(R.string.hours);
        }
        return text;
    }


    @Override
    public void onMultiItemSelected(boolean[] items) {
        updateListText(items);
        saveCheckedItems(items);
    }

    private void saveCheckedItems(boolean[] items) {
        String[] events = getResources().getStringArray(R.array.event_types);
        int[] ids = getResources().getIntArray(R.array.event_type_ids);
        SharedPreferences.Editor editor = getContext().getSharedPreferences(DrawerActivity.SHARED_PREFS,Context.MODE_PRIVATE).edit();
        for (int i=0;i<items.length;i++){
            editor.putBoolean(String.valueOf(ids[i]),items[i]);
            Log.w(TAG, events[i] + " marcado como " + items[i]);
        }
        editor.apply();
    }

    private void updateListText(boolean[] items){
        String[] eventList = getResources().getStringArray(R.array.event_types);
        int count = 0;
        String selectedEvent = "";
        for (int i = 0; i < items.length; i++) {
            if (items[i]) {
                count++;
                selectedEvent = eventList[i];
            }
        }
        String text;
        if (count == 0){
            text = getResources().getString(R.string.no_game_selected);
        } else if (count == 1){
            text = selectedEvent;
        } else {
            text = String.valueOf(count) + " " + getResources().getString(R.string.games_selected);
        }
        listText.setText(text);

    }

}
