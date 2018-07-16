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
        String get_state_string = intent.getExtras().getString("state");

        // fetch extra stings from the intent
        String get_alarm_string = intent.getExtras().getString("alarm");

        Log.e("What is the state? ", get_state_string);

        //Log.e("What is the ringtone? ", get_alarm_string);

        // create an intent to the ringtone service
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        // pass the extra string from PresetActivity to the RingtonePlayingService
        service_intent.putExtra("state", get_state_string);

        // pass the extra string from PresetActivity to the RingtonePlayingService
        service_intent.putExtra("alarm", get_alarm_string);

        // start the ringtone service
        context.startService(service_intent);
    }
}
