package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.dialogs.SingleChoiceDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;

import java.util.Calendar;

public class MainSettingsFragment extends Fragment implements FromDialogListener {

    private static final String TAG = "MainSettingsFragment";

    private SwitchCompat scheduledSwitch;
    private LinearLayout scheduledListLayout;
    private TextView timeText;
    private LinearLayout scheduledSoundLayout;
    private TextView scheduledCheckText;
    private CheckBox scheduledCheckBox;

    private LinearLayout aboutLayout;

    private ToActivityListener callback;

    SharedPreferences sharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings);
        View v = inflater.inflate(R.layout.settings_layout, container, false);

        scheduledSwitch = (SwitchCompat) v.findViewById(R.id.scheduled_switch);
        scheduledListLayout = (LinearLayout) v.findViewById(R.id.scheduled_list_layout);
        timeText = (TextView) v.findViewById(R.id.scheduled_list_text);
        scheduledSoundLayout = (LinearLayout) v.findViewById(R.id.scheduled_check_layout);
        scheduledCheckText = (TextView) v.findViewById(R.id.scheduled_check_text);
        scheduledCheckBox = (CheckBox) v.findViewById(R.id.scheduled_check);

        aboutLayout = (LinearLayout) v.findViewById(R.id.about_preference);

        scheduledSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScheduledStatus();
            }
        });

        scheduledListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title",getResources().getString(R.string.time));
                bundle.putInt("selectedItem",0);
                bundle.putString("fragTag",getTag());

                SingleChoiceDialog dialog = new SingleChoiceDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"singleChoiceDialog");
            }
        });

        scheduledCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCheckText(scheduledCheckBox, scheduledCheckText);
            }
        });

        aboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new AboutSettingsFragment();
                callback.loadNewFragment(fragment, null, "about");
            }
        });

        sharedPrefs = getActivity().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        updateViews();

        return v;
    }

    private void updateViews() {
        scheduledSwitch.setChecked(sharedPrefs.getBoolean(DrawerActivity.NOTIFY_PREF, true));

        int time = sharedPrefs.getInt(DrawerActivity.TIME_PREF, 10);
        String text;
        if (time == 0){
            text = getResources().getString(R.string.on_time);
        } else {
            text = time + " " + getResources().getString(R.string.minutes_before);
        }
        timeText.setText(text);

        scheduledCheckBox.setChecked(sharedPrefs.getBoolean(DrawerActivity.SOUND_PREF, true));
    }

    private void changeCheckText(CheckBox checkBox, TextView textView) {
        if (checkBox.isChecked()){
            textView.setText(R.string.on);
            sharedPrefs.edit().putBoolean(DrawerActivity.SOUND_PREF, checkBox.isChecked());
        } else {
            textView.setText(R.string.off);
            sharedPrefs.edit().putBoolean(DrawerActivity.SOUND_PREF, checkBox.isChecked());
        }
        sharedPrefs.edit().apply();
    }

    private void changeScheduledStatus() {
        if (scheduledSwitch.isChecked()){
            sharedPrefs.edit().putBoolean(DrawerActivity.NOTIFY_PREF, true);
            AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            scheduledListLayout.startAnimation(anim);
            scheduledListLayout.setClickable(true);
            scheduledListLayout.setFocusable(true);

            scheduledSoundLayout.startAnimation(anim);
            scheduledSoundLayout.setClickable(true);
            scheduledSoundLayout.setFocusable(true);
            scheduledCheckBox.setEnabled(true);
        } else {
            sharedPrefs.edit().putBoolean(DrawerActivity.NOTIFY_PREF, false);
            AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            scheduledListLayout.startAnimation(anim);
            scheduledListLayout.setClickable(false);
            scheduledListLayout.setFocusable(false);

            scheduledSoundLayout.startAnimation(anim);
            scheduledSoundLayout.setClickable(false);
            scheduledSoundLayout.setFocusable(false);
            scheduledCheckBox.setEnabled(false);
        }
        sharedPrefs.edit().apply();
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
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
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
    public void onItemSelected(String entry, int value) {
        String text;
        if (value == 0){
            text = getResources().getString(R.string.on_time);
        } else {
            text = value + " " + getResources().getString(R.string.minutes_before);
        }
        sharedPrefs.edit().putInt(DrawerActivity.TIME_PREF, value);
        sharedPrefs.edit().apply();
        timeText.setText(text);
    }
}
