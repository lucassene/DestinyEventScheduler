package com.destiny.event.scheduler.models;

public class MembersModel {

    private String id;
    private String name;
    private String bungieId;
    private String membershipId;
    private String clanId;
    private String iconPath;
    private String platformId;
    private String likes;
    private String dislikes;
    private String gamesCreated;
    private String gamesPlayed;
    private String memberSince;

    public String getBungieId() {
        return bungieId;
    }

    public void setBungieId(String bungieId) {
        this.bungieId = bungieId;
    }

    public String getClanId() {
        return clanId;
    }

    public void setClanId(String clanId) {
        this.clanId = clanId;
    }

    public String getDislikes() {
        return dislikes;
    }

    public void setDislikes(String dislikes) {
        this.dislikes = dislikes;
    }

    public String getGamesCreated() {
        return gamesCreated;
    }

    public void setGamesCreated(String gamesCreated) {
        this.gamesCreated = gamesCreated;
    }

    public String getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(String gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
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

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }
}
