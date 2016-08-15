package com.destiny.event.scheduler.models;

import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.StringUtils;

import java.io.Serializable;

public class MemberModel implements Serializable {

    private String name;
    private String membershipId;
    private String iconPath;
    private int platformId;
    private String title;
    private int likes;
    private int dislikes;
    private int gamesCreated;
    private int gamesPlayed;
    private String entryTime;
    private String lvl;
    private boolean isChecked;
    private int rating;

    public MemberModel(){
        super();
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getGamesCreated() {
        return gamesCreated;
    }

    public void setGamesCreated(int gamesCreated) {
        this.gamesCreated = gamesCreated;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getLvl() {
        return lvl;
    }

    public void setLvl(int likes, int dislikes, int gamesPlayed, int gamesCreated) {
        String xp = MemberTable.getMemberXP(likes, dislikes, gamesPlayed, gamesCreated);
        this.lvl = StringUtils.parseString(MemberTable.getMemberLevel(Integer.parseInt(xp)));
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }
}
