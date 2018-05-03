package com.torturedevice.nei;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WelcomeScreenActivity extends AppCompatActivity {
    // splash screen variables
    private static int SPLASH_TIME_OUT = 2000;
    TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        getSupportActionBar().hide();

        SharedPreferences sharedPref = this.getSharedPreferences("profileInfo", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        welcome = (TextView) this.findViewById(R.id.welcome);
        welcome.setText("Welcome " + username + "!");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent (WelcomeScreenActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        },SPLASH_TIME_OUT);



    }
}
