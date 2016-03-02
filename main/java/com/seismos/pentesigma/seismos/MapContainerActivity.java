package com.seismos.pentesigma.seismos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MapContainerActivity extends AppCompatActivity {

    FloatingActionButton fab;
    boolean isFabOpen = false;
    boolean isFabMoved = false;

    private float mLat, mLon;
    private FloatingActionButton fab_share;
    private FloatingActionButton fab_maptype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup Content
        setContentView(R.layout.activity_map_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMaps);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_aroundme:      // about 60km around current position
                setCurrentLocation();
                break;

            case R.id.action_share_1:
                shareMap();
                break;

            case R.id.action_maptype_1:
                setMapType();
                break;

            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    private void setCurrentLocation() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("showmap_sel", "aroundme");
        editor.commit();

        LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                new Intent("aroundme"));

    }

    private void setMapType() {

        final CharSequence[] items = {
                "Map", "Hybrid", "Satellite", "Terrain"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MapContainerActivity.this);
        builder.setTitle("Maptype");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapContainerActivity.this);
                SharedPreferences.Editor editor = prefs.edit();

                switch (item) {
                    case 0:
                        editor.putString("maptype_list", "map");
                        break;
                    case 1:
                        editor.putString("maptype_list", "hybrid");
                        break;
                    case 2:
                        editor.putString("maptype_list", "satellite");
                        break;
                    case 3:
                        editor.putString("maptype_list", "terrain");
                        break;
                    default:
                        editor.putString("maptype_list", "map");
                }

                editor.commit();

                // send broadcast to Fragment to change and refresh the MapType
                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("setMapType"));
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void shareMap() {


        LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                new Intent("takeSnapshot"));
    }
}

