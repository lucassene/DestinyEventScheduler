package com.app.the.bunker;

public class Constants {

    //Account Managament
    public static final String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public static final String ARG_AUTH_TYPE = "AUTH_TYPE";
    public static final String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";

    public static final String ACC_TYPE = "com.app.the.bunker";
    public static final String AUTH_TYPE = "default";
    public static final String ACC_MEMEBRSHIP = "membership";
    public static final String ACC_PLATFORM = "platform";
    public static final String ACC_CLAN = "clanId";

    //SharedPreferences
    public static final String SHARED_PREFS = "myDestinyPrefs";
    public static final String SCHEDULED_NOTIFY_PREF = "allowScheduledNotify";
    public static final String SCHEDULED_TIME_PREF = "notificationTime";
    public static final String SOUND_PREF = "scheduledNotifySound";
    public static final String NEW_NOTIFY_PREF = "allowNewEventsNotify";
    public static final String NEW_NOTIFY_TIME_PREF = "newNotificationTime";
    public static final String FOREGROUND_PREF = "isForeground";
    public static final String DOWNLOAD_PREF = "downloadList";
    public static final String COOKIES_PREF = "cookies";
    public static final String XCSRF_PREF = "xcsrf";
    public static final String NEW_GAMES_PREF = "newGames";
    public static final String MEMBER_PREF = "membership";
    public static final String PLATFORM_PREF = "platform";
    public static final String CLAN_PREF = "clanId";
    public static final String EVENT_PREF = "eventMax";
    public static final String TYPE_PREF = "typeMax";
    public static final String KEY_PREF = "authKey";
    public static final String USERNAME_PREF = "userName";
    public static final String LAST_DAILY_PREF = "lastDailyCheck";
    public static final String DONE_NOTIFY_PREF = "allowDoneNotify";

    //Server
    public static final String SERVER_BASE_URL = "https://destiny-scheduler.herokuapp.com/";
    //public static final String SERVER_BASE_URL = "https://destiny-event-scheduler.herokuapp.com/";
    public static final String GAME_ENDPOINT = "api/game";
    public static final String DONE_ENDPOINT = "/done";
    public static final String LOGIN_ENDPOINT = "login";
    public static final String CLAN_ENDPOINT = "api/clan";
    public static final String MEMBERS_ENDPOINT = "members";
    public static final String MEMBERLIST_ENDPOINT = "api/member/list";
    public static final String EVENTS_ENDPOINT = "api/events";
    public static final String ENTRIES_ENDPOINT = "/entries";
    public static final String JOIN_ENDPOINT = "/join";
    public static final String LEAVE_ENDPOINT = "/leave";
    public static final String VALIDATE_ENDPOINT = "/validate";
    public static final String EVALUATION_ENDPOINT = "/evaluations/";
    public static final String HISTORY_ENDPOINT = "/history";
    public static final String MEMBER_ENDPOINT = "api/member/";
    public static final String PROFILE_ENDPOINT = "/profile";
    public static final String EXCEPTION_ENDPOINT = "api/log-app";
    public static final String NOTICE_ENDPOINT = "api/notice";

    public static final String STATUS_PARAM = "?status=";
    public static final String JOINED_PARAM = "&joined=";
    public static final String INITIAL_PARAM = "?initialId=";

    public static final String MEMBER_HEADER = "membership";
    public static final String CLAN_HEADER = "clanId";
    public static final String PLATFORM_HEADER = "platform";
    public static final String TIMEZONE_HEADER = "zoneid";
    public static final String AUTH_HEADER = "Authorization";

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String DELETE_METHOD = "DELETE";


}
