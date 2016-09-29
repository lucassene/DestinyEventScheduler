package com.app.the.bunker.interfaces;

public interface FromActivityListener {

    void onEventTypeSent(int id);
    void onEventGameSent(int id);
    void onOrderBySet(String orderBy);

}
