package com.fti.sisfo.tours.tools;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, AndroidLocationServices.class);
        context.startService(myIntent);

    }
}