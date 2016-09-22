package com.destiny.event.scheduler.interfaces;

public interface FromActivityListener {

    public void onEventTypeSent(int id);
    public void onEventGameSent(int id);
    public void onOrderBySet(String orderBy);

}
