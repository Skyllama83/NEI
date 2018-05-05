package com.torturedevice.nei;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class PresetsActivity extends AppCompatActivity {

    // UI variables
    AlarmManager preset_manager;
    TimePicker preset_timepicker;
    TextView update_text;
    TextView update_temp_set;
    Context context;
    PendingIntent pending_intent;
    NumberPicker temperaturePresetNumberPicker;

    // variables
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();
    String PayloadPreset = "10";
    String PresetState;
    String TempSet;
    int min;
    int max;

    Boolean on_off;
    String T = "true";
    String F = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presets);
        this.context = this;

        // initialize our preset manager
        preset_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // initialize our timepicker
        preset_timepicker = (TimePicker) findViewById(R.id.timePicker);

        // initialize our text update box
        update_text = (TextView) findViewById(R.id.update_text);

        // initialize our temperature set update text
        update_temp_set = (TextView) findViewById(R.id.update_temp_set);

        // create an instance of a calender
        final Calendar calendar = Calendar.getInstance();

        // create an intent to the Preset Receiver class
        final Intent my_intent = new Intent(this.context, PresetReceiver.class);

        // initialize status text
        update_text.setText(((GlobalDynamicStrings) this.getApplication()).getPresetState());

        // initialize status text
        update_temp_set.setText(((GlobalDynamicStrings) this.getApplication()).getTempSet());

        // initialize the start button
        Button preset_on = (Button) findViewById(R.id.preset_on);

        // create an onClick listener to start the preset
        preset_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // setting calendar instance with the hour and minute that we picked on the time picker
                calendar.set(Calendar.HOUR_OF_DAY, preset_timepicker.getHour());
                calendar.set(Calendar.MINUTE, preset_timepicker.getMinute());

                // get the int values of the hour and minute
                int hour = preset_timepicker.getHour();
                int minute = preset_timepicker.getMinute();

                // convert the int values to strings
                String hour_string = String.valueOf(hour);
                String minute_string = String.valueOf(minute);

                // convert 24-hour time to 12-hour time
                if (hour > 12) {
                    hour_string = String.valueOf(hour - 12);
                }
                if (hour == 0) {
                    hour_string = "12";
                }

                // 10:7 --> 10:07
                if (minute < 10) {
                    minute_string = "0" + String.valueOf(minute);
                }

                // AM and PM string
                String am_pm = "AM";
                if (hour > 11) {
                    am_pm = "PM";
                }

                // set Payload Preset
                changePayloadPreset();

                // method that changes the update text with global dynamic strings
                PresetState = ("Preset set to: " + hour_string + ":" + minute_string + " " + am_pm);
                changePresetState();
                set_preset_text("Preset set to: " + hour_string + ":" + minute_string + " " + am_pm);

                // method that changes the update temperature text with global dynamic strings
                TempSet = ("Temperature set to: " + findTemp(PayloadPreset));
                changeTempSet();
                set_temp_text("Temperature set to: " + findTemp(PayloadPreset));

                // put in extra string into my_intent
                // tells the clock that you pressed the "preset on" button
                my_intent.putExtra("extra", "preset on");

                // create a pending intent that delays the intent
                // until the specified calender time
                pending_intent = PendingIntent.getBroadcast(PresetsActivity.this, 0,
                        my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // set the preset manager
                preset_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);
            }
        });

        // initialize the stop button
        Button preset_off = (Button) findViewById(R.id.preset_off);

        // create an onClick listener to unset the preset
        preset_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method that changes the update text Textbox
                PresetState = ("Preset off");
                changePresetState();
                set_preset_text("Preset off");

                // method that changes the update temperature text with global dynamic strings
                TempSet = ("");
                changeTempSet();
                set_temp_text("");

                // cancel the preset
                if(pending_intent != null) {
                    preset_manager.cancel(pending_intent);
                    //pending_intent.cancel();
                }

                // put extra string into my_intent
                // tells the clock that you pressed the "preset off' button
                my_intent.putExtra("extra", "preset off");

                // stop the ringtone
                sendBroadcast(my_intent);
            }
        });

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


        //-----------------------------------------------------------------------------------------------------
        // number picker for setting output temperature in preset
            temperaturePresetNumberPicker = (NumberPicker)findViewById(R.id.number_picker_temperature_preset);
            String[] values = this.getResources().getStringArray(R.array.select_temperature);
            ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(values));
            // clear top max part of ArrayList
            newValues.subList(0, max).clear();
            // clear bottom min part of ArrayList
            newValues.subList((min-max+1), (20-max+1)).clear();
            temperaturePresetNumberPicker.setMinValue(0);
            temperaturePresetNumberPicker.setMaxValue(newValues.size() - 1);
            temperaturePresetNumberPicker.setValue(10-max);
            temperaturePresetNumberPicker.setDisplayedValues(newValues.toArray(new String[0]));
            temperaturePresetNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            temperaturePresetNumberPicker.setWrapSelectorWheel(false);
            temperaturePresetNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    PayloadPreset = Integer.toString(newVal + max);
                   }
            });

        //-----------------------------------------------------------------------------------------------------
        // power on/off set
        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }
    }

    public void changePresetState() {
        ((GlobalDynamicStrings) this.getApplication()).setPresetState(PresetState);
    }

    public void changeTempSet() {
        ((GlobalDynamicStrings) this.getApplication()).setTempSet(TempSet);
    }

    public void changePayloadPreset() {
        if (Integer.valueOf(PayloadPreset) < max){
            ((GlobalDynamicStrings) this.getApplication()).setPayloadPreset(Integer.toString(max));
            PayloadPreset = Integer.toString(max);
        }
        else if (Integer.valueOf(PayloadPreset) > min){
            ((GlobalDynamicStrings) this.getApplication()).setPayloadPreset(Integer.toString(min));
            PayloadPreset = Integer.toString(min);
        }
        else {
            ((GlobalDynamicStrings) this.getApplication()).setPayloadPreset(PayloadPreset);
        }
        System.out.println("Preset Payload changed.");
    }

    private void set_preset_text(String output) {
        update_text.setText(output);
    }

    private void set_temp_text(String output) {
        update_temp_set.setText(output);
    }

    private String findTemp(String temp) {
        int num = Integer.valueOf(temp);
        int temperature = 10 - num;
        String temppy = Integer.toString(temperature);
        return temppy;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu (settings); adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_all, menu);

        MenuItem on = menu.findItem(R.id.action_powerStateOff);
        MenuItem off = menu.findItem(R.id.action_powerStateOn);

        off.setVisible(on_off);
        on.setVisible(!on_off);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle on_off when icon is pressed
        if (item.getItemId() == R.id.action_powerStateOff || item.getItemId() == R.id.action_powerStateOn) {
            on_off = !on_off;

            // change icon when pressed/not pressed and set on_off string in GlobalDynamicStrings
            if (on_off) {
                item.setIcon(R.drawable.poweron);
                //gds.setOnOff(T);

                ((GlobalDynamicStrings) this.getApplication()).setOnOff(T);
            } else  {
                item.setIcon(R.drawable.poweroff);
                //gds.setOnOff(F);

                ((GlobalDynamicStrings) this.getApplication()).setOnOff(F);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}


// Once upon a time there was an egg. this was no ordinary egg. this egg had a dream. Its dream was to fly. Fly to the moon where Ito could not eat it.
// It accomplished this dream by riding the back of a flying unicorn. It succeeded and lived happily ever after.

// THE END
