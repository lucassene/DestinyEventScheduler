package com.destiny.event.scheduler.models;

import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class MemberModel implements Serializable {

    private String name;
    private String membershipId;
    private String clanId;
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
    private int evaluationsMade;
    private ArrayList<EventTypeModel> typesPlayed;
    private EventModel favoriteEvent;
    private boolean isInsert;

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
        int xp = MemberTable.getMemberXP(likes, dislikes, gamesPlayed, gamesCreated);
        this.lvl = StringUtils.parseString(MemberTable.getMemberLevel((xp)));
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

    public int getEvaluationsMade() {
        return evaluationsMade;
    }

    public void setEvaluationsMade(int evaluationsMade) {
        this.evaluationsMade = evaluationsMade;
    }

    public ArrayList<EventTypeModel> getTypesPlayed() {
        return typesPlayed;
    }

    public void setTypesPlayed(ArrayList<EventTypeModel> typesPlayed) {
        this.typesPlayed = typesPlayed;
    }

    public EventModel getFavoriteEvent() {
        return favoriteEvent;
    }

    public void setFavoriteEvent(EventModel favoriteEvent) {
        this.favoriteEvent = favoriteEvent;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public void setInsert(boolean insert) {
        isInsert = insert;
    }

    public String getClanId() {
        return clanId;
    }

    public void setClanId(String clanId) {
        this.clanId = clanId;
    }
}
