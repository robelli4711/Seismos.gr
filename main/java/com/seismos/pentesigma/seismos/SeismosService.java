package com.seismos.pentesigma.seismos;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

public class SeismosService extends Service {

    private final IBinder mBinder = new MyBinder();
    public boolean mFirst = true;


    public void setmFirst(boolean mFirst) {
        this.mFirst = mFirst;
    }

    public SeismosService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        giveFeedback();
        return mBinder;
    }

    public class MyBinder extends Binder {

        SeismosService getService() {

            Log.i("Seismos", "Binder Called");
            sendNotification();
            return SeismosService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Seismos", "Seismos Service OnStart");
        sendNotification();
        return Service.START_STICKY;
    }

    private void sendNotification() {
        Log.i("Seismos", "sendNotification");

        if(mFirst) {
            mFirst = false;
            scheduleNextUpdate();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        DataSource_Events dataSource_events = new DataSource_Events(getApplicationContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            Notification n = new Notification.Builder(this)
                    .setContentTitle("Seismos .GR")
                    .setContentText(dataSource_events.getActualEvent().toString())
                    .setSmallIcon(R.drawable.ic_stat_seismosgr)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setLights(Color.RED, 3000, 3000)
                    .addAction(R.drawable.ic_stat_seismosgr, "More", pIntent).build();

        if (prefs.getBoolean("notification_new_message", true)) {
            n.defaults |= Notification.DEFAULT_SOUND;
        }

        if (prefs.getBoolean("notification_new_message_vibrate", true)) {
            n.defaults |= Notification.DEFAULT_VIBRATE;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);

        scheduleNextUpdate();
    }

    public void giveFeedback() {

        Log.i("Seismos", "Service started");
    }

    private void scheduleNextUpdate() {
        Log.i("Seismos", "scheduleNextUpdate");

        // get the preferences for refresh interval
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int interval = Integer.parseInt(prefs.getString("sync_frequency", "60"));

        // setting up
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTimeMillis = System.currentTimeMillis();
        long nextUpdateTimeMillis = currentTimeMillis + interval * DateUtils.SECOND_IN_MILLIS;
        Time nextUpdateTime = new Time();
        nextUpdateTime.set(nextUpdateTimeMillis);

        if (nextUpdateTime.hour < 8 || nextUpdateTime.hour >= 18) {
            nextUpdateTime.hour = 8;
            nextUpdateTime.minute = 0;
            nextUpdateTime.second = 0;
            nextUpdateTimeMillis = nextUpdateTime.toMillis(false) + DateUtils.DAY_IN_MILLIS;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
    }

}
