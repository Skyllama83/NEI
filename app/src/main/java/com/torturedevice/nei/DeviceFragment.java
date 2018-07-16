package com.torturedevice.nei;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Scanner;

/**
 * Created by Ito Perez on 2/18/2018.
 */

public class DeviceFragment extends Fragment {

    private static final String TAG = "DeviceFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_fragment, container, false);

        System.out.println("Beginning of onCreateView.");

        Button openNORDIC = (Button) view.findViewById(R.id.open_nordic);

        // opens Nordic nRF UART and sleeps NEI then reverses when returns
        openNORDIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Inside onClick before intent jump.");
                Intent intentLoadMainActivity = new Intent(getActivity(), MainActivity.class);
                startActivity(intentLoadMainActivity);
                System.out.println("Inside onClick after intent jump.");
                //Processor();
            }
        });

        System.out.println("Right after, outside onClick.");

        return view;
    }

    /*
    public void Processor() {
        synchronized (Thread.currentThread()) {
            try {
                System.out.println("Main thread running.");
                Thread.currentThread().wait();
                System.out.println("Resumed.");
            } catch (InterruptedException e) {
                Log.e("NEI:", "Main thread interrupted while waiting.");
                e.printStackTrace();
            }
            Intent intentLoadMainActivity = new Intent(getActivity(), MainActivity.class);
            startActivity(intentLoadMainActivity);
            notify();

        }
    }*/

}



