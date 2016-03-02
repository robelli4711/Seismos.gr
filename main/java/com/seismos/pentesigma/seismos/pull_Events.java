package com.seismos.pentesigma.seismos;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class pull_Events extends AsyncTask<String, Void, String> {

    Context context;
    ArrayList<String> mTitles = new ArrayList();

    public pull_Events(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            getDataFromServer();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return "done";
    }

    @Override
    protected void onPostExecute(String result) {

        // send broadcast to Fragment to change and refresh the MapType
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent("setListAdapter"));

//        setListAdapter();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    public ArrayList<String> getDataFromServer() throws XmlPullParserException, IOException {

        String strEvent = "";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DataSource_Events dataSource_events = new DataSource_Events(context);
        dataSource_events.open();

        URL url = null;
        try {
            url = new URL("http://www.geophysics.geol.uoa.gr/stations/maps/seismicity.xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        factory.setNamespaceAware(false);
        XmlPullParser xpp = null;

        try {
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        // We will get the XML from an input stream
        try {
            xpp.setInput(getInputStream(url), "UTF_8");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        boolean insideItem = false;

        // Returns the type of current event: START_TAG, END_TAG, etc..
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {

                if (xpp.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                } else {
                    if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideItem) {
                            strEvent = xpp.nextText();
                        }
                    } else {
                        if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem) {
                                dataSource_events.createEvent(strEvent, xpp.nextText());
                            }
                        }
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                insideItem = false;
            }

            eventType = xpp.next(); //move to next element
        }

        dataSource_events.close();
        return mTitles;
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public boolean isNetworkavailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
