package com.torturedevice.nei;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Thread.sleep;

public class HomeActivity extends AppCompatActivity {

    // permissions
    public static final int BLE_REQUEST = 12345;
    String permissionBLE;
    Boolean perBLE = false;
    String permissionContacts;

    // bluetooth
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mDevice = null;
    private UartService mService = null;
    public static final String TAG = "Home Activity";

    // UI variables
    ImageButton dataChartImageButton;
    ImageButton presetsImageButton;
    NumberPicker temperatureNumberPicker;
    TextView showOnOff;
    TextView showPayload;
    private Button connectdisconnectDEVICE;

    // globals
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();

    // variables
    Boolean on_off;
    String T = "true";
    String F = "false";
    String Payload;
    int min;
    int max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dataChartImageButton = (ImageButton) findViewById(R.id.image_button_data_chart);
        presetsImageButton = (ImageButton) findViewById(R.id.image_button_presets);
        //temperatureNumberPicker = null;

        connectdisconnectDEVICE = (Button) findViewById(R.id.connect_disconnect_device);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

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
        // power on/off set
        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }

        // power on/off debugger
        showOnOff = (TextView) findViewById(R.id.showOnOff);
        showOnOff.setText("On/Off: " + ((GlobalDynamicStrings) this.getApplication()).getOnOff());
        //showOnOff.setText("On/Off: " + gds.getOnOff());

        //-----------------------------------------------------------------------------------------------------
        connectdisconnectDEVICE.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 permissionBLE();
             }
         });

        //-----------------------------------------------------------------------------------------------------
        // called when the user presses the [[Data Chart]] Button to open Data Chart page
        dataChartImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLoadDataChart = new Intent(HomeActivity.this, DataChartActivity.class);
                startActivity(intentLoadDataChart);
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // called when the user presses the [[Presets]] Button to open Presets page
        presetsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLoadPresets = new Intent(HomeActivity.this, PresetsActivity.class);
                startActivity(intentLoadPresets);
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // number picker for setting output temperature
        temperatureNumberPicker = (NumberPicker)findViewById(R.id.number_picker_temperature);
        String[] values = this.getResources().getStringArray(R.array.select_temperature);
        for (int i = 0; i <= 20; i++) {
            if ((i < max) || (i > min)) {
                values[i] = "-";
            }
        }
        temperatureNumberPicker.setMinValue(0);
        temperatureNumberPicker.setMaxValue(values.length - 1);
        temperatureNumberPicker.setValue(Integer.valueOf(((GlobalDynamicStrings) this.getApplication()).getPayload()));
        temperatureNumberPicker.setDisplayedValues(values);
        temperatureNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        temperatureNumberPicker.setWrapSelectorWheel(false);
        temperatureNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
        @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Payload = Integer.toString(newVal);
                changePayload();
            }
        });

        //number picker debugger
        showPayload = (TextView) findViewById(R.id.showPayload);
        showPayload.setText("Payload: " + ((GlobalDynamicStrings) this.getApplication()).getPayload());

        //-----------------------------------------------------------------------------------------------------
        // warning button
        onWarningButtonClickListener();

        //-----------------------------------------------------------------------------------------------------
    }

    // creates the action bar menu ----------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu (settings); adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem on = menu.findItem(R.id.action_powerStateOff);
        MenuItem off = menu.findItem(R.id.action_powerStateOn);

        off.setVisible(on_off);
        on.setVisible(!on_off);

        return true;
    }

    // handles action bar icon selects ------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // once menu item pressed it jumped to activity
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        // toggle on_off when icon is pressed
        if (item.getItemId() == R.id.action_powerStateOff || item.getItemId() == R.id.action_powerStateOn) {
            on_off = !on_off;

            // change icon when pressed/not pressed and set on_off string in GlobalDynamicStrings
            if (on_off) {
                item.setIcon(R.drawable.poweron);
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(T);
            } else  {
                item.setIcon(R.drawable.poweroff);
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(F);
            }

            //GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();
            showOnOff = (TextView) findViewById(R.id.showOnOff);
            showOnOff.setText("On/Off: " + ((GlobalDynamicStrings) this.getApplication()).getOnOff());

        }
        return super.onOptionsItemSelected(item);
    }

    public void changePayload() {
        if (Integer.valueOf(Payload) < max){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(max));
        }
        else if (Integer.valueOf(Payload) > min){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(min));
        }
        else {
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Payload);
        }
    }

    // handles disclaimer -------------------------------------------------------------------------------------
    public void onWarningButtonClickListener() {
        Button warningButton = (Button) findViewById(R.id.button_warning);
        warningButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder a_builder = new AlertDialog.Builder(HomeActivity.this);
                        a_builder.setMessage("Unto thee know thine own self and be'eth not an incompetent fool. Thou shan't use this app/device if thee cannot handle the fire and ice! " +
                                             "Proceed with assurance that thee will perish with or without our aid. Acknowledge and embrace the uncertainty. Proceed at thine own gamble. Would you like to continue?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                        AlertDialog alert = a_builder.create();
                        alert.setTitle("WARNING (can't sue me now!)");
                        alert.show();
                    }
                }
        );
    }

    // checks BLE permissions ---------------------------------------------------------------------------------
    public void permissionBLE() {
        // check if the BLE permission is already available
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // BLE permission is already available
            Toast.makeText(this, "BLE Location permission has already been granted.", Toast.LENGTH_SHORT).show();
            connectBLE();
        } else {
            // BLE permission has not been granted

            // request BLE permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, BLE_REQUEST);

            // provide additional rationale to the user to get them to grant access
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                SystemClock.sleep(2000);
                Toast.makeText(this, "Location permission is needed to connect to the BLE device.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // checks results of BLE permissions ----------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionBLE  = permissions[0];

        if (requestCode == BLE_REQUEST) {
            // received permission from user for BLE

            // check if the only required permission has been granted
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // BLE permission has been granted
                Toast.makeText(this, "BLE permission has been granted by the user.", Toast.LENGTH_SHORT).show();
                connectBLE();
            }
            if ( !(shouldShowRequestPermissionRationale(permissionBLE)) ) {
                // BLE permission was denied
                SystemClock.sleep(2000);
                Toast.makeText(this, "Set permission in Settings -> Apps.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // enable BLE, if already enabled start connection process
    public void connectBLE() {
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (connectdisconnectDEVICE.getText().equals("Connect to Device")){
                connectdisconnectDEVICE.setText("Disconnect from Device");
                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                Intent newIntent = new Intent(HomeActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            } else {
                connectdisconnectDEVICE.setText("Connect from Device");
                //Disconnect button pressed
                if (mDevice!=null)
                {
                    mService.disconnect();
                }
            }
        }
    }



}
