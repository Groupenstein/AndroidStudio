package com.groupenstein.groupenstein;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by Brett on 12/28/2014.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {        ComponentName comp = new ComponentName(context.getPackageName(),
            GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        Bundle extras = intent.getExtras();
        String msg = extras.getString("message");
        if (msg != null || msg != "")
        {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }

    }
}
