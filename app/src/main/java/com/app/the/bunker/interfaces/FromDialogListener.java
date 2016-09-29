package com.app.the.bunker.interfaces;


import java.util.Calendar;

public interface FromDialogListener {

    int GUARDIAN_TYPE = 1;
    int LIGHT_TYPE = 2;

    void onPositiveClick(String input, int type);
    void onDateSent(Calendar date);
    void onTimeSent(int hour, int minute);
    void onLogoff();
    void onItemSelected(String type, String entry, int value);
    void onMultiItemSelected(boolean[] items);
}
