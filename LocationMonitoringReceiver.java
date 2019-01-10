package com.deepwares.checkpointdwi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationMonitoringReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("broadcast_receiver: ", "location_monitoring_service");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, LocationService.class));
            } else {
                context.startService(new Intent(context, LocationService.class));
            }
        }
    }
}
