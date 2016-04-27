package com.destiny.event.scheduler.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DrawerAdapter;
import com.destiny.event.scheduler.adapters.ViewPageAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.fragments.DetailEventFragment;
import com.destiny.event.scheduler.fragments.HistoryFragment;
import com.destiny.event.scheduler.fragments.MyClanFragment;
import com.destiny.event.scheduler.fragments.MyProfileFragment;
import com.destiny.event.scheduler.fragments.NewEventFragment;
import com.destiny.event.scheduler.fragments.SearchFragment;
import com.destiny.event.scheduler.fragments.ValidateFragment;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.views.SlidingTabLayout;

public class DrawerActivity extends AppCompatActivity implements ToActivityListener, LoaderManager.LoaderCallbacks<Cursor>, OnEventCreatedListener, FromDialogListener{

    private static final String TAG = "DrawerActivity";

    private static final int URL_LOADER_CLAN = 40;

    private static final int TYPE_USER = 1;
    private static final int TYPE_MEMBER = 2;

    private Toolbar toolbar;

    RecyclerView rView;
    RecyclerView.Adapter rAdapter;
    RecyclerView.LayoutManager rLayoutManager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    ProgressBar progress;

    private FromActivityListener newEventListener;
    //private OnEventCreatedListener createdEventListener;

    private FragmentTransaction ft;
    private FragmentManager fm;
    private Fragment openFragment;
    private String fragmentTag;
    //private ArrayList<String> backStackList;

    private String clanName;
    private String clanId;
    private String clanIcon;
    private String clanBanner;
    private String clanDesc;

    private String bungieId;
    private String userName;

    ViewPager viewPager;
    ViewPageAdapter viewPageAdapter;
    SlidingTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_layout);

        progress = (ProgressBar) findViewById(R.id.progress_bar);

        if (savedInstanceState == null){
            //Toast.makeText(this, "Verificando usuário logado...", Toast.LENGTH_SHORT).show();
            getClanData();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.home_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bungieId = getIntent().getStringExtra("bungieId");
        userName = getIntent().getStringExtra("userName");


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
        viewPager.setCurrentItem(1);

        if (getSupportFragmentManager().getBackStackEntryCount()>0){
            Log.w("DrawerActivity", "Fragment BackStack Count: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
        }

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
        }
        return super.onOptionsItemSelected(item);
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
                fragmentTag = openFragment.getTag();
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
    public void onGameSelected(String id) {

        Bundle bundle = new Bundle();
        bundle.putString("gameId",id);
        tabLayout.setViewPager(null);
        viewPager.setAdapter(null);
        loadNewFragment(new DetailEventFragment(), bundle, id);

    }

    @Override
    public void onNoScheduledGames() {
        onDataLoaded();
        viewPager.setCurrentItem(0);
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

    public boolean openValidateEventFragment(View child){
        if (openFragment instanceof ValidateFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        ValidateFragment fragment = new ValidateFragment();
        prepareFragmentHolder(fragment, child, null, "validate");
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

    public boolean openHistoryFragment(View child){
        if (openFragment instanceof HistoryFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        HistoryFragment fragment = new HistoryFragment();
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
        bundle.putInt("type", TYPE_USER);

        prepareFragmentHolder(fragment, child, bundle, "profile");
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
            return true;
        }
        drawerLayout.closeDrawers();
        return false;
    }

    public void showNewEvent(View view){
        view.setVisibility(View.GONE);
        openNewEventFragment(view);
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
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.w(TAG, "Clan Cursor: " + DatabaseUtils.dumpCursorToString(data));

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
                            openValidateEventFragment(child);
                            break;
                        case 4:
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
                            return openValidateEventFragment(child);
                        case 4:
                            return false;
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
                            return false;
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
        logOffDialog.show(getSupportFragmentManager(),"Logoff");
        child.playSoundEffect(SoundEffectConstants.CLICK);
        return true;

    }

    @Override
    public void onPositiveClick(String input, int type) {

    }

    @Override
    public void onDateSent(String date) {

    }

    @Override
    public void onTimeSent(String time) {

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
}


