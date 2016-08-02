package com.destiny.event.scheduler.models;

import com.destiny.event.scheduler.data.MemberTable;

import java.io.Serializable;

public class EntryModel implements Serializable {

    private String id;
    private String name;
    private String membershipId;
    private String iconPath;
    private int platformId;
    private String title;
    private String entryTime;
    private String lvl;

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLvl() {
        return lvl;
    }

    public void setLvl(int likes, int dislikes, int gamesPlayed, int gamesCreated) {
        this.lvl = MemberTable.getMemberXP(likes, dislikes, gamesPlayed, gamesCreated);
    }
}