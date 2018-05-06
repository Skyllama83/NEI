package com.torturedevice.nei;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    // variables
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();

    Boolean on_off;
    String T = "true";
    String F = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate: Starting.");

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // set up the ViewPager with the sections adapter
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // power on/off set
        if (F.equals(((GlobalDynamicStrings) this.getApplication()).getOnOff())) {
            on_off = false;
        } else {
            on_off = true;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new AppFragment(), "App");
        adapter.addFragment(new ProfileFragment(), "Profile");
        adapter.addFragment(new DeviceFragment(), "Device");
        viewPager.setAdapter(adapter);
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
