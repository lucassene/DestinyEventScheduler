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
import com.destiny.event.scheduler.adapters.ViewPageAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
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
import com.destiny.event.scheduler.fragments.SearchFragment;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.AlarmReceiver;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.RequestResultReceiver;
import com.destiny.event.scheduler.utils.CookiesUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;
import com.destiny.event.scheduler.views.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class DrawerActivity extends AppCompatActivity implements ToActivityListener, LoaderManager.LoaderCallbacks<Cursor>, OnEventCreatedListener, FromDialogListener, RequestResultReceiver.Receiver{

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
    private ArrayList<RefreshDataListener> refreshDataListenerList;
    private ArrayList<UserDataListener> userDataListener;

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

    private String isNewScheduledOrValidated = GameTable.STATUS_NEW;
    private boolean hasScheduledGames = true;
    private boolean hasValidatedGames = true;

    ViewPager viewPager;
    ViewPageAdapter viewPageAdapter;
    SlidingTabLayout tabLayout;
    private int selectedTabFragment;

    TextView toolbarTitle;

    private Bundle spinnerSelections = new Bundle();

    private int selectedDrawerItem;

    RequestResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_layout);

        progress = (ProgressBar) findViewById(R.id.progress_bar);

        getClanData();
        getLoggedUserData();

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
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(),titles,numOfTabs);

        viewPager.setAdapter(viewPageAdapter);

        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(viewPager);
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicatorColor);
            }
        });

        if (selectedTabFragment != 0) viewPager.setCurrentItem(selectedTabFragment);

        if (getSupportFragmentManager().getBackStackEntryCount()>0){
            //Log.w("DrawerActivity", "Fragment BackStack Count: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
        }

        spinnerSelections.putInt(TAG_MY_EVENTS, 0);
        spinnerSelections.putInt(TAG_SEARCH_EVENTS, 0);

        if (savedInstanceState != null){
            openedFragmentType = savedInstanceState.getInt("fragType");
            fragmentTag = savedInstanceState.getString("fragTag");
            openedFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            //Toast.makeText(this, "FragmentType: " + openedFragmentType + " FragmentTag: " + fragmentTag, Toast.LENGTH_SHORT).show();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_refresh:
                if (openedFragment instanceof MyClanFragment){
                    MyClanFragment frag = (MyClanFragment) openedFragment;
                    ArrayList<String> idList = frag.getBungieIdList();
                    if (idList != null){
                        if (mReceiver == null){
                            mReceiver = new RequestResultReceiver(new Handler());
                            mReceiver.setReceiver(this);
                        }
                        if (!isBungieServiceRunning() && NetworkUtils.checkConnection(this)){
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
                    } else Log.w(TAG, "bungieIdList is empty!");
                } else refreshLists();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshLists() {

        if (openedFragment == null || openedFragment instanceof SearchFragment){
            if (refreshDataListenerList != null){
                for (int i=0; i<refreshDataListenerList.size(); i++){
                    if (refreshDataListenerList.get(i).getFragment().isAdded()){
                        refreshDataListenerList.get(i).onRefreshData();
                    } else Log.w(TAG, "Fragment " + refreshDataListenerList.get(i).getFragment().getClass().getName() + " não está atachado ainda!");
                }
            }
        }

        //Toast.makeText(this, R.string.data_refreshed, Toast.LENGTH_SHORT).show();

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
        if (refreshDataListenerList != null) {
            refreshLists();
        } else Log.w(TAG, "refreshDataListenerList é vazio");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //Toast.makeText(this, "Back Count: " + fm.getBackStackEntryCount(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "prior openedFragment: " + fragmentTag, Toast.LENGTH_SHORT).show();
            if (fragmentTag == null){
                finish();
            } else if (openedFragmentType == FRAGMENT_TYPE_WITHOUT_BACKSTACK){
                openMainActivity(null);
                switch (isNewScheduledOrValidated){
                    case GameTable.STATUS_NEW:
                        viewPager.setCurrentItem(0);
                        break;
                    case GameTable.STATUS_SCHEDULED:
                        if (hasScheduledGames){
                            viewPager.setCurrentItem(1);
                        } else viewPager.setCurrentItem(0);
                        break;
                    case GameTable.STATUS_VALIDATED:
                        if (hasValidatedGames){
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

    public boolean openMainActivity(View child){
        if(openedFragment != null){
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
        outState.putString("fragTag", fragmentTag);
        outState.putInt("fragType", openedFragmentType);
    }

    private void prepareFragmentHolder(Fragment fragment, View child, Bundle bundle, String tag){
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
        viewPager.setAdapter(viewPageAdapter);
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
    public void onGameSelected(String id, String tag, String creator, String status) {

        String gameOrigin = tag;
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.clear();

        if (status!= null){
            switch (status){
                case GameTable.STATUS_WAITING:
                case GameTable.STATUS_VALIDATED:
                case GameTable.STATUS_EVALUATED:
                    bundle.putString("gameId",id);
                    bundle.putString("creator", creator);
                    bundle.putString("status", status);
                    bundle.putString("origin", gameOrigin);
                    //Toast.makeText(this, "Backstack count: " + fm.getBackStackEntryCount(), Toast.LENGTH_SHORT).show();
                    isNewScheduledOrValidated = GameTable.STATUS_VALIDATED;
                    fragment = new DetailValidationFragment();
                    break;
                case HistoryListFragment.STATUS_HISTORY:
                    bundle.putString("gameId",id);
                    isNewScheduledOrValidated = GameTable.STATUS_NEW;
                    fragment = new DetailHistoryFragment();
                    break;
                case GameTable.STATUS_NEW:
                    bundle.putString("gameId",id);
                    bundle.putString("origin", gameOrigin);
                    bundle.putString("creator", creator);
                    bundle.putString("status", status);
                    fragment = new DetailEventFragment();
                    isNewScheduledOrValidated = GameTable.STATUS_NEW;
                    break;
                case GameTable.STATUS_SCHEDULED:
                    bundle.putString("gameId",id);
                    bundle.putString("origin", gameOrigin);
                    bundle.putString("creator", creator);
                    bundle.putString("status", status);
                    fragment = new DetailEventFragment();
                    isNewScheduledOrValidated = GameTable.STATUS_SCHEDULED;
                    break;
                default:
                    bundle.putString("gameId",id);
                    bundle.putString("origin", gameOrigin);
                    bundle.putString("creator", creator);
                    bundle.putString("status", status);
                    fragment = new DetailEventFragment();
                    isNewScheduledOrValidated = GameTable.STATUS_NEW;
                    break;
            }

            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
            loadNewFragment(fragment, bundle, "game");
        }

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
    public void registerRefreshListener(Fragment fragment) {
        if (refreshDataListenerList == null){
            refreshDataListenerList = new ArrayList<>();
        }
        refreshDataListenerList.add((RefreshDataListener) fragment);
    }

    @Override
    public void deleteRefreshListener(Fragment fragment) {
        refreshDataListenerList.remove(fragment);
    }

    @Override
    public void registerAlarmTask(Calendar firstNotification, int firstId, Calendar secondNotification, int secondId) {

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (firstId != 0){
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, firstId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, firstNotification.getTimeInMillis(), pIntent);
        }

        if (secondId != 0){
            Intent sIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent psIntent = PendingIntent.getBroadcast(this, secondId, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, secondNotification.getTimeInMillis(), psIntent);
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
        if (userDataListener == null){
            userDataListener = new ArrayList<>();
        }
        userDataListener.add((UserDataListener) fragment);
    }

    @Override
    public void deleteUserDataListener(Fragment fragment) {
        userDataListener.remove(fragment);
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
    public void onEventCreated() {
        openMainActivity(null);
        viewPager.setCurrentItem(1);
    }


    public boolean openNewEventFragment(View child){
        if (openedFragment instanceof NewEventFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        NewEventFragment fragment = new NewEventFragment();
        prepareFragmentHolder(fragment, child, null, "new");
        return true;
    }

    public boolean openSearchEventFragment(View child){
        if (openedFragment instanceof SearchFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        SearchFragment fragment = new SearchFragment();
        prepareFragmentHolder(fragment, child, null, "search");
        return true;
    }

    private boolean openMyEventsFragment(View child) {
        if (openedFragment instanceof MyEventsFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        MyEventsFragment fragment = new MyEventsFragment();
        prepareFragmentHolder(fragment, child, null, "myevents");
        return true;

    }

    public boolean openHistoryFragment(View child){
        if (openedFragment instanceof HistoryListFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        HistoryListFragment fragment = new HistoryListFragment();
        prepareFragmentHolder(fragment, child, null, "history");
        return true;
    }

    public boolean openMyClanFragment(View child){
        if (openedFragment instanceof MyClanFragment){
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

    public boolean openMyProfileFragment(View child){
        if (openedFragment instanceof MyNewProfileFragment){
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

    public boolean openConfigFragment(View child){
        if (openedFragment instanceof MainSettingsFragment){
            drawerLayout.closeDrawers();
            return false;
        }

        MainSettingsFragment fragment = new MainSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "config");
        return true;

    }

    private boolean openAboutFragment(View child) {
        if (openedFragment instanceof AboutSettingsFragment){
            drawerLayout.closeDrawers();
            return false;
        }

        AboutSettingsFragment fragment = new AboutSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "about");
        return true;
    }

    private boolean openDBViewerFragment(View child) {
        if (openedFragment instanceof DBViewerFragment){
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

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
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
                    if (userDataListener != null){
                        for (int i=0; i<userDataListener.size();i++){
                            userDataListener.get(i).onUserDataLoaded();
                        }
                    }
                    break;
            }
            onDataLoaded();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void prepareDrawerMenu(){

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

        final GestureDetector gestureDetector = new GestureDetector(DrawerActivity.this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = rView.findChildViewUnder(e.getX(), e.getY());

                if (child!=null){
                    switch (rView.getChildAdapterPosition(child)){
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

        rView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener(){

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rView.findChildViewUnder(e.getX(), e.getY());

                if (child!=null && gestureDetector.onTouchEvent(e)){
                    switch (rView.getChildAdapterPosition(child)){
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
        bundle.putInt("type",0);
        logOffDialog.setArguments(bundle);
        logOffDialog.show(getSupportFragmentManager(),"Logoff");
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
        editor.putString(PrepareActivity.LEGEND_PREF,getString(R.string.vanguard_data));
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case BungieService.STATUS_RUNNING:
                onLoadingData();
                break;
            case BungieService.STATUS_FINISHED:
                if (openedFragment instanceof MyClanFragment){
                    MyClanFragment frag = (MyClanFragment) openedFragment;
                    frag.refreshData();
                }
                Toast.makeText(this, R.string.clan_updated, Toast.LENGTH_SHORT).show();
                onDataLoaded();
                break;
            case BungieService.STATUS_ERROR:
                Log.w(TAG, "Error on BungieService");
                progress.setVisibility(View.GONE);

                DialogFragment dialog = new MyAlertDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("type",MyAlertDialog.ALERT_DIALOG);
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.unable_update_clan));
                bundle.putString("posButton",getString(R.string.got_it));
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(),"alert");

                break;
        }
    }

    public boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        boolean var = sharedPrefs.getBoolean(BungieService.RUNNING_SERVICE, false);
        //Log.w(TAG, "BungieService running? " +  var);
        return var;
    }
}


