package com.seismos.pentesigma.seismos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SeismosBroadcastTrigger extends BroadcastReceiver {
    public SeismosBroadcastTrigger() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Seismos", "Seismos Service Triggered");

        Intent service = new Intent(context, SeismosService.class);
        context.startService(service);
    }
}
