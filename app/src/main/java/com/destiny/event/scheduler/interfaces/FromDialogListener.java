package com.destiny.event.scheduler.interfaces;


import java.util.Calendar;

public interface FromDialogListener {

    public static final int GUARDIAN_TYPE = 1;
    public static final int LIGHT_TYPE = 2;

    public void onPositiveClick(String input, int type);
    public void onDateSent(Calendar date);
    public void onTimeSent(int hour, int minute);
    public void onLogoff();
    public void onItemSelected(String type, String entry, int value);
    public void onMultiItemSelected(boolean[] items);
}
