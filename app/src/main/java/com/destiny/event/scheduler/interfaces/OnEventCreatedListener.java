package com.destiny.event.scheduler.interfaces;

import com.destiny.event.scheduler.models.GameModel;

public interface OnEventCreatedListener {

    public void onEventCreated(GameModel game);
}
