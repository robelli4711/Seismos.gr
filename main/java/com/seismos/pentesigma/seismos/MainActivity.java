package com.seismos.pentesigma.seismos;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    String TAG = "Seismos .GR - ";
    private final int SHOW_STRONGEST = 1;
    private final int SHOW_LATEST = 2;
    private final int SHOW_SMALLEST = 3;
    private int REFRESH_TYPE = SHOW_LATEST;

    RecyclerView recyclerView;
    private Recycler_View_Adapter adapter;

    private SwipeRefreshLayout mSwipeRefresh;
    private View mListItem;
    private RecyclerView eventList;
    private SeismosService service = null;

    private LocationManager mLocationManager;
    private double mLat;
    private double mLon;

    private AdView mAdView;
    private Tracker mTracker;

    private ProgressBar mprogressBar_1;
    private View mWaiting;
    private ObjectAnimator animation;
    private Animation mAnim;


    public MainActivity() {
/*        super();
        Intent intent = new Intent(this, SeismosService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE); */
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            SeismosService.MyBinder b = (SeismosService.MyBinder) binder;
            service = b.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init Stetho Inspection for Database -> remove before for publish
//        Stetho.initializeWithDefaults(this);

        // Setup Broadcast Receiver
        initBroadcastListeners();

        // set content
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // setup waiting spinner, stop after list is filled up
        mprogressBar_1 = (ProgressBar)findViewById(R.id.progressBar);
        mWaiting = (View)findViewById(R.id.waiting_1);
        startWaiting();

        // setup shared preferences if needed (first time setup)
        initSharedPreferences();

        // setup Current Position
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();

            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            try {
                mLat = location.getLatitude();      // quick get last known pos
                mLon = location.getLongitude();     // quick get last known pos
            } catch (NullPointerException e) {
                mLat = 0;
                mLon = 0;
            }

            editor.putString("myLocation_Lat", String.valueOf(mLat));
            editor.putString("myLocation_Lon", String.valueOf(mLon));
            editor.commit();

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);  // setup Listner for new Coordinates
        }

        // start System Service
        Log.i("Seismos", "start Service");

