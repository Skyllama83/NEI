package com.torturedevice.nei;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class DataChartActivity extends AppCompatActivity {

    // variables
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();

    Boolean on_off;
    String T = "true";
    String F = "false";

    private RelativeLayout mainLayout;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chart);

        //-----------------------------------------------------------------------------------------------------
        // chart stuff

        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        //mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        //mainLayout = findViewById(R.id.mainLayout);

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

        //-----------------------------------------------------------------------------------------------------
        // power on/off set

        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }

        //-----------------------------------------------------------------------------------------------------
    }

    @Override
    protected void onResume() {
        super.onResume();
        // now to simulate real time data

        new Thread(new Runnable() {

            @Override
            public void run() {
                // add 100 entries
                for (int i = 0; i < 100; i++) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry(); // chart is notified of update in addEntry method
                        }
                    });

                    // pause between adds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // manage error...
                    }
                }
            }
        }).start();
    }

    // method to add entry data to the line chart
    private void addEntry() {
        LineData data = mChart.getData();
        // add data only if device is on
        if (((GlobalDynamicStrings) this.getApplication()).getOnOff() == "true") {
            if (data != null) {
                LineDataSet set = data.getDataSetByIndex(0);

                if (set == null) {
                    // creation if null
                    set = createSet();
                    data.addDataSet(set);
                }

                // add a new random value
                data.addXValue(getTime());
                data.addEntry(new Entry((float) (Math.random() * 20), set.getEntryCount()), 0);


                // notify chart data has changed
                mChart.notifyDataSetChanged();

                // limit number of visible entries
                mChart.setVisibleXRange(4);

                // scroll to the last entry
                mChart.moveViewToX(data.getXValCount() - 5);
            }
        }
    }

    // method to create set
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Temperature in °F");
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

    // method to get current time
    public String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(cal.getTime());

        return time;
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
