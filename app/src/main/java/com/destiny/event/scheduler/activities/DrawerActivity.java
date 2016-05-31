package com.destiny.event.scheduler.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DrawerAdapter;
import com.destiny.event.scheduler.adapters.ViewPageAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.fragments.DBViewerFragment;
import com.destiny.event.scheduler.fragments.DetailEventFragment;
import com.destiny.event.scheduler.fragments.DetailHistoryFragment;
import com.destiny.event.scheduler.fragments.HistoryListFragment;
import com.destiny.event.scheduler.fragments.MainSettingsFragment;
import com.destiny.event.scheduler.fragments.MyClanFragment;
import com.destiny.event.scheduler.fragments.MyEventsFragment;
import com.destiny.event.scheduler.fragments.MyProfileFragment;
import com.destiny.event.scheduler.fragments.NewEventFragment;
import com.destiny.event.scheduler.fragments.NewEventsListFragment;
import com.destiny.event.scheduler.fragments.ScheduledListFragment;
import com.destiny.event.scheduler.fragments.SearchFragment;
import com.destiny.event.scheduler.fragments.ValidateFragment;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.AlarmReceiver;
import com.destiny.event.scheduler.views.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class DrawerActivity extends AppCompatActivity implements ToActivityListener, LoaderManager.LoaderCallbacks<Cursor>, OnEventCreatedListener, FromDialogListener{

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
    public static final String COURT_PREF = "1";
    public static final String CRUCIBLE_PREF = "2";
    public static final String PATROL_PREF = "3";
    public static final String PRISON_PREF = "4";
    public static final String RAID_PREF = "5";
    public static final String STORY_PREF = "6";
    public static final String STRIKE_PREF = "7";
    public static final String STRIKE_LIST_PREF = "8";

    private Toolbar toolbar;

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
    private Fragment openFragment;
    private String fragmentTag;
    private boolean configFragOpened = false;
    //private ArrayList<String> backStackList;

    private String clanName;
    private String clanId;
    private String clanIcon;
    private String clanBanner;
    private String clanDesc;

    private String bungieId;
    private String userName;

    private String gameOrigin;

    private String clanOrderBy;

    ViewPager viewPager;
    ViewPageAdapter viewPageAdapter;
    SlidingTabLayout tabLayout;
    private int selectedTabFragment;

    private Bundle spinnerSelections = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_layout);

        progress = (ProgressBar) findViewById(R.id.progress_bar);

        getClanData();
        getLoggedUserData();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.home_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
            Log.w("DrawerActivity", "Fragment BackStack Count: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
        }

        refreshDataListenerList = new ArrayList<>();
        userDataListener = new ArrayList<>();

        Bundle notifyBundle = getIntent().getExtras();
        if (notifyBundle != null && notifyBundle.containsKey("notification")){
            refreshLists();
        }

        spinnerSelections.putInt(TAG_MY_EVENTS, 0);
        spinnerSelections.putInt(TAG_SEARCH_EVENTS, 0);

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
                refreshLists();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshLists() {

        if (openFragment == null){
            for (int i=0; i<refreshDataListenerList.size(); i++){
                if (refreshDataListenerList.get(i).getFragment().isAdded()){
                    refreshDataListenerList.get(i).onRefreshData();
                } else Log.w(TAG, "Fragment " + refreshDataListenerList.get(i).getFragment().getClass().getName() + " não está atachado ainda!");
            }
        }

        Toast.makeText(this, R.string.data_refreshed, Toast.LENGTH_SHORT).show();

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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //Toast.makeText(this, "Back Count: " + fm.getBackStackEntryCount(), Toast.LENGTH_SHORT).show();
            if (fm.getBackStackEntryCount() == 1) {
                super.onBackPressed();
                openMainActivity(null);
            } else {
                super.onBackPressed();
                openFragment = fm.findFragmentById(R.id.content_frame);
                if (openFragment != null) fragmentTag = openFragment.getTag();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fragment", fragmentTag);
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
        openFragment = null;
        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setViewPager(viewPager);
        viewPager.setCurrentItem(1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.home_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void loadNewFragment(Fragment fragment, Bundle bundle, String tag) {

        fragment.setArguments(bundle);
        ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, fragment, tag);
        ft.addToBackStack(null);
        ft.commit();
        fragmentTag = tag;
        openFragment = fragment;
        invalidateOptionsMenu();
    }

    @Override
    public void loadWithoutBackStack(Fragment fragment, Bundle bundle, String tag) {

        fragment.setArguments(bundle);
        ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, fragment, tag);
        ft.commit();
        fragmentTag = tag;
        openFragment = fragment;
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
    public String getClanName() {
        return clanName;
    }

    @Override
    public String getOrderBy() {
        return clanOrderBy;
    }

    @Override
    public void closeFragment() {
        ft = fm.beginTransaction();
        ft.remove(openFragment);
        ft.commit();
        fm.popBackStack();
        fragmentTag = null;
        if (fm.getBackStackEntryCount() == 1){
            updateViewPager();
        } else {
            openFragment = fm.findFragmentById(R.id.content_frame);
            fragmentTag = openFragment.getTag();
        }
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

        gameOrigin = tag;
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
                    fragment = new ValidateFragment();
                    break;
                case HistoryListFragment.STATUS_HISTORY:
                    bundle.putString("gameId",id);
                    fragment = new DetailHistoryFragment();
                    break;
                default:
                    bundle.putString("gameId",id);
                    bundle.putString("origin",gameOrigin);
                    bundle.putString("creator", creator);
                    bundle.putString("status", status);
                    fragment = new DetailEventFragment();
                    break;
            }

            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
            loadNewFragment(fragment, bundle, id);
        }

    }

    @Override
    public void onNoScheduledGames() {
        onDataLoaded();
        if (selectedTabFragment == 1){
            viewPager.setCurrentItem(0);
        } else viewPager.setCurrentItem(selectedTabFragment);
    }

    @Override
    public void setClanOrderBy(String orderBy) {
        clanOrderBy = orderBy;
    }

    @Override
    public void registerRefreshListener(Fragment fragment) {
        refreshDataListenerList.add((RefreshDataListener) fragment);
    }

    @Override
    public void deleteRefreshListener(Fragment fragment) {
        refreshDataListenerList.remove(fragment);
    }

    @Override
    public void registerAlarmTask(Calendar time, int requestId) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pIntent);
        Log.w(TAG, "Notification scheduled to: " + time.getTime());
        Log.w(TAG, "requestId:" + requestId);
    }

    @Override
    public void registerUserDataListener(Fragment fragment) {
        userDataListener.add((UserDataListener) fragment);
    }

    @Override
    public void deleteUserDataListener(Fragment fragment) {
        userDataListener.remove(fragment);
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
    public void onEventCreated() {
        closeFragment();
    }


    public boolean openNewEventFragment(View child){
        if (openFragment instanceof NewEventFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        NewEventFragment fragment = new NewEventFragment();
        prepareFragmentHolder(fragment, child, null, "new");
        return true;
    }

    public boolean openSearchEventFragment(View child){
        if (openFragment instanceof SearchFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        SearchFragment fragment = new SearchFragment();
        prepareFragmentHolder(fragment, child, null, "search");
        return true;
    }

    private boolean openMyEventsFragment(View child) {
        if (openFragment instanceof MyEventsFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        MyEventsFragment fragment = new MyEventsFragment();
        prepareFragmentHolder(fragment, child, null, "myevents");
        return true;

    }

    public boolean openHistoryFragment(View child){
        if (openFragment instanceof HistoryListFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        HistoryListFragment fragment = new HistoryListFragment();
        prepareFragmentHolder(fragment, child, null, "history");
        return true;
    }

    public boolean openMyClanFragment(View child){
        if (openFragment instanceof MyClanFragment){
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
        if (openFragment instanceof MyProfileFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        MyProfileFragment fragment = new MyProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putString("clanName", clanName);
        bundle.putInt("type", MyProfileFragment.TYPE_USER);

        prepareFragmentHolder(fragment, child, bundle, "profile");
        return true;
    }

    public boolean openConfigFragment(View child){
       if (openFragment instanceof MainSettingsFragment){
            drawerLayout.closeDrawers();
            return false;
        }

        MainSettingsFragment fragment = new MainSettingsFragment();
        prepareFragmentHolder(fragment, child, null, "config");
        return true;

    }

    private boolean openAboutFragment(View child) {
        if (openFragment instanceof DBViewerFragment){
            drawerLayout.closeDrawers();
            return false;
        }

        DBViewerFragment fragment = new DBViewerFragment();
        prepareFragmentHolder(fragment, child, null, "about");
        return true;
    }

    public boolean openMainActivity(View child){
        if(openFragment != null){
            ft = fm.beginTransaction();
            ft.remove(openFragment);
            ft.commit();
            fragmentTag = null;
            updateViewPager();
            drawerLayout.closeDrawers();
            if (child != null) child.playSoundEffect(SoundEffectConstants.CLICK);


            if (gameOrigin != null){
                switch (gameOrigin){
                    case NewEventsListFragment.TAG:
                        viewPager.setCurrentItem(0);
                        break;
                    case ScheduledListFragment.TAG:
                        viewPager.setCurrentItem(1);
                        break;
                    default:
                        viewPager.setCurrentItem(1);
                        break;
                }
            }



            return true;
        }
        drawerLayout.closeDrawers();
        return false;
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
                projection = new String[]{LoggedUserTable.COLUMN_ID, LoggedUserTable.COLUMN_MEMBERSHIP, LoggedUserTable.COLUMN_NAME};
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

        if (data.moveToFirst() && data != null){
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
                    }
                    for (int i=0; i<userDataListener.size();i++){
                        userDataListener.get(i).onUserDataLoaded();
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

        rView = (RecyclerView) findViewById(R.id.drawer_view);
        rAdapter = new DrawerAdapter(getApplicationContext(), header, sections, icons, items, clanIcon, clanName, clanDesc, clanBanner);
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
                            openMainActivity(child);
                            break;
                        case 1:
                            openNewEventFragment(child);
                            break;
                        case 2:
                            openSearchEventFragment(child);
                            break;
                        case 3:
                            openMyEventsFragment(child);
                            break;
                        case 4:
                            openHistoryFragment(child);
                            break;
                        case 5:
                            break;
                        case 6:
                            openMyClanFragment(child);
                            break;
                        case 7:
                            openMyProfileFragment(child);
                            break;
                        case 8:
                            break;
                        case 9:
                            openConfigFragment(child);
                            break;
                        case 10:
                            break;
                        case 11:
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
                            return openMainActivity(child);
                        case 1:
                            return openNewEventFragment(child);
                        case 2:
                            return openSearchEventFragment(child);
                        case 3:
                            return openMyEventsFragment(child);
                        case 4:
                            return openHistoryFragment(child);
                        case 5:
                            return false;
                        case 6:
                            return openMyClanFragment(child);
                        case 7:
                            return openMyProfileFragment(child);
                        case 8:
                            return false;
                        case 9:
                            return openConfigFragment(child);
                        case 10:
                            return openAboutFragment(child);
                        case 11:
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

}


