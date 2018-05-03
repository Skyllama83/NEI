package com.torturedevice.nei;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Ito Perez on 2/18/2018.
 */

public class PresetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("We are in the receiver.", "Yay!");

        // fetch extra stings from the intent
        String get_your_string = intent.getExtras().getString("extra");

        Log.e("What is the key? ", get_your_string);

        // create an intent to the ringtone service
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        // pass the extra string from PresetActivity to the RingtonePlayingService
        service_intent.putExtra("extra", get_your_string);

        // start the ringtone service
        context.startService(service_intent);
    }
}
