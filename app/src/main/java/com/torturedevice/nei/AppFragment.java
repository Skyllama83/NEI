package com.torturedevice.nei;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.StrictMath.toIntExact;

/**
 * Created by Ito Perez on 2/18/2018.
 */

public class AppFragment extends Fragment{
    private static final String TAG = "AppFragment";

    // variables
    Spinner spinner_min;
    Spinner spinner_max;
    ArrayAdapter<CharSequence> adapter_min;
    ArrayAdapter<CharSequence> adapter_max;
    TextView showMinimum;
    TextView showMaximum;

    String minString;
    String maxString;
    Long minLong;
    Long maxLong;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment, container, false);

        Button showALL = view.findViewById(R.id.show_all);
        Button saveALL = view.findViewById(R.id.save_all_app);

        showMinimum = (TextView) view.findViewById(R.id.showMinimum);
        showMaximum = (TextView) view.findViewById(R.id.showMaximum);

        //-----------------------------------------------------------------------------------------------------
        // get saved min/max or set them if first time running app
        SharedPreferences sharedPref = getContext().getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        String minimum = sharedPref.getString("minimum", "");
        String maximum = sharedPref.getString("maximum", "");
        if ((minimum == "") && (maximum == "")) {
            minimum = "20";
            maximum = "0";
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("minimum", "20");
            editor.putString("maximum", "0");
            editor.apply();
        }
        String minSpin = String.valueOf(Integer.valueOf(minimum)-10);
        String maxSpin = maximum;

        //-----------------------------------------------------------------------------------------------------

        spinner_min = (Spinner) view.findViewById(R.id.spinnerMin);

        adapter_min = ArrayAdapter.createFromResource(getActivity(), R.array.select_temperature_min, android.R.layout.simple_spinner_item);
        adapter_min.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_min.setAdapter(adapter_min);
        spinner_min.setSelection(Integer.parseInt(minSpin));

        spinner_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                minString = Long.toString(parent.getItemIdAtPosition(position + 10));
                minLong = parent.getItemIdAtPosition(position + 10);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //-----------------------------------------------------------------------------------------------------
        spinner_max = (Spinner) view.findViewById(R.id.spinnerMax);

        adapter_max = ArrayAdapter.createFromResource(getActivity(), R.array.select_temperature_max, android.R.layout.simple_spinner_item);
        adapter_max.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_max.setAdapter(adapter_max);
        spinner_max.setSelection(Integer.parseInt(maxSpin));

        spinner_max.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                maxString = Long.toString(parent.getItemIdAtPosition(position));
                maxLong = parent.getItemIdAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //-----------------------------------------------------------------------------------------------------
        // shows the saved minimum and maximum in Shared Preferences
        showALL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getContext().getSharedPreferences("appInfo", Context.MODE_PRIVATE);

                String minimum = sharedPref.getString("minimum", "");
                String maximum = sharedPref.getString("maximum", "");
                showMinimum.setText("minimum: " + minimum);
                showMaximum.setText("maximum: " + maximum);
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // receive input from the Plain Text username and email and saves them to Shared Preferences
        saveALL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (minLong < maxLong) {
                    Toast.makeText(getActivity(), "ERROR: Min is greater than Max", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPref = getContext().getSharedPreferences("appInfo", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("minimum", minString);
                    editor.putString("maximum", maxString);
                    editor.apply();

                    ((GlobalDynamicStrings) getActivity().getApplication()).setPayload("10");

                    Toast.makeText(getActivity(), "APP SETTING HAVE BEEN SAVED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------

        return view;
    }
}
