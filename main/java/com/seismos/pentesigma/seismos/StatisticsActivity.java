package com.seismos.pentesigma.seismos;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain_stats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Numbers");

        LinearLayout l0 = (LinearLayout) findViewById(R.id.l0);
        LinearLayout l1 = (LinearLayout) findViewById(R.id.stats_ll1_1);
        LinearLayout l2 = (LinearLayout) findViewById(R.id.stats_ll2_1);
        LinearLayout l3 = (LinearLayout) findViewById(R.id.stats_ll3);

        LinearLayout l0_48 = (LinearLayout) findViewById(R.id.text_eventcount48_layout);
        LinearLayout l1_48 = (LinearLayout) findViewById(R.id.stats_ll1_48);
        LinearLayout l2_48 = (LinearLayout) findViewById(R.id.stats_ll2_48);
        LinearLayout l3_48 = (LinearLayout) findViewById(R.id.stats_ll3_48);

        l0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllTodaysEvents"));

                finish();
            }
        });

        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllSmallMagnitudes"));

                finish();
            }
        });

        l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllMediumMagnitudes"));

                finish();
            }
        });

        l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllLargeMagnitudes"));

                finish();
            }
        });

        l0_48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllMagnitudes_48"));

                finish();
            }
        });

        l1_48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllSmallMagnitudes_48"));

                finish();
            }
        });

        l2_48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllMediumMagnitudes_48"));

                finish();
            }
        });

        l3_48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(getApplication().getApplicationContext()).sendBroadcast(
                        new Intent("getAllLargeMagnitudes_48"));

                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        try {

            new SetSummary().execute("").get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public class SetSummary extends AsyncTask<String, Void, String> {

        Context context;
        ArrayList<String> mTitles = new ArrayList();
        DataSource_Events events;

        @Override
        protected void onPostExecute(String result) {

            // Daily total
            TextView total = (TextView) findViewById(R.id.text_eventcount1);
            total.setText(String.valueOf(events.countTodays()));

            // magnitude counters TODAY
            TextView small = (TextView) findViewById(R.id.text_eventcount3);
            small.setText(String.valueOf(events.countMagnitude(0, 2.9)));

            TextView medium = (TextView) findViewById(R.id.text_eventcount5);
            medium.setText(String.valueOf(events.countMagnitude(3, 4.9)));

            TextView large = (TextView) findViewById(R.id.text_eventcount9);
            large.setText(String.valueOf(events.countMagnitude(5, 10)));

            // Daily total
            TextView total48 = (TextView) findViewById(R.id.text_eventcount48);
            total48.setText(String.valueOf(events.count()));

            // magnitude counters TODAY
            TextView small48 = (TextView) findViewById(R.id.text_eventcount3_48);
            small48.setText(String.valueOf(events.countMagnitude48(0, 2.9)));

            TextView medium48 = (TextView) findViewById(R.id.text_eventcount5_48);
            medium48.setText(String.valueOf(events.countMagnitude48(3, 4.9)));

            TextView large48 = (TextView) findViewById(R.id.text_eventcount9_48);
            large48.setText(String.valueOf(events.countMagnitude48(5, 10)));
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected String doInBackground(String... params) {

            setSummary();
            return null;
        }

        private void setSummary() {

            events = new DataSource_Events(getApplicationContext());

        }
    }
}
