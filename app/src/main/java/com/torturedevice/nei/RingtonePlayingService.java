package com.torturedevice.nei;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RingtonePlayingService extends Service {

    // variables
    MediaPlayer media_song;
    int startId;
    boolean isRunning;
    String PayloadNow;
    int min;
    int max;

    // notification
    NotificationCompat.Builder notification;
    private static final int uniqueID = 69830;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // fetch the extra string values
        String state = intent.getExtras().getString("state");
        String alarm = intent.getExtras().getString("alarm");

        Log.e("Ring state: extra is ", state);

        // this converts the extra strings from the intent
        // to start IDs, values 0 or 1
        assert state != null;
        switch (state) {
            case "preset on":
                startId = 1;
                break;
            case "preset off":
                startId = 0;
                Log.e("Start ID is ", state);
                break;
            default:
                startId = 0;
                break;
        }

        // creates notification
        notification = new NotificationCompat.Builder(this);
        // deletes notification after it is opened
        notification.setAutoCancel(true);

        // get min and max temperatures set
        SharedPreferences sharedPref = getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        String minimum = sharedPref.getString("minimum", "");
        String maximum = sharedPref.getString("maximum", "");
        if ((minimum != "") && (maximum != "")) {
            min = Integer.valueOf(minimum);
            max = Integer.valueOf(maximum);
        } else {
            min = 20;
            max = 0;
        }

        // if else statements

        // if there is no music playing and the user pressed "preset on"
        // music should start playing
        if (!this.isRunning && startId == 1) {
            Log.e("there is no music, ", "and you want start");

            // create an instance of the media player
            assert alarm != null;
            switch(alarm) {
                case "0":
                    media_song = MediaPlayer.create(this, R.raw.silence);
                    break;
                case "1":
                    media_song = MediaPlayer.create(this, R.raw.katyperryhotncoldringtone);
                    break;
                case "2":
                    media_song = MediaPlayer.create(this, R.raw.pokemonthemesongoriginal);
                    break;
                default:
                    media_song = MediaPlayer.create(this, R.raw.silence);
            }

            // start the ringtone
            //media_song.setLooping(true);
            media_song.start();

            this.isRunning = true;
            this.startId = 0;

            // build the notification
            notification.setSmallIcon(R.drawable.widowmaker);
            // ticker text
            notification.setTicker("Torture Device Preset");
            // time notification is sent
            notification.setWhen(System.currentTimeMillis());
            // title of notification
            notification.setContentTitle("A preset is going off!");
            // body of notification
            notification.setContentText("Click before you burn/freeze your hand off");

            // whenever notification is clicked the user is taken to the Home Activity
            Intent intent_presets_activity = new Intent(this, HomeActivity.class);
            PendingIntent pending_intent_presets_activity = PendingIntent.getActivity(this,
                    0, intent_presets_activity, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pending_intent_presets_activity);

            // builds notification and issues it (sends it out to the device)
            // dude that builds and sends out notifications
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // sending it out
            nm.notify(uniqueID, notification.build());

            // turn on On/Off, set Payload
            ((GlobalDynamicStrings) this.getApplication()).setOnOff("true");
            PayloadNow = ((GlobalDynamicStrings) this.getApplication()).getPayloadPreset();
            changePayload();
            //((GlobalDynamicStrings) this.getApplication()).setPayload(PayloadNow);

            // when in app jump to Home Activity
            Intent intentJumpHome = new Intent(this, HomeActivity.class);
            startActivity(intentJumpHome);
        }
        // if there is music playing and the user pressed "preset off"
        // music should stop playing
        else if (this.isRunning && startId == 0) {
            Log.e("there is music, ", "and you want end");

            // stop the ringtone
            media_song.stop();
            media_song.reset();

            this.isRunning = false;
            this.startId = 0;
        }
        // these are if the user presses random buttons
        // just to bug-proof the app

        // if there is no music playing and the user presses "preset off"
        // do nothing
        else if (!this.isRunning && startId == 0) {
            Log.e("there is no music, ", "and you want end");

            this.isRunning = false;
            this.startId = 0;
        }
        // if there is music playing and the user pressed "preset on'
        // do nothing
        else if (this.isRunning && startId == 1) {
            Log.e("there is music, ", "and you want start");

            this.isRunning = true;
            this.startId = 1;
        }
        // can't think of anything else, just to catch the odd events
        else {
            Log.e("else, ", "somehow you reached this");
        }

        return START_NOT_STICKY;
    }

    public void changePayload() {
        if (Integer.valueOf(PayloadNow) < max){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(max));
        }
        else if (Integer.valueOf(PayloadNow) > min){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(min));
        }
        else {
            ((GlobalDynamicStrings) this.getApplication()).setPayload(PayloadNow);
        }
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Log.e("on Destroy called", "ta da");

        super.onDestroy();
        this.isRunning = false;
    }


}

    /*
    // notification
    // set up the notification service
    NotificationManager notify_manager = (NotificationManager)
            getSystemService(NOTIFICATION_SERVICE);
    // set up an intent that goes to the Presets Activity
    Intent intent_presets_activity = new Intent(this.getApplicationContext(), PresetsActivity.class);
    // set up a pending intent
    PendingIntent pending_intent_presets_activity = PendingIntent.getActivity(this, 0,
            intent_presets_activity, 0);

    // make the notification parameters
    Notification notification_popup = new Notification.Builder(this)
            .setContentTitle("A preset is going off!")
            .setContentText("Click me!")
            .setContentIntent(pending_intent_presets_activity)
            .setAutoCancel(true)
            .build();

    // set up the notification call command
    notify_manager.notify(0, notification_popup);
    */