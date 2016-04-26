package com.destiny.event.scheduler.interfaces;


public interface FromDialogListener {

    public static final int GUARDIAN_TYPE = 1;
    public static final int LIGHT_TYPE = 2;

    public void onPositiveClick(String input, int type);
    public void onDateSent(String date);
    public void onTimeSent(String time);
    public void onLogoff();
}
