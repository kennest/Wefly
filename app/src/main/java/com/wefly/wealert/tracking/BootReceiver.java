package com.wefly.wealert.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by root on 12/20/17.
 */

public class BootReceiver extends BroadcastReceiver
{

    public void onReceive(Context context, Intent intent)
    {
        // Your code to execute when Boot Completd
        Toast.makeText(context, "Démarage wefly locate", Toast.LENGTH_LONG).show();
        Intent serviceIntent = new Intent(context,NavigationService.class);
        context.startService(serviceIntent);
    }
}
