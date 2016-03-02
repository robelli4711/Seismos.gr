package com.seismos.pentesigma.seismos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapContainerActivityFragment
        extends Fragment
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    public MapContainerActivityFragment() {
        BroadcastReceiver localBroadcastReceiver = new LocalBroadcastReceiver();

        IntentFilter filterSetMapType = new IntentFilter("setMapType");
        IntentFilter filtertakeSnapshot = new IntentFilter("takeSnapshot");
        IntentFilter filteraroundMe = new IntentFilter("aroundme");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localBroadcastReceiver, filterSetMapType);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localBroadcastReceiver, filtertakeSnapshot);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localBroadcastReceiver, filteraroundMe);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_map_container, container, false);

        // setup Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapinfrag);
        mapFragment.getMapAsync(this);

        return myView;
    }

    public void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapinfrag);

        if (mapFragment != null) {      // prevent a exception -> to be investigated
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (prefs.getBoolean("showmap", false)) {

            switch (prefs.getString("showmap_sel", "all")) {
                case "todays":
                    try {
                        showAllEventsOnMap(googleMap, "todays");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "small":
                    try {
                        showAllEventsOnMap(googleMap, "small");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "medium":
                    try {
                        showAllEventsOnMap(googleMap, "medium");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "large":
                    try {
                        showAllEventsOnMap(googleMap, "large");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "all_48":
                    try {
                        showAllEventsOnMap(googleMap, "all_48");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "small_48":
                    try {
                        showAllEventsOnMap(googleMap, "small_48");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "medium_48":
                    try {
                        showAllEventsOnMap(googleMap, "medium_48");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "large_48":
                    try {
                        showAllEventsOnMap(googleMap, "large_48");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "aroundme":
                    try {
                        showAllEventsOnMap(googleMap, "aroundme");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    try {
                        showAllEventsOnMap(googleMap, "all");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
            }
        } else {
            showEventOnMap(googleMap);
        }
    }

    private void showAllEventsOnMap(GoogleMap googleMap, String type) throws ParseException {

        mMap = googleMap;
        boolean bfirst = true;
        List<Data_Events> values;
        DataSource_Events dataSource_events = new DataSource_Events(getContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        // initial move the camera
        LatLng location = new LatLng(37.5, 23.44);

        // Setup  the Map
        switch (prefs.getString("maptype_list", "map")) {
            case "map":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "hybrid":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        // get the position from SharedPreferences
        switch (type) {
            case "todays":
                values = dataSource_events.getAllTodaysEvents();
                getActivity().setTitle(getString(R.string.string_todays_events));
                break;
            case "small":
                values = dataSource_events.getEventsRange(0, 2.9);
                getActivity().setTitle(getString(R.string.string_todays_events_l3));
                break;
            case "medium":
                values = dataSource_events.getEventsRange(3, 4.9);
                getActivity().setTitle(getString(R.string.string_todays_events_l5));
                break;
            case "large":
                values = dataSource_events.getEventsRange(5, 10);
                getActivity().setTitle(getString(R.string.string_todays_events_b5));
                break;
            case "all_48":
                values = dataSource_events.getAllEvents();
                getActivity().setTitle(getString(R.string.string_all_events));
                break;
            case "small_48":
                values = dataSource_events.getEventsRange48(0, 2.9);
                getActivity().setTitle(getString(R.string.string_48_events_l3));
                break;
            case "medium_48":
                values = dataSource_events.getEventsRange48(3, 4.9);
                getActivity().setTitle(getString(R.string.string_48_events_l5));
                break;
            case "large_48":
                values = dataSource_events.getEventsRange48(5, 10);
                getActivity().setTitle(getString(R.string.string_48_events_b5));
                break;
            default:
                values = dataSource_events.getAllEvents();
                getActivity().setTitle(getString(R.string.string_all_events));
        }
        editor.putString("showmap_sel", "all");
        editor.commit();

        int i = 0;
        while (i < values.size()) {

            if (values.get(i).getLatitude() == 0 || values.get(i).getLongitude() == 0) {
                i++;
                continue;
            }

            String title = prefs.getString("title", "Event");
            mMap.setContentDescription(title);

            // Add a marker and move the camera
            location = new LatLng(values.get(i).getLatitude(), values.get(i).getLongitude());

            mMap.addMarker(new MarkerOptions().position(location)
                    .title(values.get(i).getTitle())
                    .snippet("Depth: " + values.get(i).getDepth())
                    .icon(BitmapDescriptorFactory.defaultMarker((float) setMarkerColor(values.get(i).getMagnitude()))));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(4));

            i++;
        }

        float mlat = Float.parseFloat(prefs.getString("myLocation_Lat", "37.5"));
        float mlon = Float.parseFloat(prefs.getString("myLocation_Lon", "23.44"));
        location = new LatLng(mlat, mlon);

        mMap.addMarker(new MarkerOptions().position(location)
                .title("my location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_loc)));

        switch (type) {
            case "aroundme":
                getActivity().setTitle(getString(R.string.string_nearyour));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(9));
                break;
            default:
                location = new LatLng(37.5, 23.44);     // Athens Center
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(6));
        }
    }

    private void showEventOnMap(GoogleMap googleMap) {

        mMap = googleMap;

        // get the position from SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        double lat = prefs.getFloat(("latitude"), 0);
        double lon = prefs.getFloat(("longitude"), 0);
        double mag = prefs.getFloat(("magnitude"), 0);
        double dep = prefs.getFloat(("depth"), 0);
        String title = prefs.getString("title", "Event");

        getActivity().setTitle(String.format("Magnitude %.1f, Depth %.1f km", mag, dep)); // setup Acitivity Title

        // Setup  the Map
        switch (prefs.getString("maptype_list", "map")) {
            case "map":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "hybrid":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        // setup Googlemaps Tools
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // setup description for this event
        mMap.setContentDescription(title + "\nLat: " + lat + "\nLon: " + lon);

        // Add a marker and move the camera
        LatLng location = new LatLng(lat, lon);
        Marker marker = mMap.addMarker(new MarkerOptions().position(location)
                .title(title + "\nLat: " + lat + "\nLon: " + lon)
                .snippet("Depth: " + dep)
                .icon(BitmapDescriptorFactory.defaultMarker((float) setMarkerColor(mag))));

        marker.showInfoWindow();

        // move the camera to the event, depending on the preferences
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(Integer.parseInt(prefs.getString("zoom_list", "7"))));
    }


    private float setMarkerColor(Double mag) {
        if (mag < 2)
            return BitmapDescriptorFactory.HUE_GREEN;   // Green 25% alpha

        if (mag >= 2 && mag < 3)
            return BitmapDescriptorFactory.HUE_YELLOW;  // Yellow 25 % alpha

        if (mag >= 3 && mag < 4)
            return BitmapDescriptorFactory.HUE_MAGENTA; // Magneta 25% alpha

        if (mag >= 4)
            return BitmapDescriptorFactory.HUE_RED;   // Red 25% alpha


        return Color.LTGRAY;
    }


    // Take Snapshot from GoogleMaps
    public void takeSnapshot() throws  FileNotFoundException{
        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) throws NullPointerException {

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(path, "seismosSnapshot.png");
                FileOutputStream fileOutPutStream = null;
                try {
                    fileOutPutStream = new FileOutputStream(imageFile);
                    snapshot.compress(Bitmap.CompressFormat.PNG, 80, fileOutPutStream);

                    fileOutPutStream.flush();
                    fileOutPutStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.parse("file://" + imageFile.getAbsolutePath());

                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Seismos .GR"));
            }
        };

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(callback);
            }
        });
    }

    /**
     * Class: Broadcast Receiver
     */
    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // safety check
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals("aroundme")) {
                setupMap();
            }

            if (intent.getAction().equals("setMapType")) {
                setupMap();
            }

            if (intent.getAction().equals("takeSnapshot")) {
                try {
                    takeSnapshot();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
