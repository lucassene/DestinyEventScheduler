package com.destiny.event.scheduler.interfaces;

import com.destiny.event.scheduler.models.GameModel;

import java.util.List;

public interface UserDataListener {

    public void onUserDataLoaded();
    public void onGamesLoaded(List<GameModel> gameList);

}
