package com.torturedevice.nei;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class HomeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

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
    TextView showTX;
    TextView showRX;
    private Button connectdisconnectDEVICE;

    // globals
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();

    // variables
    Boolean on_off;
    boolean ready;
    String T = "true";
    String F = "false";
    String Payload;
    String userPayload;
    int min;
    int max;

    // BLE things
    //private static final int REQUEST_SELECT_DEVICE = 1;
    //private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    //public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    //private UartService mService = null;
    //private BluetoothDevice mDevice = null;
    //private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    //private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate()");

        //-----------------------------------------------------------------------------------------------------
        // Service Intent to create .txt file
        Intent intentSensor =  new Intent(this, SensorServiceIntent.class);
        startService(intentSensor);

        //-----------------------------------------------------------------------------------------------------
        dataChartImageButton = (ImageButton) findViewById(R.id.image_button_data_chart);
        presetsImageButton = (ImageButton) findViewById(R.id.image_button_presets);
        //temperatureNumberPicker = null;

        connectdisconnectDEVICE = (Button) findViewById(R.id.connect_disconnect_device);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // show disclaimer first time app ever opens
        SharedPreferences sharedPrefDis = getSharedPreferences("homeInfo", Context.MODE_PRIVATE);
        String disclaimer = sharedPrefDis.getString("disclaimer", "false");
        if (disclaimer == "false") {
            disclaimer();
        }

        //-----------------------------------------------------------------------------------------------------
        // get min and max temperatures set
        SharedPreferences sharedPrefMinMax = getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        String minimum = sharedPrefMinMax.getString("minimum", "");
        String maximum = sharedPrefMinMax.getString("maximum", "");
        if ((minimum != "") && (maximum != "")) {
            min = Integer.valueOf(minimum);
            max = Integer.valueOf(maximum);
        } else {
            min = 20;
            max = 0;
        }

        //-----------------------------------------------------------------------------------------------------
        // set Payload
        Payload = Integer.toString(Integer.valueOf(((GlobalDynamicStrings) this.getApplication()).getPayload()) - max );

        // set userPayload
        setUserPayload();

        //-----------------------------------------------------------------------------------------------------
        // power on/off set
        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }

        //-----------------------------------------------------------------------------------------------------
        // ready set
        ready = Boolean.valueOf(((GlobalDynamicStrings) this.getApplication()).getReadyState());

        //-----------------------------------------------------------------------------------------------------
        // power on/off debugger
        showOnOff = (TextView) findViewById(R.id.showOnOff);
        showOnOff.setText("On/Off: " + ((GlobalDynamicStrings) this.getApplication()).getOnOff());

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
        // [[number picker]] for setting output temperature
        temperatureNumberPicker = (NumberPicker)findViewById(R.id.number_picker_temperature);
        String[] values = this.getResources().getStringArray(R.array.select_temperature);
        ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(values));
        // clear top max part of ArrayList
        newValues.subList(0, max).clear();
        // clear bottom min part of ArrayList
        newValues.subList((min-max+1), (20-max+1)).clear();
        temperatureNumberPicker.setMinValue(0);
        temperatureNumberPicker.setMaxValue(newValues.size()-1);
        if (((GlobalDynamicStrings) this.getApplication()).getOnOff() == "true") {
            temperatureNumberPicker.setValue(Integer.valueOf(((GlobalDynamicStrings) this.getApplication()).getPayload()) - max);
            temperatureNumberPicker.setEnabled(true);
        } else {
            temperatureNumberPicker.setValue(10-max);
            temperatureNumberPicker.setEnabled(false);
        }
        temperatureNumberPicker.setDisplayedValues(newValues.toArray(new String[0]));
        temperatureNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        temperatureNumberPicker.setWrapSelectorWheel(false);

        // number picker debugger set from dynamic memory
        showPayload = (TextView) findViewById(R.id.showPayload);
        showPayload.setText("Payload: " + ((GlobalDynamicStrings) this.getApplication()).getPayload()  + " [" + userPayload + "]");

        // number picker listener
        temperatureNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
        @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Payload = Integer.toString(newVal + max);
                changePayload();
                setUserPayload();
                showPayload.setText("Payload: " + Payload + " [" + userPayload + "]");
                if (ready) {
                    sendData();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // set TX
        showTX = (TextView) findViewById(R.id.showTX);
        showTX.setText("TX: ");

        //-----------------------------------------------------------------------------------------------------
        // warning button
        onWarningButtonClickListener();

        //-----------------------------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------------------------
        // [[Connect/Disconnect]] form BLE UART
        connectdisconnectDEVICE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //permissionBLE();
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, BLE_REQUEST);
                if (ready) {
                    sendData();
                    Payload = Integer.toString(10);
                    changePayload();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //messageListView = (ListView) findViewById(R.id.listMessage);
        //listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        //messageListView.setAdapter(listAdapter);
        //messageListView.setDivider(null);
        //btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        //btnSend=(Button) findViewById(R.id.sendButton);
        //edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();


        /*
        // Handle Disconnect & Connect button
        //btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();

                        }
                    }
                }
            }
        });*/
        /*
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });*/
        if (ready) {
            sendData();
        }



        //-----------------------------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------------------------
    }

    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------

    // Handle sending data when on/off is true
    public void sendData() {
        if (((GlobalDynamicStrings) this.getApplication()).getOnOff() == "true") {
            // get payload message to send
            //EditText editText = (EditText) findViewById(R.id.sendText);
            String messageON;
            if (Integer.valueOf(Payload) < 10) {
                messageON = ("0" + Payload);
            } else {
                messageON = (Payload);
            }
            Log.d(TAG, "Payload: " + Payload);
            byte[] value;
            try {
                //send data to service
                value = messageON.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                showTX.setText("TX: " + messageON);
                //Update the log with time stamp
                //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //edtMessage.setText("");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            String messageOFF;
            messageOFF = "10";
            Log.d(TAG, "Payload: " + Payload);
            byte[] value;
            try {
                //send data to service
                value = messageOFF.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                showTX.setText("TX: " + messageOFF);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.d(TAG, "sendData()");
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService + " [ServiceConnection]");
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }
        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
            Log.d(TAG, "onServiceConnected mService = Disconnected" + " [ServiceConnection]");
        }
    };



    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        connectdisconnectDEVICE.setText("Disconnect form Device");
                        //sendData();
                        //edtMessage.setEnabled(true);
                        //btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceSelect)).setText(mDevice.getName()+ " - ready");
                        ready = true;
                        setReadyGSD(ready);
                        //((GlobalDynamicStrings) getActivity().getApplication()).setPayload("10");

                        //.setReadyState("true");

                        //listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        connectdisconnectDEVICE.setText("Connect to Device");
                        //edtMessage.setEnabled(false);
                        //btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceSelect)).setText("Not Connected");
                        ready = false;
                        setReadyGSD(ready);
                        //listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }
            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            // number picker debugger set from dynamic memory
                            showRX = (TextView) findViewById(R.id.showRX);
                            showRX.setText(" | RX: " + text);
                            //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            //listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private void service_init() {
        //Intent bindIntent = new Intent(this, UartService.class);
        //bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "service_init()");

        Intent intent = new Intent(this, UartService.class);

        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        //service_init();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");




        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
         mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResults()");
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceSelect)).setText(mDevice.getName()+ " - connecting");
                    ready = false;
                    setReadyGSD(ready);
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        /*
        else {
            new android.app.AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }*/
    }

    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------




    // creates the action bar menu ----------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        // inflate the menu (settings); adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_home, menu);
        // set correct powerState icon
        MenuItem ON_OFF = menu.findItem(R.id.action_powerState);
        if (!on_off) {
            ON_OFF.setIcon(R.drawable.poweroff);
            ON_OFF.setTitle("Power OFF");
        } else {
            ON_OFF.setIcon(R.drawable.poweron);
            ON_OFF.setTitle("Power ON");
        }
        return true;
    }

    // handles action bar icon selects ------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");
        // once menu item pressed it jumped to activity
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        // toggle on_off when icon is pressed
        if (item.getItemId() == R.id.action_powerState) {
            on_off = !on_off;

            // change icon when pressed/not pressed and set on_off string in GlobalDynamicStrings
            if (on_off) {
                item.setIcon(R.drawable.poweron);
                item.setTitle("Power ON");
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(T);
                temperatureNumberPicker.setValue(Integer.valueOf(((GlobalDynamicStrings) this.getApplication()).getPayload()) - max);
                temperatureNumberPicker.setEnabled(true);
            } else  {
                item.setIcon(R.drawable.poweroff);
                item.setTitle("Power OFF");
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(F);
                temperatureNumberPicker.setValue(10-max);
                temperatureNumberPicker.setEnabled(false);
            }

            //GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();
            showOnOff = (TextView) findViewById(R.id.showOnOff);
            showOnOff.setText("On/Off: " + ((GlobalDynamicStrings) this.getApplication()).getOnOff());

            if (ready) {
                // update data sent
                sendData();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void changePayload() {
        Log.d(TAG, "changePayload()");
        if (Integer.valueOf(Payload) < max){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(max));
            Payload = Integer.toString(max);
            setUserPayload();
        }
        else if (Integer.valueOf(Payload) > min){
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Integer.toString(min));
            Payload = Integer.toString(min);
            setUserPayload();
        }
        else {
            ((GlobalDynamicStrings) this.getApplication()).setPayload(Payload);
        }
    }

    public void setUserPayload() {
        Log.d(TAG, "setUserPayload()");
        if (Integer.valueOf(Payload) < 10){
            userPayload = Integer.toString(Math.abs(Integer.valueOf(Payload) - 10));
        }
        else if (Integer.valueOf(Payload) > 10){
            userPayload = Integer.toString(-1*(Integer.valueOf(Payload) - 10));
        }
        else {
            userPayload = "0";
        }
    }

    // handles disclaimer -------------------------------------------------------------------------------------
    public void onWarningButtonClickListener() {
        Button warningButton = (Button) findViewById(R.id.button_warning);
        warningButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disclaimer();
                    }
                }
        );
    }

    // disclaimer dialog box
    public void disclaimer() {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(HomeActivity.this);
        a_builder.setMessage("Unto thee know thine own self and be'eth not an incompetent fool. Thou shan't use this app/device if thee cannot handle the fire and ice! " +
                "Proceed with assurance that thee will perish with or without our aid. Acknowledge and embrace the uncertainty. Proceed at thine own gamble. Would you like to continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPrefDis = getSharedPreferences("homeInfo", Context.MODE_PRIVATE);
                        String disclaimer = sharedPrefDis.getString("disclaimer", "false");
                        SharedPreferences.Editor editor = sharedPrefDis.edit();
                        editor.putString("disclaimer", "true");
                        editor.apply();
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










    /*
    // checks BLE permissions ---------------------------------------------------------------------------------
    public void permissionBLE() {
        Log.d(TAG, "permissionsBLE()");
        // check if the BLE permission is already available
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // BLE permission is already available
            //Toast.makeText(this, "BLE Location permission has already been granted.", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "onRequestPermissionsResults()");
        permissionBLE  = permissions[0];

        if (requestCode == BLE_REQUEST) {
            // received permission from user for BLE

            // check if the only required permission has been granted
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // BLE permission has been granted
                //Toast.makeText(this, "BLE permission has been granted by the user.", Toast.LENGTH_SHORT).show();
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
    }*/



    // checks BLE permissions ---------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResults()");
        // check if the Files permission is already available
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Files permission has already been granted
            //Toast.makeText(this, "We gucci.", Toast.LENGTH_SHORT).show();
            connectBLE();

        } else {
            // BLE permission has not been granted

            // request BLE permission
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILES_REQUEST);

            // provide additional rationale to the user to get them to grant access
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                //SystemClock.sleep(2000);
                Toast.makeText(this, "Location permission is needed to connect to the BLE device.", Toast.LENGTH_LONG).show();
                //finish();
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                //SystemClock.sleep(2000);
                Toast.makeText(this, "Set permission in Settings -> Apps.", Toast.LENGTH_LONG).show();
                //finish();
            }
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
                //connectdisconnectDEVICE.setText("Disconnect from Device");
                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                Intent newIntent = new Intent(HomeActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            } else {
                //connectdisconnectDEVICE.setText("Connect to Device");
                //Disconnect button pressed
                if (mDevice!=null)
                {
                    mService.disconnect();
                }
            }
        }
    }

    public void setReadyGSD(Boolean bool) {
        ((GlobalDynamicStrings) this.getApplication()).setReadyState(Boolean.toString(bool));
    }



    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------



}