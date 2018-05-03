package com.torturedevice.nei;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DataChartActivity extends AppCompatActivity {

    // variables
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();

    Boolean on_off;
    String T = "true";
    String F = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chart);

        // power on/off set
        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }
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
