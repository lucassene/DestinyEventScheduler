package com.destiny.event.scheduler.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DrawerAdapter;
import com.destiny.event.scheduler.adapters.ViewPagerAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.fragments.AboutSettingsFragment;
import com.destiny.event.scheduler.fragments.DBViewerFragment;
import com.destiny.event.scheduler.fragments.DetailEventFragment;
import com.destiny.event.scheduler.fragments.DetailHistoryFragment;
import com.destiny.event.scheduler.fragments.DetailValidationFragment;
import com.destiny.event.scheduler.fragments.HistoryListFragment;
import com.destiny.event.scheduler.fragments.MainSettingsFragment;
import com.destiny.event.scheduler.fragments.MyClanFragment;
import com.destiny.event.scheduler.fragments.MyEventsFragment;
import com.destiny.event.scheduler.fragments.MyNewProfileFragment;
import com.destiny.event.scheduler.fragments.NewEventFragment;
import com.destiny.event.scheduler.fragments.NewEventsListFragment;
import com.destiny.event.scheduler.fragments.ScheduledListFragment;
import com.destiny.event.scheduler.fragments.SearchFragment;
import com.destiny.event.scheduler.fragments.ValidateListFragment;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.AlarmReceiver;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.CreateNotificationService;
import com.destiny.event.scheduler.services.LocalService;
import com.destiny.event.scheduler.services.RequestResultReceiver;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.utils.CookiesUtils;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;
import com.destiny.event.scheduler.views.SlidingTabLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DrawerActivity extends AppCompatActivity implements ToActivityListener, LoaderManager.LoaderCallbacks<Cursor>, OnEventCreatedListener, FromDialogListener, RequestResultReceiver.Receiver {

    private static final String TAG = "DrawerActivity";

    private static final int URL_LOADER_CLAN = 40;
    private static final int URL_LOADER_USER = 30;

    public static final String TAG_MY_EVENTS = "myEvents";
    public static final String TAG_SEARCH_EVENTS = "searchEvents";

    public static final String SHARED_PREFS = "myDestinyPrefs";
    public static final String SCHEDULED_NOTIFY_PREF = "allowScheduledNotify";
    public static final String SCHEDULED_TIME_PREF = "notificationTime";
    public static final String SOUND_PREF = "scheduledNotifySound";
    public static final String NEW_NOTIFY_PREF = "allowNewNotify";
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
    public static final int DEFAULT_INTERVAL = 3600000;

    public static final int FRAGMENT_TYPE_WITHOUT_BACKSTACK = 0;
    public static final int FRAGMENT_TYPE_WITH_BACKSTACK = 1;

    public static final int TYPE_EMAIL_INTENT = 1;
    public static final int TYPE_BROWSER_INTENT = 2;

    RecyclerView rView;
    RecyclerView.Adapter rAdapter;
    RecyclerView.LayoutManager rLayoutManager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    ProgressBar progress;

    private FromActivityListener newEventListener;
    private UserDataListener newEventsListener;
    private UserDataListener scheduledEventsListener;
    private UserDataListener doneEventsListener;
    private UserDataListener userDataListener;

    private FragmentManager fm;
    private Fragment openedFragment;
    private int openedFragmentType;
    private String fragmentTag;

    private String clanName;
    private String clanId;
    private String clanIcon;
    private String clanBanner;
    private String clanDesc;

    private String bungieId;
    private String userName;
    private int platformId;

    private String clanOrderBy;

    private int isNewScheduledOrValidated = GameModel.STATUS_NEW;
    private boolean hasScheduledGames = true;
    private boolean hasValidatedGames = true;

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    SlidingTabLayout tabLayout;
    private int selectedTabFragment;

    TextView toolbarTitle;

    private Bundle spinnerSelections = new Bundle();

    private int selectedDrawerItem;

    RequestResultReceiver mReceiver;

    ArrayList<GameModel> allGameList;
    ArrayList<GameModel> newGameList;
    ArrayList<GameModel> scheduledGameList;
    ArrayList<GameModel> doneGameList;
    ArrayList<GameModel> searchGameList;
    ArrayList<GameModel> joinedGameList;
    ArrayList<GameModel> validatedGameList;

    ArrayList<MemberModel> entryList;
    ArrayList<MemberModel> historyEntries;

    MemberModel memberProfile;
    GameModel toCreateGame;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_layout);

        progress = (ProgressBar) findViewById(R.id.progress_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Typeface font = Typeface.createFromAsset(getAssets(), "d.ttf");
        if (toolbar != null) toolbarTitle = (TextView) toolbar.findViewById(R.id.title);
        toolbarTitle.setTypeface(font);
        toolbarTitle.setText(R.string.home_title);

        String titles[] = getResources().getStringArray(R.array.tab_titles);
        int numOfTabs = titles.length;

        tabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab);
        viewPager = (ViewPager) findViewById(R.id.content_view);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numOfTabs);

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(viewPager);
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @SuppressWarnings("deprecation")
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicatorColor);
            }
        });

        if (selectedTabFragment != 0) viewPager.setCurrentItem(selectedTabFragment);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
        }

        spinnerSelections.putInt(TAG_MY_EVENTS, 0);
        spinnerSelections.putInt(TAG_SEARCH_EVENTS, 0);

        if (savedInstanceState != null) {
            openedFragmentType = savedInstanceState.getInt("fragType");
            fragmentTag = savedInstanceState.getString("fragTag");
            bungieId = savedInstanceState.getString("bungieId");
            openedFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            readyAllLists(savedInstanceState);
        }

        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(PrepareActivity.PREPARE_PREF, false);
        editor.apply();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("isFromNews") && bundle.getBoolean("isFromNews",false)){
            openConfigFragment(null);
        }

    }

    private void getLoggedUserData() {
        onLoadingData();
        getSupportLoaderManager().initLoader(URL_LOADER_USER, null, this);
    }

    private void getClanData() {
        onLoadingData();
        getSupportLoaderManager().initLoader(URL_LOADER_CLAN, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_refresh:
                if (NetworkUtils.checkConnection(this)) {
                    if (openedFragment instanceof MyClanFragment) {
                        MyClanFragment frag = (MyClanFragment) openedFragment;
                        ArrayList<String> idList = frag.getBungieIdList();
                        if (idList != null) {
                            updateClan(idList);
                        } else Log.w(TAG, "bungieIdList is empty!");
                    } else if (getOpenedFragment() instanceof MyNewProfileFragment) {
                        if (memberProfile != null) {
                            Log.w(TAG, "opened Fragment: " + getOpenedFragment().toString());
                            bundle.clear();
                            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_PROFILE);
                            bundle.putString(ServerService.PROFILE_TAG, memberProfile.getMembershipId());
                            runServerService(bundle);
                        }
                    } else if (getOpenedFragment() instanceof  HistoryListFragment){
                        Log.w(TAG, "opened Fragment: " + getOpenedFragment().toString());

                        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_HISTORY_GAMES);
                        runServerService(bundle);
                    } else if (getOpenedFragment() instanceof NewEventFragment){
                        bundle.clear();
                        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_NEW_EVENTS);
                        runServerService(bundle);
                    } else if (getOpenedFragment() instanceof SearchFragment){
                        bundle.clear();
                        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_NEW_GAMES);
                        runServerService(bundle);
                    } else if (getOpenedFragment() instanceof MyEventsFragment){
                        bundle.clear();
                        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_JOINED_GAMES);
                        runServerService(bundle);
                    } else refreshLists();
                } else
                    Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateClan(ArrayList<String> idList){
        if (mReceiver == null) {
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
        }
        if (!isBungieServiceRunning()) {
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
            intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.TYPE_UPDATE_CLAN);
            intent.putExtra(BungieService.RECEIVER_EXTRA, mReceiver);
            intent.putExtra("memberList", idList);
            intent.putExtra("clanId", clanId);
            intent.putExtra("platformId", platformId);
            intent.putExtra("userMembership", bungieId);
            startService(intent);
        }
    }

    private void refreshLists() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_ALL_GAMES);
        runServerService(bundle);
        Log.w(TAG, "Refreshing Events data!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //drawerToggle.syncState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.w(TAG, "Application Paused!");
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, false);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.w(TAG, "Application Resumed!");
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, true);
        editor.apply();

        getClanData();
        getLoggedUserData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openMainActivity(null);
        if (intent.hasExtra("notification")){
            refreshLists();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragmentTag == null) {
                finish();
            } else if (openedFragmentType == FRAGMENT_TYPE_WITHOUT_BACKSTACK) {
                openMainActivity(null);
                switch (isNewScheduledOrValidated) {
                    case GameModel.STATUS_NEW:
                        viewPager.setCurrentItem(0);
                        break;
                    case GameModel.STATUS_SCHEDULED:
                        if (hasScheduledGames) {
                            viewPager.setCurrentItem(1);
                        } else viewPager.setCurrentItem(0);
                        break;
                    case GameModel.STATUS_VALIDATED:
                        if (hasValidatedGames) {
                            viewPager.setCurrentItem(2);
                        } else viewPager.setCurrentItem(0);
                        break;
                    default:
                        viewPager.setCurrentItem(0);
                        break;
                }
            } else {
                super.onBackPressed();
                openedFragment = fm.findFragmentById(R.id.content_frame);
                if (openedFragment != null) fragmentTag = openedFragment.getTag();
            }
    }

    private Fragment getOpenedFragment() {
        return fm.findFragmentById(R.id.content_frame);
    }

    public boolean openMainActivity(View child) {
        if (openedFragment != null) {
            selectedDrawerItem = 1;
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            updateViewPager();
            drawerLayout.closeDrawers();
            if (child != null) child.playSoundEffect(SoundEffectConstants.CLICK);
            return true;
        }
        drawerLayout.closeDrawers();
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedItem", selectedDrawerItem);
        outState.putSerializable("allGameList", allGameList);
        outState.putSerializable("validatedGameList", validatedGameList);
        outState.putString("fragTag", fragmentTag);
        outState.putInt("fragType", openedFragmentType);
        outState.putSerializable("entryList", entryList);
        outState.putSerializable("historyEntries", historyEntries);
        outState.putString("bungieId", bungieId);
        outState.putSerializable("memberProfile", memberProfile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedDrawerItem = savedInstanceState.getInt("selectedItem");
        readyAllLists(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    private void readyAllLists(Bundle savedInstanceState) {
        allGameList = new ArrayList<>();
        if (savedInstanceState.getSerializable("allGameList") != null){
            allGameList.addAll((ArrayList<GameModel>) savedInstanceState.getSerializable("allGameList"));
        }
        if (allGameList != null) {
            newGameList = getGamesFromList(GameModel.STATUS_NEW);
            if (newEventsListener != null) newEventsListener.onGamesLoaded(newGameList);

            scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
            if (scheduledEventsListener != null)
                scheduledEventsListener.onGamesLoaded(scheduledGameList);

            doneGameList = getGamesFromList(GameModel.STATUS_DONE);
            if (doneEventsListener != null) doneEventsListener.onGamesLoaded(doneGameList);

            searchGameList = getGamesFromList(GameModel.STATUS_AVAILABLE);
            joinedGameList = getGamesFromList(GameModel.STATUS_JOINED);
        }
        validatedGameList = (ArrayList<GameModel>) savedInstanceState.getSerializable("validatedGameList");
        entryList = (ArrayList<MemberModel>) savedInstanceState.getSerializable("entryList");
        historyEntries = (ArrayList<MemberModel>) savedInstanceState.getSerializable("historyEntries");
        memberProfile = (MemberModel) savedInstanceState.getSerializable("memberProfile");
        bungieId = savedInstanceState.getString("bungieId");
    }

    private void prepareFragmentHolder(Fragment fragment, View child, Bundle bundle, String tag) {
        if (drawerLayout != null) drawerLayout.closeDrawers();
        if (tabLayout != null) tabLayout.setViewPager(null);
        if (viewPager != null) viewPager.setAdapter(null);
        if (child != null) child.playSoundEffect(SoundEffectConstants.CLICK);
        loadNewFragment(fragment, bundle, tag);
    }

    @Override
    public void updateViewPager() {
        fragmentTag = null;
        openedFragment = null;
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setViewPager(viewPager);
        selectedDrawerItem = 1;
        rAdapter.notifyDataSetChanged();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbarTitle.setText(R.string.home_title);
    }

    @Override
    public void loadNewFragment(Fragment fragment, Bundle bundle, String tag) {

        fragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, tag);
        ft.addToBackStack(null);
        ft.commit();
        fragmentTag = tag;
        //Toast.makeText(this, "openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
        if (tag.equals("profile")) {
            memberProfile = null;
        }
        openedFragment = fragment;
        if (rAdapter != null) rAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    @Override
    public void onEventTypeSelected(int id) {
        fm.popBackStack();
        openedFragment = fm.findFragmentByTag("new");
        if (openedFragment != null) fragmentTag = openedFragment.getTag();
        newEventListener = (FromActivityListener) getSupportFragmentManager().findFragmentByTag("new");
        newEventListener.onEventTypeSent(id);
    }

    @Override
    public void onEventGameSelected(int id) {
        fm.popBackStack();
        openedFragment = fm.findFragmentByTag("new");
        if (openedFragment != null) fragmentTag = openedFragment.getTag();
        newEventListener = (FromActivityListener) getSupportFragmentManager().findFragmentByTag("new");
        newEventListener.onEventGameSent(id);
    }

    @Override
    public String getBungieId() {
        return bungieId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public int getPlatform() {
        return platformId;
    }

    @Override
    public String getClanName() {
        return clanName;
    }

    @Override
    public String getOrderBy() {
        return clanOrderBy;
    }

    @Override
    public void closeFragment() {
        openMainActivity(null);
    }

    @Override
    public void onLoadingData() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataLoaded() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onGameSelected(GameModel game, String tag) {

        entryList = null;
        historyEntries = null;
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putString("origin", tag);
        bundle.putSerializable("game", game);

        switch (game.getStatus()) {
            case GameModel.STATUS_NEW:
                fragment = new DetailEventFragment();
                if (game.isJoined()) {
                    isNewScheduledOrValidated = GameModel.STATUS_SCHEDULED;
                } else isNewScheduledOrValidated = GameModel.STATUS_NEW;
                break;
            case GameModel.STATUS_WAITING:
                isNewScheduledOrValidated = GameModel.STATUS_VALIDATED;
                fragment = new DetailValidationFragment();
                break;
            case GameModel.STATUS_VALIDATED:
                if (!game.isEvaluated()) {
                    isNewScheduledOrValidated = GameModel.STATUS_VALIDATED;
                    fragment = new DetailValidationFragment();
                } else {
                    isNewScheduledOrValidated = GameModel.STATUS_VALIDATED;
                    fragment = new DetailHistoryFragment();
                }
                break;
            default:
                fragment = new DetailEventFragment();
                isNewScheduledOrValidated = GameModel.STATUS_NEW;
                break;
        }

        tabLayout.setViewPager(null);
        viewPager.setAdapter(null);
        loadNewFragment(fragment, bundle, "game");

    }

    @Override
    public void setClanOrderBy(String orderBy) {
        clanOrderBy = orderBy;
    }

    @Override
    public void registerAlarmTask(Calendar firstNotification, int firstId, Calendar secondNotification, int secondId) {

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (firstId != 0) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_SCHEDULED_NOTIFICATIONS);
            intent.putExtra(AlarmReceiver.NOTIFY_ID, firstId);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, firstId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Log.w(TAG, "Code M: " + Build.VERSION_CODES.M);
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,firstNotification.getTimeInMillis(),pIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, firstNotification.getTimeInMillis(), pIntent);
            } else alarm.set(AlarmManager.RTC_WAKEUP, firstNotification.getTimeInMillis(), pIntent);
            Log.w(TAG, "requestId: " + firstId + " registered!");
        }

        if (secondId != 0) {
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            sIntent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_SCHEDULED_NOTIFICATIONS);
            sIntent.putExtra(AlarmReceiver.NOTIFY_ID, secondId);
            PendingIntent psIntent = PendingIntent.getBroadcast(this, secondId, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Log.w(TAG, "Code M: " + Build.VERSION_CODES.M);
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,secondNotification.getTimeInMillis(),psIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, secondNotification.getTimeInMillis(), psIntent);
            } else alarm.set(AlarmManager.RTC_WAKEUP, secondNotification.getTimeInMillis(), psIntent);
            Log.w(TAG, "requestId: " + secondId + " registered!");
        }

    }

    @Override
    public void cancelAlarmTask(int requestId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.w(TAG, "requestId canceled: " + requestId);
    }

    @Override
    public void registerUserDataListener(Fragment fragment) {
        if (fragment instanceof NewEventsListFragment) {
            newEventsListener = (UserDataListener) fragment;
        } else if (fragment instanceof ScheduledListFragment) {
            scheduledEventsListener = (UserDataListener) fragment;
        } else if (fragment instanceof ValidateListFragment) {
            doneEventsListener = (UserDataListener) fragment;
        } else {
            userDataListener = (UserDataListener) fragment;
        }
    }

    @Override
    public void deleteUserDataListener(Fragment fragment) {
        if (fragment instanceof NewEventsListFragment) {
            newEventsListener = null;
        } else if (fragment instanceof ScheduledListFragment) {
            scheduledEventsListener = null;
        } else if (fragment instanceof ValidateListFragment) {
            doneEventsListener = null;
        } else {
            userDataListener = null;
        }
    }

    @Override
    public void setSpinnerSelection(String tag, int position) {
        spinnerSelections.putInt(tag, position);
    }

    @Override
    public int getSpinnerSelection(String tag) {
        return spinnerSelections.getInt(tag);
    }

    @Override
    public void onSelectedFragment(int id) {
        selectedTabFragment = id;
    }

    @Override
    public void setFragmentType(int type) {
        openedFragmentType = type;
    }

    @Override
    public int getFmBackStackCount() {
        return fm.getBackStackEntryCount();
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    @Override
    public int getSelectedItem() {
        return selectedDrawerItem;
    }

    @Override
    public void onEventCreated(GameModel game) {
        openMainActivity(null);
        if (allGameList == null) allGameList = new ArrayList<>();
        allGameList.add(game);
        if (scheduledGameList == null) scheduledGameList = new ArrayList<>();
        scheduledGameList.add(game);
        if (scheduledEventsListener != null) {
            scheduledEventsListener.onGamesLoaded(scheduledGameList);
        }
        viewPager.setCurrentItem(1);
        setAlarmNotification(game);
    }

    private void setAlarmNotification(GameModel game) {

        int firstId = 0;
        int secondId = 0;

        Calendar gameTime = DateUtils.stringToDate(game.getTime());
        Calendar notifyTime = getNotifyTime(gameTime);
        Log.w(TAG, "gameTime: " + game.getTime() + " and notifyTime: " + DateUtils.calendarToString(notifyTime));

        if (notifyTime.getTimeInMillis() == gameTime.getTimeInMillis()) {
            ContentValues values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, game.getGameId());
            values.put(NotificationTable.COLUMN_EVENT, game.getEventName());
            values.put(NotificationTable.COLUMN_TYPE, game.getTypeName());
            values.put(NotificationTable.COLUMN_ICON, game.getEventIcon());
            values.put(NotificationTable.COLUMN_TIME, game.getTime());
            values.put(NotificationTable.COLUMN_GAME_TIME, game.getTime());
            Uri uri = getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
            if (uri != null) {
                firstId = Integer.parseInt(uri.getLastPathSegment());
            } else Log.w(TAG, "Notification cannot be created");
            values.clear();
        } else {
            ContentValues values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, game.getGameId());
            values.put(NotificationTable.COLUMN_EVENT, game.getEventName());
            values.put(NotificationTable.COLUMN_TYPE, game.getTypeName());
            values.put(NotificationTable.COLUMN_ICON, game.getEventIcon());
            values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(notifyTime));
            values.put(NotificationTable.COLUMN_GAME_TIME, game.getTime());
            Uri uri = getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
            if (uri != null) {
                firstId = Integer.parseInt(uri.getLastPathSegment());
            } else Log.w(TAG, "Notification cannot be created");
            values.clear();

            values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, game.getGameId());
            values.put(NotificationTable.COLUMN_EVENT, game.getEventName());
            values.put(NotificationTable.COLUMN_TYPE, game.getTypeName());
            values.put(NotificationTable.COLUMN_ICON, game.getEventIcon());
            values.put(NotificationTable.COLUMN_TIME, game.getTime());
            values.put(NotificationTable.COLUMN_GAME_TIME, game.getTime());
            uri = getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
            if (uri != null) {
                secondId = Integer.parseInt(uri.getLastPathSegment());
            } else Log.w(TAG, "Notification cannot be created");
            values.clear();
        }

        registerAlarmTask(notifyTime, firstId, gameTime, secondId);

    }

    private Calendar getNotifyTime(Calendar gameTime) {
        Calendar notifyTime = Calendar.getInstance();
        notifyTime.setTime(gameTime.getTime());
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int alarmTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 0) * -1;
        notifyTime.add(Calendar.MINUTE, alarmTime);
        return notifyTime;
    }

    @Override
    public boolean runServerService(Bundle bundle) {
        if (NetworkUtils.checkConnection(this)) {
            onLoadingData();
            if (mReceiver == null) mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
            intent.putExtra(ServerService.REQUEST_TAG, bundle.getInt(ServerService.REQUEST_TAG));
            intent.putExtra(ServerService.RECEIVER_TAG, mReceiver);
            intent.putExtra(ServerService.MEMBER_TAG, bungieId);
            intent.putExtra(ServerService.PLATFORM_TAG, platformId);
            intent.putExtra(ServerService.CLAN_TAG, clanId);

            if (bundle.containsKey(ServerService.GAME_TAG)) intent.putExtra(ServerService.GAME_TAG, bundle.getSerializable(ServerService.GAME_TAG));
            if (bundle.containsKey(ServerService.GAMEID_TAG))
                intent.putExtra(ServerService.GAMEID_TAG, bundle.getInt(ServerService.GAMEID_TAG));
            if (bundle.containsKey(ServerService.ENTRY_TAG))
                intent.putStringArrayListExtra(ServerService.ENTRY_TAG, bundle.getStringArrayList(ServerService.ENTRY_TAG));
            if (bundle.containsKey(ServerService.EVALUATIONS_TAG))
                intent.putParcelableArrayListExtra(ServerService.EVALUATIONS_TAG, bundle.getParcelableArrayList(ServerService.EVALUATIONS_TAG));
            if (bundle.containsKey(ServerService.PROFILE_TAG))
                intent.putExtra(ServerService.PROFILE_TAG, bundle.getString(ServerService.PROFILE_TAG));

            if (bundle.getInt(ServerService.REQUEST_TAG) == ServerService.TYPE_CREATE_GAME) {
                toCreateGame = (GameModel) bundle.getSerializable(ServerService.GAME_TAG);
            }

            startService(intent);
            return true;
        } else {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public List<GameModel> getGameList(int type) {
        return getGamesFromList(type);
    }

    @Override
    public void getGameEntries(int gameId) {
        if (entryList == null) {
            Log.w(TAG, "entryList is null");
            if (NetworkUtils.checkConnection(this)) {
                if (!isServerServiceRunning()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_GAME_ENTRIES);
                    bundle.putInt(ServerService.GAMEID_TAG, gameId);
                    runServerService(bundle);
                } else Log.w(TAG, "ServerService still running");
            } else {
                Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (userDataListener != null) {
                userDataListener.onEntriesLoaded(entryList, false, gameId);
            } else Log.w(TAG, "userDataListener is null");
        }
    }

    @Override
    public void getHistoryEntries(int gameId) {
        if (historyEntries == null) {
            Log.w(TAG, "historyEntries is null");
            if (NetworkUtils.checkConnection(this)) {
                if (!isServerServiceRunning()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_HISTORY);
                    bundle.putInt(ServerService.GAMEID_TAG, gameId);
                    runServerService(bundle);
                } else Log.w(TAG, "ServerService still running");
            } else {
                Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (userDataListener != null) {
                userDataListener.onEntriesLoaded(historyEntries, false, gameId);
            }
        }
    }

    @Override
    public List<GameModel> getHistoryGames() {
        return validatedGameList;
    }

    @Override
    public void updateGameStatus(GameModel game, int status) {
        switch (status) {
            case GameModel.STATUS_NEW:
                for (int i = 0; i < allGameList.size(); i++) {
                    if (game.getGameId() == allGameList.get(i).getGameId()) {
                        allGameList.remove(i);
                        break;
                    }
                }
                newGameList = getGamesFromList(GameModel.STATUS_NEW);
                break;
            case GameModel.STATUS_SCHEDULED:
                for (int i = 0; i < allGameList.size(); i++) {
                    if (allGameList.get(i).getGameId() == game.getGameId()) {
                        allGameList.get(i).setStatus(GameModel.STATUS_WAITING);
                        break;
                    }
                }
                scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                getContentResolver().delete(DataProvider.NOTIFICATION_URI, NotificationTable.COLUMN_GAME + "=" + game.getGameId(), null);
                closeFragment();
                break;
        }
    }

    @Override
    public void updateGameEntries(int status, int gameId, int entries) {
        for (int i = 0; i < allGameList.size(); i++) {
            if (allGameList.get(i).getGameId() == gameId) {
                allGameList.get(i).setInscriptions(entries);
                break;
            }
        }
        switch (status) {
            case GameModel.STATUS_DONE:
                doneGameList = getGamesFromList(GameModel.STATUS_DONE);
                break;
            case GameModel.STATUS_SCHEDULED:
                scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                break;
            case GameModel.STATUS_NEW:
                newGameList = getGamesFromList(GameModel.STATUS_NEW);
                break;
        }
    }

    public boolean openNewEventFragment(View child) {
        if (openedFragment instanceof NewEventFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 2;
        NewEventFragment fragment = new NewEventFragment();
        prepareFragmentHolder(fragment, child, null, "new");
        return true;
    }

    public boolean openSearchEventFragment(View child) {
        if (openedFragment instanceof SearchFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 3;
        SearchFragment fragment = new SearchFragment();
        prepareFragmentHolder(fragment, child, null, "search");
        return true;
    }

    private boolean openMyEventsFragment(View child) {
        if (openedFragment instanceof MyEventsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 4;
        MyEventsFragment fragment = new MyEventsFragment();
        prepareFragmentHolder(fragment, child, null, "myevents");
        return true;
    }

    public boolean openHistoryFragment(View child) {
        if (openedFragment instanceof HistoryListFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 5;
        HistoryListFragment fragment = new HistoryListFragment();
        prepareFragmentHolder(fragment, child, null, "history");
        return true;
    }

    public boolean openMyClanFragment(View child) {
        if (openedFragment instanceof MyClanFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 7;
        MyClanFragment fragment = new MyClanFragment();
        Bundle bundle = new Bundle();
        bundle.putString("clanName", clanName);
        bundle.putString("clanDesc", clanDesc);
        bundle.putString("clanBanner", clanBanner);
        bundle.putString("clanIcon", clanIcon);
        prepareFragmentHolder(fragment, child, bundle, "clan");
        return true;
    }

    public boolean openMyProfileFragment(View child) {
        if (memberProfile != null){
            if (openedFragment instanceof MyNewProfileFragment && memberProfile.getMembershipId().equals(bungieId)) {
                drawerLayout.closeDrawers();
                return false;
            }
        }
        selectedDrawerItem = 8;
        MyNewProfileFragment fragment = new MyNewProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putInt("type", MyNewProfileFragment.TYPE_MENU);
        prepareFragmentHolder(fragment, child, bundle, "profile");
        return true;
    }

    public boolean openConfigFragment(View child) {
        if (openedFragment instanceof MainSettingsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 10;
        MainSettingsFragment fragment = new MainSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "config");
        return true;
    }

    private boolean openAboutFragment(View child) {
        if (openedFragment instanceof AboutSettingsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        selectedDrawerItem = 11;
        AboutSettingsFragment fragment = new AboutSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "about");
        return true;
    }

    private boolean openDBViewerFragment(View child) {
        if (openedFragment instanceof DBViewerFragment) {
            drawerLayout.closeDrawers();
            return false;
        }

        DBViewerFragment fragment = new DBViewerFragment();
        prepareFragmentHolder(fragment, child, null, "dbviewer");
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection;

        switch (id) {
            case URL_LOADER_CLAN:
                projection = ClanTable.ALL_COLUMNS;
                return new CursorLoader(
                        this,
                        DataProvider.CLAN_URI,
                        projection,
                        null,
                        null,
                        null
                );
            case URL_LOADER_USER:
                projection = new String[]{LoggedUserTable.COLUMN_ID, LoggedUserTable.COLUMN_MEMBERSHIP, LoggedUserTable.COLUMN_NAME, LoggedUserTable.COLUMN_PLATFORM};
                return new CursorLoader(
                        this,
                        DataProvider.LOGGED_USER_URI,
                        projection,
                        null,
                        null,
                        null);
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Log.w(TAG, "Clan Cursor: " + DatabaseUtils.dumpCursorToString(data));

        if (data != null && data.moveToFirst()) {
            switch (loader.getId()) {
                case URL_LOADER_CLAN:
                    clanId = data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_BUNGIE_ID));
                    clanName = data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_NAME));
                    clanDesc = data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_DESC));
                    clanIcon = data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_ICON));
                    clanBanner = data.getString(data.getColumnIndexOrThrow(ClanTable.COLUMN_BACKGROUND));
                    prepareDrawerMenu();
                    break;
                case URL_LOADER_USER:
                    if (data.getCount() > 0) {
                        bungieId = data.getString(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
                        userName = data.getString(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_NAME));
                        platformId = data.getInt(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_PLATFORM));
                    }
                    if (allGameList == null) getAllGames();
                    break;
            }
            onDataLoaded();
        }
    }

    private void getAllGames() {
        if (NetworkUtils.checkConnection(this)) {
            if (!isServerServiceRunning()) {
                Bundle bundle = new Bundle();
                bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_ALL_GAMES);
                runServerService(bundle);
            } else Log.w(TAG, "ServerService still running");
        } else {
            Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void prepareDrawerMenu() {

        String[] items = getResources().getStringArray(R.array.menu_item);
        String[] sections = getResources().getStringArray(R.array.menu_section);

        if (selectedDrawerItem < 1) selectedDrawerItem = 1;
        rView = (RecyclerView) findViewById(R.id.drawer_view);
        rAdapter = new DrawerAdapter(this, sections, items, clanIcon, clanName, clanDesc, clanBanner);
        rView.setAdapter(rAdapter);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);
        fm = getSupportFragmentManager();

        final GestureDetector gestureDetector = new GestureDetector(DrawerActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = rView.findChildViewUnder(e.getX(), e.getY());

                if (child != null) {
                    switch (rView.getChildAdapterPosition(child)) {
                        case 0:
                            openMainActivity(child);
                            break;
                        case 1:
                            openMainActivity(child);
                            break;
                        case 2:
                            openNewEventFragment(child);
                            break;
                        case 3:
                            openSearchEventFragment(child);
                            break;
                        case 4:
                            openMyEventsFragment(child);
                            break;
                        case 5:
                            openHistoryFragment(child);
                            break;
                        case 6:
                            break;
                        case 7:
                            openMyClanFragment(child);
                            break;
                        case 8:
                            openMyProfileFragment(child);
                            break;
                        case 9:
                            break;
                        case 10:
                            openConfigFragment(child);
                            break;
                        case 11:
                            openAboutFragment(child);
                            break;
                        case 12:
                            showLogOffDialog(child);
                            break;
                    }
                }
            }
        });

        rView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rView.findChildViewUnder(e.getX(), e.getY());

                if (child != null && gestureDetector.onTouchEvent(e)) {
                    switch (rView.getChildAdapterPosition(child)) {
                        case 0:
                            return openMainActivity(child);
                        case 1:
                            return openMainActivity(child);
                        case 2:
                            return openNewEventFragment(child);
                        case 3:
                            return openSearchEventFragment(child);
                        case 4:
                            return openMyEventsFragment(child);
                        case 5:
                            return openHistoryFragment(child);
                        case 6:
                            return false;
                        case 7:
                            return openMyClanFragment(child);
                        case 8:
                            return openMyProfileFragment(child);
                        case 9:
                            return false;
                        case 10:
                            return openConfigFragment(child);
                        case 11:
                            return openAboutFragment(child);
                        case 12:
                            return showLogOffDialog(child);
                    }
                    return false;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_menu, R.string.close_menu) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

    }

    private boolean showLogOffDialog(View child) {
        if (!hasOpenedDialogs()){
            DialogFragment logOffDialog = new MyAlertDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", MyAlertDialog.LOGOFF_DIALOG);
            logOffDialog.setArguments(bundle);
            logOffDialog.show(getSupportFragmentManager(), "Logoff");
            child.playSoundEffect(SoundEffectConstants.CLICK);
            return true;
        } else {
            Log.w(TAG, "Logoff dialog already open!");
            return false;
        }
    }

    public boolean hasOpenedDialogs() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof MyAlertDialog) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onPositiveClick(String input, int type) {
        if (type == MyAlertDialog.LOGOFF_DIALOG) {
            onLogoff();
        }
    }

    @Override
    public void onDateSent(Calendar date) {
    }

    @Override
    public void onTimeSent(int hour, int minute) {
    }

    @Override
    public void onLogoff() {
        DBHelper database = new DBHelper(getApplicationContext());
        SQLiteDatabase db = database.getWritableDatabase();
        database.onUpgrade(db, 0, 0);
        db.close();
        database.close();

        CookiesUtils.clearCookies();

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PrepareActivity.LEGEND_PREF, getString(R.string.vanguard_data));
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemSelected(String type, String entry, int value) {
    }

    @Override
    public void onMultiItemSelected(boolean[] items) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Bundle bundle = new Bundle();
        switch (resultCode) {
            case BungieService.STATUS_RUNNING:
                onLoadingData();
                break;
            case BungieService.STATUS_FINISHED:
                if (openedFragment instanceof MyClanFragment) {
                    MyClanFragment frag = (MyClanFragment) openedFragment;
                    frag.refreshData();
                }
                onDataLoaded();
                break;
            case BungieService.STATUS_ERROR:
                Log.w(TAG, "Error on BungieService");
                progress.setVisibility(View.GONE);
                bundle.clear();
                bundle.putInt("type", MyAlertDialog.ALERT_DIALOG);
                bundle.putString("title", getString(R.string.error));
                bundle.putString("msg", getString(R.string.unable_update_clan));
                bundle.putString("posButton", getString(R.string.got_it));
                showAlertDialog(bundle);
                onDataLoaded();
                break;
            case LocalService.STATUS_FINISHED:
                Log.w(TAG, "LocalService finished! Calling onMembersUpdated()");
                if (getOpenedFragment() instanceof DetailEventFragment || getOpenedFragment() instanceof DetailValidationFragment){
                    if (userDataListener != null){
                        userDataListener.onMembersUpdated();
                    }
                }
                onDataLoaded();
                break;
            case ServerService.STATUS_RUNNING:
                onLoadingData();
                int requestType = resultData.getInt(ServerService.REQUEST_TAG);
                switch (requestType){
                    case ServerService.TYPE_CREATE_GAME:
                    case ServerService.TYPE_NEW_EVENTS:
                        if (getOpenedFragment() instanceof NewEventFragment){
                            NewEventFragment frag = (NewEventFragment) getOpenedFragment();
                            frag.onServerResponse(requestType);
                        }
                        break;
                    case ServerService.TYPE_DELETE_GAME:
                    case ServerService.TYPE_JOIN_GAME:
                    case ServerService.TYPE_LEAVE_GAME:
                        if (getOpenedFragment() instanceof DetailEventFragment){
                            DetailEventFragment frag = (DetailEventFragment) getOpenedFragment();
                            frag.onServerResponse(requestType);
                        }
                        if (getOpenedFragment() instanceof DetailValidationFragment){
                            DetailValidationFragment frag = (DetailValidationFragment) getOpenedFragment();
                            frag.onServerResponse(requestType);
                        }
                        break;
                    case ServerService.TYPE_VALIDATE_GAME:
                    case ServerService.TYPE_EVALUATE_GAME:
                        if (getOpenedFragment() instanceof DetailValidationFragment){
                            DetailValidationFragment frag = (DetailValidationFragment) getOpenedFragment();
                            frag.onServerResponse(requestType);
                        }
                        break;
                }
                break;
            case ServerService.STATUS_ERROR:
                progress.setVisibility(View.GONE);
                int error = resultData.getInt(ServerService.ERROR_TAG);
                Log.w(TAG, "Error on ServerService");
                switch (error) {
                    case ServerService.ERROR_NO_CONNECTION:
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
                        break;
                    case ServerService.ERROR_NO_EVENT:
                        progress.setVisibility(View.GONE);
                        Bundle dialog = new Bundle();
                        dialog.putInt("type", MyAlertDialog.ALERT_DIALOG);
                        dialog.putString("title", getString(R.string.error));
                        dialog.putString("msg", getString(R.string.no_event_msg));
                        dialog.putString("posButton", getString(R.string.got_it));
                        showAlertDialog(dialog);
                        break;
                    default:
                        progress.setVisibility(View.GONE);
                        Bundle dialogBundle = new Bundle();
                        dialogBundle.putInt("type", MyAlertDialog.ALERT_DIALOG);
                        dialogBundle.putString("title", getString(R.string.error));
                        dialogBundle.putString("msg", getString(R.string.server_error_msg));
                        dialogBundle.putString("posButton", getString(R.string.got_it));
                        showAlertDialog(dialogBundle);
                        break;
                }
                onDataLoaded();
                break;
            case ServerService.STATUS_FINISHED:
                switch (resultData.getInt(ServerService.REQUEST_TAG)) {
                    case ServerService.TYPE_CREATE_GAME:
                        toCreateGame.setGameId(resultData.getInt(ServerService.INT_TAG));
                        onEventCreated(toCreateGame);
                        break;
                    case ServerService.TYPE_HISTORY_GAMES:
                        validatedGameList = new ArrayList<>();
                        validatedGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);
                        Collections.sort(validatedGameList, new HistoryComparator());
                        if (userDataListener != null && getOpenedFragment() instanceof HistoryListFragment) {
                            userDataListener.onGamesLoaded(validatedGameList);
                        } else if (getOpenedFragment() instanceof HistoryListFragment){
                            userDataListener = (UserDataListener) getOpenedFragment();
                            userDataListener.onGamesLoaded(validatedGameList);
                        }
                        break;
                    case ServerService.TYPE_NEW_GAMES:
                        searchGameList = new ArrayList<>();
                        searchGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);
                        if (userDataListener != null && getOpenedFragment() instanceof SearchFragment) {
                            userDataListener.onGamesLoaded(searchGameList);
                        } else if (getOpenedFragment() instanceof SearchFragment){
                            userDataListener = (UserDataListener) getOpenedFragment();
                            userDataListener.onGamesLoaded(searchGameList);
                        }
                        addtoAllGameList(searchGameList);
                        break;
                    case ServerService.TYPE_JOINED_GAMES:
                        joinedGameList = new ArrayList<>();
                        joinedGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);
                        if (userDataListener != null && getOpenedFragment() instanceof MyEventsFragment) {
                            userDataListener.onGamesLoaded(joinedGameList);
                        } else if (getOpenedFragment() instanceof MyEventsFragment){
                            userDataListener = (UserDataListener) getOpenedFragment();
                            userDataListener.onGamesLoaded(joinedGameList);
                        }
                        addtoAllGameList(joinedGameList);
                        break;
                    case ServerService.TYPE_JOIN_GAME:
                        for (int i = 0; i < allGameList.size(); i++) {
                            if (allGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                allGameList.get(i).setJoined(true);
                                allGameList.get(i).setInscriptions(allGameList.get(i).getInscriptions() + 1);
                                break;
                            }
                        }
                        newGameList = getGamesFromList(GameModel.STATUS_NEW);
                        scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                        closeFragment();
                        updateGameList(newEventsListener, newGameList);
                        updateGameList(scheduledEventsListener, scheduledGameList);
                        break;
                    case ServerService.TYPE_LEAVE_GAME:
                        for (int i = 0; i < allGameList.size(); i++) {
                            if (allGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                allGameList.get(i).setJoined(false);
                                allGameList.get(i).setInscriptions(allGameList.get(i).getInscriptions() - 1);
                                break;
                            }
                        }
                        newGameList = getGamesFromList(GameModel.STATUS_NEW);
                        scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                        closeFragment();
                        updateGameList(newEventsListener, newGameList);
                        updateGameList(scheduledEventsListener, scheduledGameList);
                        break;
                    case ServerService.TYPE_DELETE_GAME:
                        for (int i = 0; i < allGameList.size(); i++) {
                            if (allGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                allGameList.remove(i);
                                break;
                            }
                        }
                        newGameList = getGamesFromList(GameModel.STATUS_NEW);
                        scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                        doneGameList = getGamesFromList(GameModel.STATUS_DONE);
                        closeFragment();
                        updateGameList(newEventsListener, newGameList);
                        updateGameList(scheduledEventsListener, scheduledGameList);
                        updateGameList(doneEventsListener, doneGameList);
                        break;
                    case ServerService.TYPE_GAME_ENTRIES:
                        entryList = (ArrayList<MemberModel>) resultData.getSerializable(ServerService.ENTRY_TAG);
                        if (userDataListener != null)
                            userDataListener.onEntriesLoaded(entryList, true, resultData.getInt(ServerService.GAMEID_TAG));
                        if (!isLocalServiceRunning() && entryList != null) updateMembers(entryList);
                        break;
                    case ServerService.TYPE_VALIDATE_GAME:
                        for (int i = 0; i < allGameList.size(); i++) {
                            if (allGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                allGameList.get(i).setStatus(GameModel.STATUS_VALIDATED);
                                allGameList.get(i).setEvaluated(true);
                                if (validatedGameList != null) {
                                    validatedGameList.add(allGameList.get(i));
                                    Collections.sort(validatedGameList,new HistoryComparator());
                                }
                            }
                        }
                        doneGameList = getGamesFromList(GameModel.STATUS_DONE);
                        closeFragment();
                        updateGameList(doneEventsListener, doneGameList);
                        break;
                    case ServerService.TYPE_EVALUATE_GAME:
                        for (int i = 0; i < allGameList.size(); i++) {
                            if (allGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                allGameList.get(i).setEvaluated(true);
                                if (validatedGameList != null) {
                                    validatedGameList.add(allGameList.get(i));
                                    Collections.sort(validatedGameList,new HistoryComparator());
                                }
                                break;
                            }
                        }
                        doneGameList = getGamesFromList(GameModel.STATUS_DONE);
                        closeFragment();
                        updateGameList(doneEventsListener, doneGameList);
                        break;
                    case ServerService.TYPE_HISTORY:
                        historyEntries = (ArrayList<MemberModel>) resultData.getSerializable(ServerService.ENTRY_TAG);
                        if (userDataListener != null && getOpenedFragment() instanceof DetailHistoryFragment) {
                            userDataListener.onEntriesLoaded(historyEntries, true, resultData.getInt(ServerService.GAMEID_TAG));
                        }
                        break;
                    case ServerService.TYPE_ALL_GAMES:
                        Log.w(TAG, "No fragments open. Refreshing main lists");
                        allGameList = new ArrayList<>();
                        allGameList.addAll((ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG));

                        if (allGameList != null) {
                            newGameList = getGamesFromList(GameModel.STATUS_NEW);
                            if (newGameList != null && newEventsListener != null) {
                                updateNewGamesListPref();
                                newEventsListener.onGamesLoaded(newGameList);
                            } else Log.w(TAG, "newGameList is null");

                            scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
                            if (scheduledGameList != null && scheduledEventsListener != null) {
                                scheduledEventsListener.onGamesLoaded(scheduledGameList);
                                if (!isNotifyServiceRunning())
                                    updateNotifications(scheduledGameList);
                            } else Log.w(TAG, "scheduledGameList is null");

                            doneGameList = getGamesFromList(GameModel.STATUS_DONE);
                            if (doneGameList != null && doneEventsListener != null) {
                                doneEventsListener.onGamesLoaded(doneGameList);
                            } else Log.w(TAG, "doneGameList is null");

                            searchGameList = getGamesFromList(GameModel.STATUS_NEW);
                            joinedGameList = getGamesFromList(GameModel.STATUS_JOINED);
                        }
                        break;
                    case ServerService.TYPE_PROFILE:
                        MemberModel member = (MemberModel) resultData.getSerializable(ServerService.PROFILE_TAG);
                        if (member != null) {
                            Log.w(TAG, "member " + member.getName() + " sent by ServerService");
                            memberProfile = member;
                            ArrayList<MemberModel> mList = new ArrayList<>();
                            mList.add(member);
                            if (!isLocalServiceRunning()) updateMembers(mList);
                            if (userDataListener != null && getOpenedFragment() instanceof MyNewProfileFragment) {
                               userDataListener.onMemberLoaded(memberProfile, false);
                            }
                        }
                        break;
                    case ServerService.TYPE_NEW_EVENTS:
                        if (getOpenedFragment() instanceof NewEventFragment){
                            NewEventFragment frag = (NewEventFragment) getOpenedFragment();
                            frag.onServerResponse(0);
                        }
                        break;
                }
                onDataLoaded();
                break;
        }
    }

    private void addtoAllGameList(ArrayList<GameModel> gameList) {
        boolean add = false;
        int added = 0;
        if (allGameList == null) allGameList = new ArrayList<>();
        if (allGameList.size() == 0){
            for (int i=0;i<gameList.size();i++){
                added++;
                allGameList.add(gameList.get(i));
            }
        } else {
            for (int i=0;i<gameList.size();i++){
                for (int x=0;x<allGameList.size();x++){
                    if (allGameList.get(x).getGameId() == gameList.get(i).getGameId()){
                        add = false;
                        break;
                    } else add = true;
                }
                if (add){
                    added++;
                    allGameList.add(gameList.get(i));
                }
            }
        }
        if (added >0){
            Collections.sort(allGameList, new GameComparator());
            newGameList = getGamesFromList(GameModel.STATUS_NEW);
            if (newEventsListener != null) newEventsListener.onGamesLoaded(newGameList);
            scheduledGameList = getGamesFromList(GameModel.STATUS_SCHEDULED);
            if (scheduledEventsListener != null) scheduledEventsListener.onGamesLoaded(scheduledGameList);
        } else Log.w(TAG, "No game added to any lists");
    }

    private void updateGameList(UserDataListener listener, ArrayList<GameModel> list) {
        if (listener != null) listener.onGamesLoaded(list);
    }

    private void updateNewGamesListPref() {

        ArrayList<Integer> selectedIds = new ArrayList<>();
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String gameIds = "";
        ArrayList<GameModel> gameList = getGamesFromList(GameModel.STATUS_NEW);

        if (gameList != null && gameList.size() > 0) {
            int typeIds[] = getResources().getIntArray(R.array.event_type_ids);
            for (int typeId : typeIds) {
                boolean b = sharedPrefs.getBoolean(String.valueOf(typeId), false);
                if (b) {
                    selectedIds.add(typeId);
                }
            }

            for (int i = 0; i < gameList.size(); i++) {
                for (int x = 0; x < selectedIds.size(); x++) {
                    Log.w(TAG, "Comparing game.typeId: " + gameList.get(i).getTypeId() + " with selectedTypeId: " + selectedIds.get(x));
                    if (gameList.get(i).getTypeId() == selectedIds.get(x)) {
                        gameIds = gameIds + String.valueOf(gameList.get(i).getGameId()) + ",";
                    }
                }
            }

            if (!gameIds.equals("")) {
                Log.w(TAG, "Saving string: " + gameIds);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(DrawerActivity.NEW_GAMES_PREF, gameIds);
                editor.apply();
            }
        }

    }

    private void updateNotifications(ArrayList<GameModel> scheduledGameList) {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false)) {
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CreateNotificationService.class);
            intent.putExtra(CreateNotificationService.GAME_HEADER, scheduledGameList);
            startService(intent);
        }
    }

    @Override
    public void updateMembers(List<MemberModel> list) {
        if (mReceiver == null) mReceiver = new RequestResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, LocalService.class);
        intent.putExtra(ServerService.RECEIVER_TAG, mReceiver);
        intent.putExtra(LocalService.REQUEST_HEADER, LocalService.TYPE_UPDATE_MEMBERS);
        intent.putExtra(LocalService.MEMBERS_HEADER, (Serializable) list);
        intent.putExtra(LocalService.CLAN_HEADER, clanId);
        startService(intent);
    }

    @Override
    public void registerNewGamesAlarm() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(NEW_NOTIFY_PREF, false)) {
            int interval = sharedPrefs.getInt(NEW_NOTIFY_TIME_PREF, DEFAULT_INTERVAL);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_NEW_NOTIFICATIONS);
            intent.putExtra("memberId", bungieId);
            intent.putExtra("platformId", platformId);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Intent nIntent = new Intent(this, DrawerActivity.class);
                nIntent.putExtra("isFromNews", true);
                PendingIntent npIntent = PendingIntent.getActivity(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo aC = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + interval, npIntent);
                alarm.setAlarmClock(aC, pIntent);
            } else alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pIntent);
            Log.w(TAG, "New game alarm registered in an interval of " + interval + " millis");
        }
    }

    @Override
    public void deleteNewGamesAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_NEW_NOTIFICATIONS);
        intent.putExtra("memberId", bungieId);
        intent.putExtra("platformId", platformId);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.w(TAG, "New game alarm deleted.");

    }

    @Override
    public MemberModel getMemberProfile() {
        return memberProfile;
    }

    @Override
    public void callAndroidIntent(int type, String text) {
        switch (type){
            case TYPE_BROWSER_INTENT:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
                startActivity(intent);
                break;
            case TYPE_EMAIL_INTENT:
                Intent eIntent = new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:" + text));
                startActivity(eIntent);
                break;
        }
    }

    private ArrayList<GameModel> getGamesFromList(int type) {
        ArrayList<GameModel> result = new ArrayList<>();
        if (allGameList != null) {
            switch (type) {
                case GameModel.STATUS_NEW:
                case GameModel.STATUS_AVAILABLE:
                    for (int i = 0; i < allGameList.size(); i++) {
                        if (allGameList.get(i).getStatus() == GameModel.STATUS_NEW && !allGameList.get(i).isJoined()) {
                            result.add(allGameList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                case GameModel.STATUS_SCHEDULED:
                    for (int i = 0; i < allGameList.size(); i++) {
                        if (allGameList.get(i).getStatus() == GameModel.STATUS_NEW && allGameList.get(i).isJoined()) {
                            result.add(allGameList.get(i));
                        }
                    }
                    return result;
                case GameModel.STATUS_DONE:
                    for (int i = 0; i < allGameList.size(); i++) {
                        if (allGameList.get(i).getStatus() > 0 && allGameList.get(i).isJoined() && !allGameList.get(i).isEvaluated()) {
                            result.add(allGameList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    Collections.sort(result, new DoneGameComparator());
                    return result;
                case GameModel.STATUS_VALIDATED:
                    for (int i = 0; i < allGameList.size(); i++) {
                        if (allGameList.get(i).getStatus() == 2 && allGameList.get(i).isJoined() && allGameList.get(i).isEvaluated()) {
                            result.add(allGameList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                case GameModel.STATUS_JOINED:
                    for (int i = 0; i < allGameList.size(); i++) {
                        if (allGameList.get(i).isJoined() && !allGameList.get(i).isEvaluated()) {
                            if (allGameList.get(i).getStatus() == 2) {
                                if (!allGameList.get(i).getCreatorId().equals(bungieId)) {
                                    result.add(allGameList.get(i));
                                }
                            } else {
                                result.add(allGameList.get(i));
                            }
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                default:
                    return new ArrayList<>();
            }
        } else {
            Log.w(TAG, "gameList is null");
            return new ArrayList<>();
        }
    }

    private void showAlertDialog(Bundle dialogBundle) {
        DialogFragment dialog = new MyAlertDialog();
        dialog.setArguments(dialogBundle);
        dialog.show(getSupportFragmentManager(), "alert");
    }

    private boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(BungieService.RUNNING_SERVICE, false);
    }

    public boolean isServerServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(ServerService.RUNNING_SERVICE, false);
    }

    public boolean isLocalServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(LocalService.RUNNING_SERVICE, false);
    }

    public boolean isNotifyServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(CreateNotificationService.RUNNING_SERVICE, false);
    }

    public class DoneGameComparator implements Comparator<GameModel>{
        @Override
        public int compare(GameModel game1, GameModel game2) {
            return game1.getStatus() -  game2.getStatus();
        }
    }

    public class GameComparator implements Comparator<GameModel> {
        @Override
        public int compare(GameModel game1, GameModel game2) {
            return (int) (DateUtils.stringToDate(game1.getTime()).getTimeInMillis() - DateUtils.stringToDate(game2.getTime()).getTimeInMillis());
        }
    }

    public class HistoryComparator implements Comparator<GameModel> {
        @Override
        public int compare(GameModel game1, GameModel game2) {
            return (int) (DateUtils.stringToDate(game2.getTime()).getTimeInMillis() - DateUtils.stringToDate(game1.getTime()).getTimeInMillis());
        }
    }

}


