package com.torturedevice.nei;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ito Perez on 2/18/2018.
 */

public class ProfileFragment extends Fragment{
    private static final String TAG = "ProfileFragment";

    // variables
    EditText usernameInput;
    EditText emailInput;
    TextView showUsername;
    TextView showEmail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        usernameInput = (EditText) view.findViewById(R.id.usernameInput);
        emailInput = (EditText) view.findViewById(R.id.emailInput);

        showUsername = (TextView) view.findViewById(R.id.showUsername);
        showEmail = (TextView) view.findViewById(R.id.showEmail);

        final Button saveUSERNAME = view.findViewById(R.id.save_username);
        Button saveEMAIL = view.findViewById(R.id.save_email);
        Button saveALL = view.findViewById(R.id.save_all_profile);
        Button showALL = view.findViewById(R.id.show_all);

        SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");
        String email = sharedPref.getString("email", "");

        // populate EditText hints
        if (!("".equals(username))) {
            usernameInput.setHint(username);
        } else {
            usernameInput.setHint("Enter a Username");
        }
        if (!("".equals(email))) {
            emailInput.setHint(email);
        } else {
            emailInput.setHint("Enter an Email");
        }


        //-----------------------------------------------------------------------------------------------------
        // receive input from the Plain Text username and saves it to Shared Preferences
        saveUSERNAME.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make spaces into empty string
                if (checkIfEmptyString(usernameInput.getText().toString())) {
                    usernameInput.setText("");
                    Toast.makeText(getActivity(), "USERNAME IS EMPTY", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", usernameInput.getText().toString());
                    editor.apply();

                    usernameInput.setHint(usernameInput.getText().toString());

                    Toast.makeText(getActivity(), "USERNAME HAS BEEN SAVED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // receive input from the Plain Text email and saves it to Shared Preferences
        saveEMAIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make spaces into empty string
                if (checkIfEmptyString(emailInput.getText().toString())) {
                    emailInput.setText("");
                    Toast.makeText(getActivity(), "EMAIL IS EMPTY", Toast.LENGTH_SHORT).show();
                } else {
                    // set and save email
                    SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("email", emailInput.getText().toString());
                    editor.apply();

                    emailInput.setHint(emailInput.getText().toString());

                    Toast.makeText(getActivity(), "EMAIL HAS BEEN SAVED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // shows the saved username and email in Shared Preferences
        showALL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);

                String username = sharedPref.getString("username", "");
                String email = sharedPref.getString("email", "");
                showUsername.setText("username: " + username);
                showEmail.setText("email: " + email);
            }
        });

        //-----------------------------------------------------------------------------------------------------
        // receive input from the Plain Text username and email and saves them to Shared Preferences
        saveALL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( checkIfEmptyString(usernameInput.getText().toString()) && checkIfEmptyString(emailInput.getText().toString()) ) {
                    usernameInput.setText("");
                    emailInput.setText("");
                    Toast.makeText(getActivity(), "USERNAME AND EMAIL ARE EMPTY", Toast.LENGTH_SHORT).show();
                }

                if ( checkIfEmptyString(usernameInput.getText().toString()) ^ checkIfEmptyString(emailInput.getText().toString()) ) {
                    if ( (checkIfEmptyString(usernameInput.getText().toString()) == true)  && (checkIfEmptyString(emailInput.getText().toString()) == false) ){
                        // username empty
                        usernameInput.setText("");
                        // set and save email
                        SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("email", emailInput.getText().toString());
                        editor.apply();
                        emailInput.setHint(emailInput.getText().toString());
                        Toast.makeText(getActivity(), "EMAIL HAS BEEN SAVED, USERNAME IS EMPTY", Toast.LENGTH_SHORT).show();
                    }
                    if ( (checkIfEmptyString(usernameInput.getText().toString()) == false)  && (checkIfEmptyString(emailInput.getText().toString()) == true) ){
                        // email empty
                        emailInput.setText("");
                        // set and save username
                        SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("username", usernameInput.getText().toString());
                        editor.apply();
                        usernameInput.setHint(usernameInput.getText().toString());
                        Toast.makeText(getActivity(), "USERNAME HAS BEEN SAVED, EMAIL IS EMPTY", Toast.LENGTH_SHORT).show();
                    }
                }

                if ( !checkIfEmptyString(usernameInput.getText().toString()) && !checkIfEmptyString(emailInput.getText().toString()) ) {
                    SharedPreferences sharedPref = getContext().getSharedPreferences("profileInfo", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", usernameInput.getText().toString());
                    editor.putString("email", emailInput.getText().toString());
                    editor.apply();
                    usernameInput.setHint(usernameInput.getText().toString());
                    emailInput.setHint(emailInput.getText().toString());
                    Toast.makeText(getActivity(), "PROFILE SETTING HAVE BEEN SAVED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //-----------------------------------------------------------------------------------------------------
        return view;
    }

    public boolean checkIfEmptyString(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (c != ' ') {
                return false;
            }
        }
        return true;
    }
}