//            Intent intent = new Intent(this, SeismosService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // Refresh Event List with Swiper
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_eventlist);
        eventList = (RecyclerView) findViewById(R.id.recyclerview);
        try {
            refreshList();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        // Listener
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                try {
                    refreshList();
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        });

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {

                animation.cancel();
                startWaiting();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        if (new pull_Events(getApplicationContext()).isNetworkavailable()) {
/*
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
*/
        }

        // setup and start Google Analytics
        SeismosApplication application = (SeismosApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Share")
                .build());

        mTracker.setScreenName("Home");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        switch (id) {
            case R.id.action_settings:
                return true;

            case R.id.action_show_map:
                editor.putBoolean("showmap", true);
                editor.commit();

                // Show Map in a own Activity
                Intent intent = new Intent(this, MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                break;

            case R.id.action_sort:
                REFRESH_TYPE = SHOW_STRONGEST;
                mSwipeRefresh.setRefreshing(true);
                try {
                    Toast.makeText(MainActivity.this, "show strongest first", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("showmap", false);
                    editor.commit();
                    refreshList();
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.action_sort_date:
                REFRESH_TYPE = SHOW_LATEST;
                mSwipeRefresh.setRefreshing(true);
                try {
                    Toast.makeText(MainActivity.this, "show recent first", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("showmap", false);
                    editor.commit();
                    refreshList();
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Getting the Menu selection of the drawer
     *
     * @param item whih Item is selected
     * @return true/false
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        String str = (String) item.getTitle();

        if (str.equals("Settings")) {
            intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivityForResult(intent, 0);
        }

        if (str.equals("Numbers")) {
            intent = new Intent(this, StatisticsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivityForResult(intent, 1);
//            startActivity(intent);        }
        }

        if (str.equals("Charts")) {
            intent = new Intent(this, ChartsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivityForResult(intent, 2);
//            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (mConnection != null) {
//            unbindService(mConnection);
//        }
    }

    /**
     * Refresh the Eventlist dependig on the latest Sort
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void refreshList() throws IOException, XmlPullParserException {

        if (new pull_Events(this).isNetworkavailable()) {
            eventList.setAdapter(null);     // clear the list content

            DataSource_Events dataSource_events = new DataSource_Events(getApplicationContext());
            dataSource_events.deleteAllEvents();
            new pull_Events(this).execute("");
        } else {
            Toast.makeText(MainActivity.this, "NO NETWORK, last datas shown", Toast.LENGTH_LONG).show();
            setListAdapter();
            mSwipeRefresh.setRefreshing(false);
        }
    }

    public void setListAdapter() {

        DataSource_Events dataSource_events = new DataSource_Events(getApplicationContext());

        List<Data_Events> values;
        switch (REFRESH_TYPE) {
            case SHOW_LATEST:
                values = dataSource_events.getAllEvents();
                break;
            case SHOW_STRONGEST:
                values = dataSource_events.getAllEventsSortMagD();
                break;
            default:
                values = dataSource_events.getAllEvents();
        }

        List<Data_Events> data = values;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        adapter = new Recycler_View_Adapter(getApplicationContext(), REFRESH_TYPE, data, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefresh.setRefreshing(false);
        stopWaiting();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (new pull_Events(getApplicationContext()).isNetworkavailable()) {
/*
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
*/
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Share")
                .build());
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (mConnection != null) {
//            unbindService(mConnection);
//        }
    }

    private void initSharedPreferences() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // don't overwrite the preferences
        if (prefs.contains("maptype_list"))
            return;

        // setup initial values
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("maptype_list", "satellite");
        editor.putString("sync_frequency", "3600");
        editor.putString("zoom_list", "10");
        editor.commit();
    }

    private void initBroadcastListeners() {

        BroadcastReceiver localBroadcastReceiver = new LocalBroadcastReceiver();
        IntentFilter filterSmall = new IntentFilter("getAllSmallMagnitudes");
        IntentFilter filterMedium = new IntentFilter("getAllMediumMagnitudes");
        IntentFilter filterLarge = new IntentFilter("getAllLargeMagnitudes");
        IntentFilter filterAllTodays = new IntentFilter("getAllTodaysEvents");
        IntentFilter filterListAdapter = new IntentFilter("setListAdapter");

        IntentFilter filterAll_48 = new IntentFilter("getAllMagnitudes_48");
        IntentFilter filterSmall_48 = new IntentFilter("getAllSmallMagnitudes_48");
        IntentFilter filterMedium_48 = new IntentFilter("getAllMediumMagnitudes_48");
        IntentFilter filterLarge_48 = new IntentFilter("getAllLargeMagnitudes_48");

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterSmall);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterMedium);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterLarge);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterAllTodays);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterListAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterAll_48);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterSmall_48);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterMedium_48);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterLarge_48);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();

            mLat = location.getLatitude();
            mLon = location.getLongitude();

            editor.putString("myLocation_Lat", String.valueOf(mLat));
            editor.putString("myLocation_Lon", String.valueOf(mLon));
            editor.commit();

            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void startWaiting() {

 //       mWaiting.setVisibility(View.VISIBLE);
        animation = ObjectAnimator.ofInt (mprogressBar_1, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration (5000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
    }

    private void stopWaiting() {

        mWaiting.setVisibility(View.GONE);
    }


    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // safety check
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals("setListAdapter")) {
                setListAdapter();
            }

            if (intent.getAction().equals("getAllTodaysEvents")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "todays");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllSmallMagnitudes")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "small");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllMediumMagnitudes")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "medium");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllLargeMagnitudes")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "large");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllMagnitudes_48")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "all_48");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllSmallMagnitudes_48")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "small_48");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllMediumMagnitudes_48")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "medium_48");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }

            if (intent.getAction().equals("getAllLargeMagnitudes_48")) {

                // set Status Flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("showmap_sel", "large_48");
                editor.putBoolean("showmap", true);
                editor.commit();

                // start map activity
                intent = new Intent(getApplicationContext(), MapContainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                return;
            }
        }
    }

}


