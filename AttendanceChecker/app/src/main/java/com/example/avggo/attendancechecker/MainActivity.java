package com.example.avggo.attendancechecker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.example.avggo.attendancechecker.adapter.ViewPagerAdapter;
import com.example.avggo.attendancechecker.ui.HelpActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private SlidingTabLayout tabSlider;
    private CharSequence tabList[] = {"current", "done", "submitted"};
    private ArrayList<String> buildings = new ArrayList<String>();
    ArrayList<String> curBuildings = new ArrayList<String>();
    ArrayList<Integer> buldingIDs = new ArrayList<Integer>();
    public static final int TAB_NUMBERS = 3;
    public static final int DONE_TAB = 1;

    //navigation items
    String TITLES[] = {"Help", "Logout"};
    int ICONS[] = {R.drawable.ic_help_black_24dp, R.drawable.ic_input_black_24dp};
    //header
    String NAME;
    String EMAIL;
    int PROFILE = R.drawable.dummy_pic;

    NavigationView mNavigationView;
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    Button submitButton;


    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    private String RID;
    private DatabaseOpenHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RID = getIntent().getStringExtra("RID");
        populateAllBuildings();
        //Toast.makeText(getApplicationContext(), RID, Toast.LENGTH_LONG).show();

        //SET NAVIGATION TEXT
        NAME = getIntent().getStringExtra("DISPLAY_NAME");
        EMAIL = getIntent().getStringExtra("EMAIL");
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Attendance");
        setSupportActionBar(toolbar);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabList, TAB_NUMBERS, RID);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new AccordionTransformer());
        viewPager.setCurrentItem(0);
        submitButton = (Button) findViewById(R.id.submitSheetButton);
        submitButton.setVisibility(View.GONE);
        submitButton.setEnabled(false);
        tabSlider = (SlidingTabLayout) findViewById(R.id.tabs);
        tabSlider.setDistributeEvenly(true);
        tabSlider.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getApplicationContext(), R.color.tabsScrollColor);
            }
        });
        tabSlider.setViewPager(viewPager);
        db = new DatabaseOpenHelper(getBaseContext());

        curBuildings = db.getAssignedBuildings(RID);
        //initialize drawer
        initializeDrawer();

        tabSlider.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                if(position == DONE_TAB) {
                    submitButton.setVisibility(View.VISIBLE);
                    if (db.getAssignedAttendance(RID, "NULL").size() == 0)
                        submitButton.setEnabled(true);
                    else
                        submitButton.setEnabled(false);
                }else
                    submitButton.setVisibility(View.GONE);
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

        scheduleTask(getBaseContext());
    }

    private void populateAllBuildings(){
        buildings.add("All Buildings");
        buildings.add("La Salle Hall");
        buildings.add("Yuchengco");
        buildings.add("Saint Joseph");
        buildings.add("Velasco");
        buildings.add("Miguel");
        buildings.add("Gokongwei");
        buildings.add("Andrew");
        buildings.add("Razon");

        buldingIDs.add(R.id.nav_allbuildings);
        buldingIDs.add(R.id.nav_lasallehall);
        buldingIDs.add(R.id.nav_yuchengco);
        buldingIDs.add(R.id.nav_saintjoseph);
        buldingIDs.add(R.id.nav_velasco);
        buldingIDs.add(R.id.nav_miguel);
        buldingIDs.add(R.id.nav_gokongwei);
        buldingIDs.add(R.id.nav_andrew);
        buldingIDs.add(R.id.nav_razon);
    }

    private void filterByBuilding(String building) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabList, TAB_NUMBERS, RID, building);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new AccordionTransformer());
        viewPager.setCurrentItem(0);
        tabSlider = (SlidingTabLayout) findViewById(R.id.tabs);
        tabSlider.setDistributeEvenly(true);
        tabSlider.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getApplicationContext(), R.color.tabsScrollColor);
            }
        });
        tabSlider.setViewPager(viewPager);
    }

    public void initializeDrawer() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        for(int i = 1; i < buildings.size(); i++){
            mNavigationView.getMenu().getItem(i).setVisible(false);
        }

        for(int i = 0; i < curBuildings.size(); i++){
            mNavigationView.getMenu().getItem(buildings.indexOf(curBuildings.get(i))).setVisible(true);
            if(i == 0)
                setMenuCounter(buldingIDs.get(buildings.indexOf(curBuildings.get(i))), db.getAssignedAttendance(RID, "NULL").size());
            else
                setMenuCounter(buldingIDs.get(buildings.indexOf(curBuildings.get(i))), db.getAssignedAttendance(RID, curBuildings.get(i)).size());
        }

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);

                switch(menuItem.getTitle().toString()) {
                    case "Log Out":
                        goToLogin();
                        break;
                    case "All Buildings":
                        filterByBuilding("NULL");
                        break;
                    case "Gokongwei":
                        filterByBuilding("Gokongwei");
                        break;
                    case "Andrew":
                        filterByBuilding("Andrew");
                        break;
                    case "Help":
                        Intent help = new Intent();
                        help.setClass(getBaseContext(), HelpActivity.class);
                        startActivity(help);
                        break;
                }

                ((DrawerLayout) findViewById(R.id.DrawerLayout)).closeDrawers();
                return true;
            }
        });

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
                TextView name = (TextView) findViewById(R.id.name);
                name.setText(NAME);
                TextView email = (TextView) findViewById(R.id.email);
                email.setText(EMAIL);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //TODO Code here will execute once drawer is closed
            }
        };

        Drawer.addDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State
    }

    private void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) mNavigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }

    public void goToLogin() {
        Intent loginscreen = new Intent(this, LoginActivity.class);
        loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginscreen);
        this.finish();
    }

    private void scheduleTask(Context c){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 11);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        //Now create the time and schedule it
        Timer timer = new Timer();

        //Use this if you want to execute it once
        timer.schedule(new AttendanceScheduler(c), today.getTime());
    }

    private static class AttendanceScheduler extends TimerTask
    {
        private Context c;

        public AttendanceScheduler(Context c){
            this.c = c;
        }

        @Override
        public void run() {
            Log.e("WORRRRRRRK", "Started Repeat Timer");
        }
    }
}
