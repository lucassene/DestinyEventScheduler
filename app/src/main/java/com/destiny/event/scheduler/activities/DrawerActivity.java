package com.destiny.event.scheduler.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
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
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.fragments.AboutSettingsFragment;
import com.destiny.event.scheduler.fragments.DBViewerFragment;
import com.destiny.event.scheduler.fragments.DetailEventFragment;
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
    public static final String FOREGROUND_PREF = "isForeground";
    public static final String DOWNLOAD_PREF = "downloadList";
    public static final String COOKIES_PREF = "cookies";
    public static final String XCSRF_PREF = "xcsrf";

    public static final int FRAGMENT_TYPE_WITHOUT_BACKSTACK = 0;
    public static final int FRAGMENT_TYPE_WITH_BACKSTACK = 1;

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
    private UserDataListener searchEventsListener;
    private UserDataListener myEventsListener;
    private UserDataListener validatedEventsListener;

    private FragmentTransaction ft;
    private FragmentManager fm;
    private Fragment openedFragment;
    private int openedFragmentType;
    private String fragmentTag;
    //private ArrayList<String> backStackList;

    private String clanName;
    private String clanId;
    private String clanIcon;
    private String clanBanner;
    private String clanDesc;

    private String bungieId;
    private String userName;
    private int platformId;

    private String clanOrderBy;

    private int isNewScheduledOrValidated = GameTable.STATUS_NEW;
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
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicatorColor);
            }
        });

        if (selectedTabFragment != 0) viewPager.setCurrentItem(selectedTabFragment);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            //Log.w("DrawerActivity", "Fragment BackStack Count: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
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
            //Toast.makeText(this, "FragmentType: " + openedFragmentType + " FragmentTag: " + fragmentTag, Toast.LENGTH_SHORT).show();
        }

        getClanData();
        getLoggedUserData();

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
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_refresh:
                if (openedFragment instanceof MyClanFragment) {
                    MyClanFragment frag = (MyClanFragment) openedFragment;
                    ArrayList<String> idList = frag.getBungieIdList();
                    if (idList != null) {
                        if (mReceiver == null) {
                            mReceiver = new RequestResultReceiver(new Handler());
                            mReceiver.setReceiver(this);
                        }
                        if (NetworkUtils.checkConnection(this)) {
                            if (!isBungieServiceRunning()) {
                                onLoadingData();
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
                        } else
                            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
                    } else Log.w(TAG, "bungieIdList is empty!");
                } else refreshLists();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshLists() {
        Bundle bundle = new Bundle();
        if (openedFragment instanceof MyEventsFragment) {
            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_JOINED_GAMES);
        }
        if (openedFragment instanceof SearchFragment) {
            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_NEW_GAMES);
        }
        if (openedFragment instanceof HistoryListFragment) {
            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_HISTORY_GAMES);
        }
        if (openedFragment == null) {
            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_ALL_GAMES);
        }
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Log.w(TAG, "Método onNewIntent chamado");
        refreshLists();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //Toast.makeText(this, "Back Count: " + fm.getBackStackEntryCount(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "prior openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
            if (fragmentTag == null) {
                finish();
            } else if (openedFragmentType == FRAGMENT_TYPE_WITHOUT_BACKSTACK) {
                openMainActivity(null);
                switch (isNewScheduledOrValidated) {
                    case GameTable.STATUS_NEW:
                        viewPager.setCurrentItem(0);
                        break;
                    case GameTable.STATUS_SCHEDULED:
                        if (hasScheduledGames) {
                            viewPager.setCurrentItem(1);
                        } else viewPager.setCurrentItem(0);
                        break;
                    case GameTable.STATUS_VALIDATED:
                        if (hasValidatedGames) {
                            viewPager.setCurrentItem(2);
                        } else viewPager.setCurrentItem(0);
                        break;
                    default:
                        viewPager.setCurrentItem(0);
                        break;
                }
                //Toast.makeText(this, "openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
                openedFragment = fm.findFragmentById(R.id.content_frame);
                if (openedFragment != null) fragmentTag = openedFragment.getTag();
                //Toast.makeText(this, "openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
                //openedFragmentType = FRAGMENT_TYPE_WITHOUT_BACKSTACK;
            }
        }
    }

    public boolean openMainActivity(View child) {
        if (openedFragment != null) {
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
        outState.putSerializable("allGameList", allGameList);
        outState.putString("fragTag", fragmentTag);
        outState.putInt("fragType", openedFragmentType);
        outState.putSerializable("entryList", entryList);
        outState.putString("bungieId", bungieId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        readyAllLists(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    private void readyAllLists(Bundle savedInstanceState){
        allGameList = (ArrayList<GameModel>) savedInstanceState.getSerializable("allGameList");
        if (allGameList != null) {
            newGameList = getGamesFromList(allGameList, GameTable.STATUS_NEW);
            if (newEventsListener != null) newEventsListener.onGamesLoaded(newGameList);

            scheduledGameList = getGamesFromList(allGameList, GameTable.STATUS_SCHEDULED);
            if (scheduledEventsListener != null) scheduledEventsListener.onGamesLoaded(scheduledGameList);

            doneGameList = getGamesFromList(allGameList, GameTable.STATUS_DONE);
            if (doneEventsListener != null) doneEventsListener.onGamesLoaded(doneGameList);

            searchGameList = getGamesFromList(allGameList, GameTable.STATUS_AVAILABLE);
            validatedGameList = getGamesFromList(allGameList, GameTable.STATUS_VALIDATED);
            joinedGameList = getGamesFromList(allGameList, GameTable.STATUS_JOINED);
        }
        entryList = (ArrayList<MemberModel>) savedInstanceState.getSerializable("entryList");
        bungieId = savedInstanceState.getString("bungieId");
    }

    private void prepareFragmentHolder(Fragment fragment, View child, Bundle bundle, String tag) {
        drawerLayout.closeDrawers();
        tabLayout.setViewPager(null);
        viewPager.setAdapter(null);
        child.playSoundEffect(SoundEffectConstants.CLICK);
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
        //viewPager.setCurrentItem(1);
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
        ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, fragment, tag);
        ft.addToBackStack(null);
        ft.commit();
        fragmentTag = tag;
        //Toast.makeText(this, "openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
        openedFragment = fragment;
        rAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    @Override
    public void onEventTypeSelected(String id) {
        fm.popBackStack();
        newEventListener = (FromActivityListener) getSupportFragmentManager().findFragmentByTag("new");
        newEventListener.onEventTypeSent(id);
    }

    @Override
    public void onEventGameSelected(String id) {
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
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putString("origin", tag);
        bundle.putSerializable("game", game);

        switch (game.getStatus()){
            case GameTable.STATUS_NEW:
                fragment = new DetailEventFragment();
                if (game.isJoined()){
                    isNewScheduledOrValidated = GameTable.STATUS_NEW;
                } else isNewScheduledOrValidated = GameTable.STATUS_SCHEDULED;
                break;
            case GameTable.STATUS_WAITING:
            case GameTable.STATUS_VALIDATED:
                isNewScheduledOrValidated = GameTable.STATUS_VALIDATED;
                fragment = new DetailValidationFragment();
                break;
            default:
                fragment = new DetailEventFragment();
                isNewScheduledOrValidated = GameTable.STATUS_NEW;
                break;
        }

/*        switch (tag) {
            case ValidateListFragment.TAG:
                isNewScheduledOrValidated = GameTable.STATUS_VALIDATED;
                fragment = new DetailValidationFragment();
                break;
            case HistoryListFragment.TAG:
                isNewScheduledOrValidated = GameTable.STATUS_NEW;
                fragment = new DetailHistoryFragment();
                break;
            case NewEventsListFragment.TAG:
                fragment = new DetailEventFragment();
                isNewScheduledOrValidated = GameTable.STATUS_NEW;
                break;
            case ScheduledListFragment.TAG:
                fragment = new DetailEventFragment();
                isNewScheduledOrValidated = GameTable.STATUS_SCHEDULED;
                break;
            default:
                fragment = new DetailEventFragment();
                isNewScheduledOrValidated = GameTable.STATUS_NEW;
                break;
        }*/

        tabLayout.setViewPager(null);
        viewPager.setAdapter(null);
        loadNewFragment(fragment, bundle, "game");

    }

    @Override
    public void onScheduledGames(boolean status) {
        onDataLoaded();
        hasScheduledGames = status;
    }

    @Override
    public void onValidateGames(boolean status) {
        onDataLoaded();
        hasValidatedGames = status;
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
            PendingIntent pIntent = PendingIntent.getBroadcast(this, firstId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, firstNotification.getTimeInMillis(), pIntent);
            Log.w(TAG, "requestId: " + firstId + " registered!");
        }

        if (secondId != 0) {
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent psIntent = PendingIntent.getBroadcast(this, secondId, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, secondNotification.getTimeInMillis(), psIntent);
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
        }
        if (fragment instanceof ScheduledListFragment) {
            scheduledEventsListener = (UserDataListener) fragment;
        }
        if (fragment instanceof ValidateListFragment) {
            doneEventsListener = (UserDataListener) fragment;
        }
        if (fragment instanceof SearchFragment) {
            searchEventsListener = (UserDataListener) fragment;
        }
        if (fragment instanceof MyEventsFragment) {
            myEventsListener = (UserDataListener) fragment;
        }
        if (fragment instanceof HistoryListFragment) {
            validatedEventsListener = (UserDataListener) fragment;
        }
    }

    @Override
    public void deleteUserDataListener(Fragment fragment) {
        if (fragment instanceof NewEventsListFragment) {
            newEventsListener = null;
        }
        if (fragment instanceof ScheduledListFragment) {
            scheduledEventsListener = null;
        }
        if (fragment instanceof ValidateListFragment) {
            doneEventsListener = null;
        }
        if (fragment instanceof SearchFragment) {
            searchEventsListener = null;
        }
        if (fragment instanceof MyEventsFragment) {
            myEventsListener = null;
        }
        if (fragment instanceof HistoryListFragment) {
            validatedEventsListener = null;
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
        scheduledGameList.add(game);
        if (scheduledEventsListener != null){
            scheduledEventsListener.onGamesLoaded(scheduledGameList);
        }
        viewPager.setCurrentItem(1);
    }

    @Override
    public void runServerService(Bundle bundle) {
        if (NetworkUtils.checkConnection(this)) {
            //if (!isServerServiceRunning()){
            onLoadingData();
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
            intent.putExtra(ServerService.REQUEST_TAG, bundle.getInt(ServerService.REQUEST_TAG));
            intent.putExtra(ServerService.RECEIVER_TAG, mReceiver);
            intent.putExtra(ServerService.MEMBER_TAG, bungieId);
            intent.putExtra(ServerService.PLATFORM_TAG, platformId);

            if (bundle.containsKey(ServerService.EVENT_TAG))
                intent.putExtra(ServerService.EVENT_TAG, bundle.getInt(ServerService.EVENT_TAG));
            if (bundle.containsKey(ServerService.TIME_TAG))
                intent.putExtra(ServerService.TIME_TAG, bundle.getString(ServerService.TIME_TAG));
            if (bundle.containsKey(ServerService.LIGHT_TAG))
                intent.putExtra(ServerService.LIGHT_TAG, bundle.getInt(ServerService.LIGHT_TAG));
            if (bundle.containsKey(ServerService.GAMEID_TAG))
                intent.putExtra(ServerService.GAMEID_TAG, bundle.getInt(ServerService.GAMEID_TAG));
            if (bundle.containsKey(ServerService.ENTRY_TAG))
                intent.putStringArrayListExtra(ServerService.ENTRY_TAG, bundle.getStringArrayList(ServerService.ENTRY_TAG));
            if (bundle.containsKey(ServerService.EVALUATIONS_TAG))
                intent.putParcelableArrayListExtra(ServerService.EVALUATIONS_TAG, bundle.getParcelableArrayList(ServerService.EVALUATIONS_TAG));

            startService(intent);
            //}
        } else Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<GameModel> getGameList(int type) {
        switch (type) {
            case GameTable.STATUS_NEW:
                return newGameList;
            case GameTable.STATUS_SCHEDULED:
                return scheduledGameList;
            case GameTable.STATUS_DONE:
                return doneGameList;
            case GameTable.STATUS_AVAILABLE:
                return searchGameList;
            case GameTable.STATUS_JOINED:
                return joinedGameList;
            case GameTable.STATUS_VALIDATED:
                return validatedGameList;
            default:
                return null;
        }
    }

    @Override
    public void getGameEntries(int gameId) {
        if (entryList == null){
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
            if (openedFragment instanceof DetailEventFragment) {
                DetailEventFragment frag = (DetailEventFragment) openedFragment;
                frag.onEntriesLoaded(entryList, false);
            }
            if (openedFragment instanceof DetailValidationFragment) {
                DetailValidationFragment frag = (DetailValidationFragment) openedFragment;
                frag.onEntriesLoaded(entryList, false);
            }
        }
    }

    @Override
    public void updateGameStatus(GameModel game, int status) {
        switch (status) {
            case GameTable.STATUS_NEW:
                for (int i = 0; i < newGameList.size(); i++) {
                    if (game.getGameId() == newGameList.get(i).getGameId()) {
                        newGameList.remove(i);
                        break;
                    }
                }
                break;
            case GameTable.STATUS_SCHEDULED:
                for (int i = 0; i < scheduledGameList.size(); i++) {
                    if (scheduledGameList.get(i).getGameId() == game.getGameId()) {
                        game.setStatus(GameTable.STATUS_WAITING);
                        doneGameList.add(game);
                        Collections.sort(doneGameList, new GameComparator());
                        scheduledGameList.remove(i);
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void updateGameEntries(int status, int gameId, int entries) {
        switch (status){
            case GameTable.STATUS_DONE:
                for (int i=0;i<doneGameList.size();i++){
                    if (doneGameList.get(i).getGameId() == gameId){
                        doneGameList.get(i).setInscriptions(entries);
                        break;
                    }
                }
                break;
            case GameTable.STATUS_SCHEDULED:
                for (int i=0;i<scheduledGameList.size();i++){
                    if (scheduledGameList.get(i).getGameId() == gameId){
                        scheduledGameList.get(i).setInscriptions(entries);
                        break;
                    }
                }
                break;
            case GameTable.STATUS_NEW:
                for (int i=0;i<newGameList.size();i++){
                    if (newGameList.get(i).getGameId() == gameId){
                        newGameList.get(i).setInscriptions(entries);
                        break;
                    }
                }
                break;
        }
    }

    public boolean openNewEventFragment(View child) {
        if (openedFragment instanceof NewEventFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        NewEventFragment fragment = new NewEventFragment();
        prepareFragmentHolder(fragment, child, null, "new");
        return true;
    }

    public boolean openSearchEventFragment(View child) {
        if (openedFragment instanceof SearchFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        SearchFragment fragment = new SearchFragment();
        prepareFragmentHolder(fragment, child, null, "search");
        return true;
    }

    private boolean openMyEventsFragment(View child) {
        if (openedFragment instanceof MyEventsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        MyEventsFragment fragment = new MyEventsFragment();
        prepareFragmentHolder(fragment, child, null, "myevents");
        return true;

    }

    public boolean openHistoryFragment(View child) {
        if (openedFragment instanceof HistoryListFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        HistoryListFragment fragment = new HistoryListFragment();
        prepareFragmentHolder(fragment, child, null, "history");
        return true;
    }

    public boolean openMyClanFragment(View child) {
        if (openedFragment instanceof MyClanFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
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
        if (openedFragment instanceof MyNewProfileFragment) {
            drawerLayout.closeDrawers();
            return false;
        }
        MyNewProfileFragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putString("clanName", clanName);
        bundle.putInt("type", MyNewProfileFragment.TYPE_MENU);

        prepareFragmentHolder(fragment, child, bundle, "profile");
        return true;
    }

    public boolean openConfigFragment(View child) {
        if (openedFragment instanceof MainSettingsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }

        MainSettingsFragment fragment = new MainSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "config");
        return true;

    }

    private boolean openAboutFragment(View child) {
        if (openedFragment instanceof AboutSettingsFragment) {
            drawerLayout.closeDrawers();
            return false;
        }

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
        TypedArray icons = getResources().obtainTypedArray(R.array.menu_icons);

        String header = getResources().getString(R.string.def_clan_header);
        String[] sections = getResources().getStringArray(R.array.menu_section);

        selectedDrawerItem = 1;
        rView = (RecyclerView) findViewById(R.id.drawer_view);
        rAdapter = new DrawerAdapter(this, header, sections, icons, items, clanIcon, clanName, clanDesc, clanBanner);
        rView.setAdapter(rAdapter);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);

        fm = getSupportFragmentManager();
        //fm.addOnBackStackChangedListener(this);

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
                            selectedDrawerItem = 1;
                            openMainActivity(child);
                            break;
                        case 1:
                            selectedDrawerItem = 1;
                            openMainActivity(child);
                            break;
                        case 2:
                            selectedDrawerItem = 2;
                            openNewEventFragment(child);
                            break;
                        case 3:
                            selectedDrawerItem = 3;
                            openSearchEventFragment(child);
                            break;
                        case 4:
                            selectedDrawerItem = 4;
                            openMyEventsFragment(child);
                            break;
                        case 5:
                            selectedDrawerItem = 5;
                            openHistoryFragment(child);
                            break;
                        case 6:
                            break;
                        case 7:
                            selectedDrawerItem = 7;
                            openMyClanFragment(child);
                            break;
                        case 8:
                            selectedDrawerItem = 8;
                            openMyProfileFragment(child);
                            break;
                        case 9:
                            break;
                        case 10:
                            selectedDrawerItem = 10;
                            openConfigFragment(child);
                            break;
                        case 11:
                            selectedDrawerItem = 11;
                            openDBViewerFragment(child);
                            break;
                        case 12:
                            selectedDrawerItem = 12;
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
                            selectedDrawerItem = 1;
                            return openMainActivity(child);
                        case 1:
                            selectedDrawerItem = 1;
                            return openMainActivity(child);
                        case 2:
                            selectedDrawerItem = 2;
                            return openNewEventFragment(child);
                        case 3:
                            selectedDrawerItem = 3;
                            return openSearchEventFragment(child);
                        case 4:
                            selectedDrawerItem = 4;
                            return openMyEventsFragment(child);
                        case 5:
                            selectedDrawerItem = 5;
                            return openHistoryFragment(child);
                        case 6:
                            return false;
                        case 7:
                            selectedDrawerItem = 7;
                            return openMyClanFragment(child);
                        case 8:
                            selectedDrawerItem = 8;
                            return openMyProfileFragment(child);
                        case 9:
                            return false;
                        case 10:
                            selectedDrawerItem = 10;
                            return openConfigFragment(child);
                        case 11:
                            selectedDrawerItem = 11;
                            return openAboutFragment(child);
                        case 12:
                            selectedDrawerItem = 12;
                            return showLogOffDialog(child);
                    }
                    return true;
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
        DialogFragment logOffDialog = new MyAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("type", 0);
        logOffDialog.setArguments(bundle);
        logOffDialog.show(getSupportFragmentManager(), "Logoff");
        child.playSoundEffect(SoundEffectConstants.CLICK);
        return true;
    }

    @Override
    public void onPositiveClick(String input, int type) {

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
    public void onItemSelected(String entry, int value) {

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
                //Toast.makeText(this, R.string.clan_updated, Toast.LENGTH_SHORT).show();
                //onDataLoaded();
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
                    case ServerService.ERROR_HTTP_REQUEST:
                    case ServerService.ERROR_INCORRECT_REQUEST:
                    case ServerService.ERROR_INCORRECT_RESPONSE:
                    case ServerService.ERROR_NULL_RESPONSE:
                    case ServerService.ERROR_RESPONSE_CODE:
                        progress.setVisibility(View.GONE);
                        Bundle dialogBundle = new Bundle();
                        dialogBundle.putInt("type", MyAlertDialog.ALERT_DIALOG);
                        dialogBundle.putString("title", getString(R.string.error));
                        dialogBundle.putString("msg", getString(R.string.server_error_msg));
                        dialogBundle.putString("posButton", getString(R.string.got_it));
                        showAlertDialog(dialogBundle);
                        break;
                }
                break;
            case ServerService.STATUS_FINISHED:
                if (openedFragment instanceof NewEventFragment) {
                    NewEventFragment frag = (NewEventFragment) openedFragment;
                    int gameId = resultData.getInt(ServerService.INT_TAG);
                    frag.createLocalEvent(gameId);
                }
                if (openedFragment instanceof SearchFragment) {
                    if (searchEventsListener != null) {
                        searchGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);
                        searchEventsListener.onGamesLoaded(searchGameList);
                    }
                }
                if (openedFragment instanceof MyEventsFragment) {
                    if (myEventsListener != null) {
                        joinedGameList = getGamesFromList((ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG),GameTable.STATUS_JOINED);
                        myEventsListener.onGamesLoaded(joinedGameList);
                    }
                }
                if (openedFragment instanceof HistoryListFragment) {
                    Log.w(TAG, "fragment instance of HistoryListFragment");
                    if (validatedEventsListener != null) {
                        validatedGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);
                        validatedEventsListener.onGamesLoaded(validatedGameList);
                    } else Log.w(TAG, "validatedEventListener is null");
                }
                if (openedFragment instanceof DetailEventFragment) {
                    if (resultData.containsKey(ServerService.REQUEST_TAG) && resultData.containsKey(ServerService.INT_TAG)) {
                        switch (resultData.getInt(ServerService.REQUEST_TAG)) {
                            case ServerService.TYPE_JOIN_GAME:
                                for (int i = 0; i < newGameList.size(); i++) {
                                    if (newGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                        newGameList.get(i).setJoined(true);
                                        newGameList.get(i).setInscriptions(newGameList.get(i).getInscriptions() + 1);
                                        scheduledGameList.add(newGameList.get(i));
                                        Collections.sort(scheduledGameList, new GameComparator());
                                        newGameList.remove(i);
                                        break;
                                    }
                                }
                                break;
                            case ServerService.TYPE_LEAVE_GAME:
                                for (int i = 0; i < scheduledGameList.size(); i++) {
                                    if (scheduledGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                        scheduledGameList.get(i).setJoined(false);
                                        scheduledGameList.get(i).setInscriptions(scheduledGameList.get(i).getInscriptions() - 1);
                                        newGameList.add(scheduledGameList.get(i));
                                        Collections.sort(newGameList, new GameComparator());
                                        scheduledGameList.remove(i);
                                        break;
                                    }
                                }
                                break;
                            case ServerService.TYPE_DELETE_GAME:
                                for (int i = 0; i < scheduledGameList.size(); i++) {
                                    if (scheduledGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                        scheduledGameList.remove(i);
                                        break;
                                    }
                                }
                                break;
                        }
                        closeFragment();
                    } else {
                        DetailEventFragment frag = (DetailEventFragment) openedFragment;
                        entryList = (ArrayList<MemberModel>) resultData.getSerializable(ServerService.ENTRY_TAG);
                        frag.onEntriesLoaded(entryList, true);
                        if (!isLocalServiceRunning() && entryList != null) updateMembers(entryList);
                    }
                }
                if (openedFragment instanceof DetailValidationFragment) {
                    if (resultData.containsKey(ServerService.REQUEST_TAG) && resultData.containsKey(ServerService.INT_TAG)) {
                        switch (resultData.getInt(ServerService.REQUEST_TAG)) {
                            case ServerService.TYPE_VALIDATE_GAME:
                            case ServerService.TYPE_DELETE_GAME:
                            case ServerService.TYPE_EVALUATE_GAME:
                                for (int i = 0; i < doneGameList.size(); i++) {
                                    if (doneGameList.get(i).getGameId() == resultData.getInt(ServerService.INT_TAG)) {
                                        doneGameList.remove(i);
                                    }
                                }
                                break;
                        }
                        closeFragment();
                    } else {
                        DetailValidationFragment frag = (DetailValidationFragment) openedFragment;
                        entryList = (ArrayList<MemberModel>) resultData.getSerializable(ServerService.ENTRY_TAG);
                        frag.onEntriesLoaded(entryList, true);
                        if (!isLocalServiceRunning() && entryList != null) updateMembers(entryList);
                    }
                }
                if (openedFragment == null) {
                    Log.w(TAG, "No fragments open. Refreshing main lists");
                    allGameList = (ArrayList<GameModel>) resultData.getSerializable(ServerService.GAME_TAG);

                    if (allGameList != null) {
                        newGameList = getGamesFromList(allGameList, GameTable.STATUS_NEW);
                        if (newGameList != null && newEventsListener != null) {
                            newEventsListener.onGamesLoaded(newGameList);
                        } else Log.w(TAG, "newGameList is null");

                        scheduledGameList = getGamesFromList(allGameList, GameTable.STATUS_SCHEDULED);
                        if (scheduledGameList != null && scheduledEventsListener != null) {
                            scheduledEventsListener.onGamesLoaded(scheduledGameList);
                            if (!isNotifyServiceRunning()) updateNotifications(scheduledGameList);
                        } else Log.w(TAG, "scheduledGameList is null");

                        doneGameList = getGamesFromList(allGameList, GameTable.STATUS_DONE);
                        if (doneGameList != null && doneEventsListener != null) {
                            doneEventsListener.onGamesLoaded(doneGameList);
                        } else Log.w(TAG, "doneGameList is null");

                        if (searchGameList != null) { searchGameList = getGamesFromList(allGameList, GameTable.STATUS_NEW); }
                        if (joinedGameList != null) { joinedGameList = getGamesFromList(allGameList, GameTable.STATUS_JOINED); }
                        if (validatedGameList != null) { validatedGameList = getGamesFromList(allGameList, GameTable.STATUS_VALIDATED); }
                    }
                }
                onDataLoaded();
                break;
        }
    }

    private void updateNotifications(ArrayList<GameModel> scheduledGameList) {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, false)){
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CreateNotificationService.class);
            intent.putExtra(CreateNotificationService.GAME_HEADER, scheduledGameList);
            startService(intent);
        }
    }

    @Override
    public void updateMembers(List<MemberModel> list) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, LocalService.class);
        intent.putExtra(LocalService.REQUEST_HEADER, LocalService.TYPE_UPDATE_MEMBERS);
        intent.putExtra(LocalService.MEMBERS_HEADER, (Serializable) list);
        intent.putExtra(LocalService.CLAN_HEADER, clanId);
        startService(intent);
    }

    private ArrayList<GameModel> getGamesFromList(ArrayList<GameModel> gList, int type) {
        ArrayList<GameModel> result = new ArrayList<>();
        if (gList != null) {
            switch (type) {
                case GameTable.STATUS_NEW:
                case GameTable.STATUS_AVAILABLE:
                    for (int i = 0; i < gList.size(); i++) {
                        if (gList.get(i).getStatus() == GameTable.STATUS_NEW && !gList.get(i).isJoined()) {
                            result.add(gList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                case GameTable.STATUS_SCHEDULED:
                    for (int i = 0; i < gList.size(); i++) {
                        if (gList.get(i).getStatus() == GameTable.STATUS_NEW && gList.get(i).isJoined()) {
                            result.add(gList.get(i));
                        }
                    }
                    return result;
                case GameTable.STATUS_DONE:
                    for (int i = 0; i < gList.size(); i++) {
                        if (gList.get(i).getStatus() > 0 && gList.get(i).isJoined() && !gList.get(i).isEvaluated()) {
                            result.add(gList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                case GameTable.STATUS_VALIDATED:
                    for (int i = 0; i < gList.size(); i++) {
                        if (gList.get(i).getStatus() == 2 && gList.get(i).isJoined() && gList.get(i).isEvaluated()) {
                            result.add(gList.get(i));
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                case GameTable.STATUS_JOINED:
                    for (int i=0;i<gList.size();i++){
                        if (gList.get(i).isJoined() && !gList.get(i).isEvaluated()){
                            if (gList.get(i).getStatus() == 2){
                                if (!gList.get(i).getCreatorId().equals(bungieId)){
                                    result.add(gList.get(i));
                                }
                            } else {
                                result.add(gList.get(i));
                            }
                        }
                    }
                    Collections.sort(result, new GameComparator());
                    return result;
                default:
                    return null;
            }
        } else {
            Log.w(TAG, "gameList is null");
            return null;
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

    public class GameComparator implements Comparator<GameModel> {

        @Override
        public int compare(GameModel game1, GameModel game2) {
            return (int) (DateUtils.stringToDate(game1.getTime()).getTimeInMillis() - DateUtils.stringToDate(game2.getTime()).getTimeInMillis());
        }
    }

}


