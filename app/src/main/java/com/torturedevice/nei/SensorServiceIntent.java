package com.torturedevice.nei;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Thread.sleep;

public class SensorServiceIntent extends IntentService {

    // variables
    private static final String TAG = "SensorServiceIntent";
    String OnOff;
    String Ready;
    String FILE_HEADER = "Temperature,HMS,TX";

    // new UserData object for the data
    UserData data = new UserData();

    public SensorServiceIntent() {
        super("SensorServiceIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // this is what the service does
        Log.d(TAG, "THE SERVICE HAS STARTED");

        while(true){

            writeUserData();

            OnOff = ((GlobalDynamicStrings) this.getApplication()).getOnOff();
            Ready = ((GlobalDynamicStrings) this.getApplication()).getReadyState();

            String filename = "TD:" + getDate();
            String content = "\n" + data.getTemperature() + "," + data.getHMS() + "," + data.getTX();

            if(OnOff == "true"){
                Log.d(TAG, "Just added: " + data);

                if(filename.equals("") && content.equals("")) {
                    saveTextAsFile(filename, content);
                }

                saveTextAsFile(filename, content);
            }

            // delay how often we add data points
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private List<UserData> userData= new ArrayList<>();

    private void writeUserData() {
        // set temperature
        data.setTemperature(((GlobalDynamicStrings) this.getApplication()).getPayload());

        // set time
        data.setHMS(getTime());

        // set TX
        data.setTX("null");

        // add everything to userData class
        userData.add(data);
    }

    private void saveTextAsFile(String filename, String content){
        String fileName = filename + ".txt";


        // get file to see if it exist
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),fileName);

        if(!file.exists()) {
            // if we are creating file for the first time, add header
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(FILE_HEADER.getBytes());
                fos.close();
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "File not Found!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving!", Toast.LENGTH_SHORT).show();
            }
        }

        // write to file
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not Found!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving!", Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        Log.d(TAG, "SensorServiceIntent: onDestroy()");
        Log.d(TAG, "THE SERVICE HAS ENDED");
        super.onDestroy();
    }

}

