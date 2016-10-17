package com.app.the.bunker.models;

import java.io.Serializable;
import java.util.ArrayList;

public class GameModel implements Serializable {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_WAITING = 1;
    public static final int STATUS_VALIDATED = 2;
    public static final int STATUS_EVALUATED = 3;
    public static final int STATUS_JOINED = 6;
    public static final int STATUS_AVAILABLE = 7;
    public static final int STATUS_DONE = 8;
    public static final int STATUS_SCHEDULED = 9;

    private int gameId;
    private String creatorId;
    private String creatorName;
    private int eventId;
    private String eventName;
    private String eventIcon;
    private int maxGuardians;
    private int typeId;
    private String typeName;
    private String typeIcon;
    private String time;
    private int minLight;
    private int inscriptions;
    private int status;
    private String comment;
    private boolean joined;
    private boolean evaluated;
    private int reserved;
    private ArrayList<MemberModel> entryList;

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

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTypeIcon() {
        return typeIcon;
    }

    public void setTypeIcon(String typeIcon) {
        this.typeIcon = typeIcon;
    }

    public ArrayList<MemberModel> getEntryList() {
        return entryList;
    }

    public void setEntryList(ArrayList<MemberModel> entryList) {
        this.entryList = entryList;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
}
