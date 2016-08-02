package com.destiny.event.scheduler.models;

import java.io.Serializable;

public class GameModel implements Serializable {

    private int gameId;
    private String creatorId;
    private String creatorName;
    private String eventName;
    private String eventIcon;
    private int maxGuardians;
    private String typeName;
    private String time;
    private int minLight;
    private int inscriptions;
    private int status;
    private boolean joined;

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getEventIcon() {
        return eventIcon;
    }

    public void setEventIcon(String eventIcon) {
        this.eventIcon = eventIcon;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getInscriptions() {
        return inscriptions;
    }

    public void setInscriptions(int inscriptions) {
        this.inscriptions = inscriptions;
    }

    public int getMaxGuardians() {
        return maxGuardians;
    }

    public void setMaxGuardians(int maxGuardians) {
        this.maxGuardians = maxGuardians;
    }

    public int getMinLight() {
        return minLight;
    }

    public void setMinLight(int minLight) {
        this.minLight = minLight;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
