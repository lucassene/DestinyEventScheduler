package com.destiny.event.scheduler.activities;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DrawerAdapter;
import com.destiny.event.scheduler.adapters.ViewPageAdapter;
import com.destiny.event.scheduler.fragments.HistoryFragment;
import com.destiny.event.scheduler.fragments.MyClanFragment;
import com.destiny.event.scheduler.fragments.MyProfileFragment;
import com.destiny.event.scheduler.fragments.NewEventFragment;
import com.destiny.event.scheduler.fragments.SearchFragment;
import com.destiny.event.scheduler.fragments.ValidateFragment;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.views.SlidingTabLayout;

import java.util.ArrayList;

public class DrawerActivity extends AppCompatActivity implements ToActivityListener, FragmentManager.OnBackStackChangedListener {

    private Toolbar toolbar;
    private FloatingActionButton newEventButton;

    RecyclerView rView;
    RecyclerView.Adapter rAdapter;
    RecyclerView.LayoutManager rLayoutManager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    private FromActivityListener newEventListener;

    private FragmentTransaction ft;
    private FragmentManager fm;
    private Fragment openFragment;
    private String fragmentTag;
    private ArrayList<String> backStackList;

    ViewPager viewPager;
    ViewPageAdapter viewPageAdapter;
    SlidingTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.home_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        newEventButton = (FloatingActionButton) findViewById(R.id.floating_button);

        String[] items = getResources().getStringArray(R.array.menu_item);
        TypedArray icons = getResources().obtainTypedArray(R.array.menu_icons);

        String header = getResources().getString(R.string.def_clan_header);
        Drawable clanImg = ContextCompat.getDrawable(this, R.drawable.default_clan_banner);
        String clanDesc = getResources().getString(R.string.def_clan_desc);

        String[] sections = getResources().getStringArray(R.array.menu_section);
        String titles[] = getResources().getStringArray(R.array.tab_titles);
        int numOfTabs = titles.length;

        rView = (RecyclerView) findViewById(R.id.drawer_view);
        rAdapter = new DrawerAdapter(header, sections, icons, items);
        rView.setAdapter(rAdapter);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);

        fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(this);

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
                            return openHistoryFragment(child);
                        case 5:
                           return false;
                        case 6:
                            return openMyClanFragment(child);
                        case 7:
                            return openMyProfileFragment(child);
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
            newEventButton.setVisibility(View.GONE);
            tabLayout.setViewPager(null);
            viewPager.setAdapter(null);
        }

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
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fragment", fragmentTag);
    }

    private void createFragmentView(Fragment fragment, View child, String tag){
        drawerLayout.closeDrawers();
        tabLayout.setViewPager(null);
        viewPager.setAdapter(null);
        child.playSoundEffect(SoundEffectConstants.CLICK);
        newEventButton.setVisibility(View.GONE);
        loadNewFragment(fragment, null, tag);
    }

    @Override
    public void updateViewPager() {
        if (getFragmentManager().getBackStackEntryCount() == 0){
            openFragment = null;
            viewPager.setAdapter(viewPageAdapter);
            tabLayout.setViewPager(viewPager);
            viewPager.setCurrentItem(1);
            newEventButton.setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.home_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
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
        getSupportFragmentManager().popBackStack();
        newEventListener = (FromActivityListener) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        newEventListener.onEventTypeSent(id);
    }

    @Override
    public void onEventGameSelected(String id) {
        getSupportFragmentManager().popBackStack();
        newEventListener = (NewEventFragment) getSupportFragmentManager().findFragmentByTag("new");
        newEventListener.onEventGameSent(id);
    }


    public boolean openNewEventFragment(View child){
        if (openFragment instanceof NewEventFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        NewEventFragment fragment = new NewEventFragment();
        createFragmentView(fragment, child, "new");
        return true;
    }

    public boolean openValidateEventFragment(View child){
        if (openFragment instanceof ValidateFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        ValidateFragment fragment = new ValidateFragment();
        createFragmentView(fragment, child, "validate");
        return true;
    }

    public boolean openSearchEventFragment(View child){
        if (openFragment instanceof SearchFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        SearchFragment fragment = new SearchFragment();
        createFragmentView(fragment, child,"search");
        return true;
    }

    public boolean openHistoryFragment(View child){
        if (openFragment instanceof HistoryFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        HistoryFragment fragment = new HistoryFragment();
        createFragmentView(fragment, child,"history");
        return true;
    }

    public boolean openMyClanFragment(View child){
        if (openFragment instanceof MyClanFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        MyClanFragment fragment = new MyClanFragment();
        createFragmentView(fragment, child,"clan");
        return true;
    }

    public boolean openMyProfileFragment(View child){
        if (openFragment instanceof MyProfileFragment){
            drawerLayout.closeDrawers();
            return false;
        }
        MyProfileFragment fragment = new MyProfileFragment();
        createFragmentView(fragment, child,"profile");
        return true;
    }

    public boolean openMainActivity(View child){
        if(openFragment !=null){
            ft = fm.beginTransaction();
            ft.remove(openFragment);
            ft.commit();
            fragmentTag = null;
            updateViewPager();
            drawerLayout.closeDrawers();
            child.playSoundEffect(SoundEffectConstants.CLICK);
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
    public void onBackStackChanged() {
        //if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //updateViewPager();
        //}
    }
}


