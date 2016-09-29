package com.app.the.bunker.interfaces;

import com.app.the.bunker.models.GameModel;
import com.app.the.bunker.models.MemberModel;

import java.util.List;

public interface UserDataListener {

    void onUserDataLoaded();
    void onGamesLoaded(List<GameModel> gameList);
    void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded, int gameId);
    void onMemberLoaded(MemberModel member, boolean isUpdateNeeded);
    void onMembersUpdated();

}
