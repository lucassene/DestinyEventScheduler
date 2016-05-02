package com.destiny.event.scheduler.interfaces;

public interface FromActivityListener {

    public void onEventTypeSent(String id);
    public void onEventGameSent(String id);
    public void onOrderBySet(String orderBy);

}
