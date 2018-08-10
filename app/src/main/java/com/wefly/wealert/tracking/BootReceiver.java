package com.wefly.wealert.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wefly.wealert.services.OfflineService;

/**
 * Created by root on 12/20/17.
 */

public class BootReceiver extends BroadcastReceiver
{

    public void onReceive(Context context, Intent intent)
    {
        // Your code to execute when Boot Completd
        Intent serviceIntent = new Intent(context,NavigationService.class);
        Intent serviceOffline = new Intent(context,OfflineService.class);

        Toast.makeText(context, "Startup wefly locate", Toast.LENGTH_LONG).show();
        context.startService(serviceIntent);

        Toast.makeText(context, "Startup wefly offline", Toast.LENGTH_LONG).show();
        context.startService(serviceOffline);
    }
}
