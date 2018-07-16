package com.torturedevice.nei;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class DataChartActivity extends AppCompatActivity {

    // permissions
    public static final int FILES_REQUEST = 54321;

    // variables
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();
    Boolean on_off;
    String T = "true";
    String F = "false";

    // spinner variables
    String txtFile;
    String[] last3txtFiles = new String[1];
    Spinner spinner_txt;
    ArrayAdapter<String> adapter_txt;
    int lastPosition = 0;

    private RelativeLayout mainLayout;
    private LineChart mChart;
    public static final String TAG = "Data Chart Activity";
    private List<UserData> userData= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chart);

        // get permission for writing to external files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILES_REQUEST);
        }

        //-----------------------------------------------------------------------------------------------------
        // populate last3txtFiles with the last three days of txt files
        //last3txtFiles[0] = ("data" + "1");
        //last3txtFiles[1] = ("data" + "2");
        //last3txtFiles[2] = ("data" + "3");
        last3txtFiles[0] = ("TD:" + getDate());

        //-----------------------------------------------------------------------------------------------------
        // initialize txtFile to today's file
        txtFile = last3txtFiles[0];

        //-----------------------------------------------------------------------------------------------------
        // create chart stuff
        createChart();

        //-----------------------------------------------------------------------------------------------------
        // [[spinnertxt]] settings
        spinner_txt = (Spinner) this.findViewById(R.id.spinnerTXT);

        adapter_txt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, last3txtFiles);
        adapter_txt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_txt.setAdapter(adapter_txt);
        spinner_txt.setSelection(0);

        spinner_txt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtFile = last3txtFiles[position];
                if(lastPosition != position) {
                    Log.d(TAG, "In clear.");
                    mChart.clear();
                }
                lastPosition = position;
                if(lastPosition == position) {
                    Log.d(TAG, "in readUserData_addEntries().");
                    createChart();
                    // check if the Files permission is already available
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        readUserData_addEntries();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "In spinner onNothingSelected.");
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // power on/off set

        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }

        //-----------------------------------------------------------------------------------------------------
    }

    // checks BLE permissions ---------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        // check if the Files permission is already available
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Files permission has already been granted
            //Toast.makeText(this, "We gucci.", Toast.LENGTH_SHORT).show();
        } else {
            // BLE permission has not been granted

            // request BLE permission
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILES_REQUEST);

            // provide additional rationale to the user to get them to grant access
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //SystemClock.sleep(2000);
                Toast.makeText(this, "Files permission is needed in order to save device data.", Toast.LENGTH_LONG).show();
                finish();
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //SystemClock.sleep(2000);
                Toast.makeText(this, "Set permission in Settings -> Apps.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------
    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        mChart.clear();
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
    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------

    // method to create chart
    public void createChart(){
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        // create lin chart
        mChart = new LineChart(this);
        // add to main layout
        mainLayout.addView(mChart);

        // customize line chart
        mChart.setDescription("");
        mChart.setNoDataTextDescription("No data at the moment.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // enable pinch zoom to avoid scaling x and y axis separately
        mChart.setPinchZoom(true);

        // alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        // working with data
        LineData data= new LineData();
        data.setValueTextColor(Color.WHITE);

        // add data to line chart
        mChart.setData(data);

        // get legend object
        Legend leg = mChart.getLegend();

        // customize legend
        leg.setForm(Legend.LegendForm.LINE);
        leg.setTextColor(Color.WHITE);

        // customize axises
        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis yleft = mChart.getAxisLeft();
        yleft.setTextColor(Color.WHITE);
        yleft.setDrawGridLines(true);
        yleft.setAxisMaxValue(21f);

        YAxis yright = mChart.getAxisRight();
        yright.setEnabled(false);
    }

    // method to create set
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Torture Device txt: " + "\"" + txtFile + "\"");
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244,117,177));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);

        return set;
    }

    // method to populate chart
    private void readUserData_addEntries() {
        //-----------------------------------------------------------------------------------------------------
        // find file from storage on phone
        String path = Environment.getExternalStorageDirectory()+"/"+ txtFile + ".txt";
        File file = new File(path);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "file does not exist");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("UTF-8")));

        //-----------------------------------------------------------------------------------------------------
        // addEntries()
        LineData dataChart = mChart.getData();

        //-----------------------------------------------------------------------------------------------------
        // readUserData()
        LineDataSet set = dataChart.getDataSetByIndex(0);

        if (set == null) {
            // creation if null
            set = createSet();
            dataChart.addDataSet(set);
        }

        String line = "";

        try {
            // step over headers
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                Log.d("DataChartActivity", "Line: " + line);

                // split by ','
                String[] tokens = line.split(",");

                // read the data
                UserData dataUser = new UserData();
                if (tokens[0].length() > 0) {
                    dataUser.setTemperature(tokens[0]);
                } else {
                    dataUser.setTemperature("0");
                }
                if (tokens[1].length() > 0) {
                    dataUser.setHMS(tokens[1]);
                } else {
                    dataUser.setHMS("0");
                }
                if (tokens.length >= 3 && tokens[2].length() > 0) {
                    dataUser.setTX(tokens[2]);
                } else {
                    dataUser.setTX("0");
                }
                userData.add(dataUser);

                //-----------------------------------------------------------------------------------------------------

                // add a new random value
                dataChart.addXValue(dataUser.getHMS());
                dataChart.addEntry(new Entry((float) Float.valueOf(dataUser.getTemperature()), set.getEntryCount()), 0);
                //dataChart.addEntry(new Entry((float) (Math.random() * 20), set.getEntryCount()), 0);

                // notify chart data has changed
                mChart.notifyDataSetChanged();

                if(getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                    // limit number of visible entries
                    mChart.setVisibleXRange(4);
                    // scroll to the last entry
                    mChart.moveViewToX(dataChart.getXValCount() - 5);
                }
                if(getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                    // limit number of visible entries
                    mChart.setVisibleXRange(6);
                    // scroll to the last entry
                    mChart.moveViewToX(dataChart.getXValCount() - 7);
                }

                //-----------------------------------------------------------------------------------------------------

                Log.d("MainActivity", "Just created: " + dataUser);
            }
        } catch (IOException e) {
            Log.wtf("MainActitivy", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

    // method to get current time
    public String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(cal.getTime());

        return time;
    }

    // method to get current date
    public String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        String date = sdf.format(cal.getTime());

        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu (settings); adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_all, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle on_off when icon is pressed
        if (item.getItemId() == R.id.action_powerState) {
            on_off = !on_off;

            // change icon when pressed/not pressed and set on_off string in GlobalDynamicStrings
            if (on_off) {
                item.setIcon(R.drawable.poweron);
                item.setTitle("Power ON");
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(T);
            } else {
                item.setIcon(R.drawable.poweroff);
                item.setTitle("Power OFF");
                ((GlobalDynamicStrings) this.getApplication()).setOnOff(F);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
