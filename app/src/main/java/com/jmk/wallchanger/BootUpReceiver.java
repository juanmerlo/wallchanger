package com.jmk.wallchanger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by juanmartin on 12/12/2016.
 */

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

         /***** For start Service  ****/
        Intent myIntent = new Intent(context, ChangeWallpaperService.class);
        context.startService(myIntent);

    }
}
