package com.app.the.bunker.interfaces;

import java.util.List;

public interface FromActivityListener {

    void onEventTypeSent(int id);
    void onEventGameSent(int id);
    void onEntriesSent(List<String> list);

}
