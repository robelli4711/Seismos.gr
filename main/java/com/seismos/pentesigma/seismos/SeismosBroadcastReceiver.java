package com.seismos.pentesigma.seismos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class SeismosBroadcastReceiver extends BroadcastReceiver {

    // restart service every xx seconds
    private static final long REPEAT_TIME = 1000 * 30;

    public SeismosBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Seismos", "Seismos Service Received");

        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SeismosBroadcastTrigger.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();

        // start XX seconds after boot completed
        cal.add(Calendar.SECOND, (int) REPEAT_TIME);

        // fetch every xx seconds
        // InexactRepeating allows Android to optimize the energy consumption
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
        service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
    }
}

