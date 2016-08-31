package com.destiny.event.scheduler.interfaces;

import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;

import java.util.List;

public interface UserDataListener {

    public void onUserDataLoaded();
    public void onGamesLoaded(List<GameModel> gameList);
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded);
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded);
    public void onMembersUpdated();

}
