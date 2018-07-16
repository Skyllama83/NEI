package com.torturedevice.nei;

import android.app.Application;

public class GlobalDynamicStrings extends Application {

    // used everywhere to get on/off state
    private String OnOff = "false";
    public String getOnOff() {
        return OnOff;
    }
    public void setOnOff(String OnOff) {
        this.OnOff = OnOff;
    }

    // main value sent to device
    private String Payload = "10";
    public String getPayload() {
        return Payload;
    }
    public void setPayload(String Payload) {
        this.Payload = Payload;
    }

    // preset value that sets main value sent to device
    private String PayloadPreset = "10";
    public String getPayloadPreset() {
        return PayloadPreset;
    }
    public void setPayloadPreset(String PayloadPreset) {
        this.PayloadPreset = PayloadPreset;
    }

    // preset updated text value
    private String PresetState = "Did you set the preset?";
    public String getPresetState() {
        return PresetState;
    }
    public void setPresetState(String PresetState) {
        this.PresetState = PresetState;
    }

    // preset temperature updated text value
    private String TempSet = "";
    public String getTempSet() {
        return TempSet;
    }
    public void setTempSet(String TempSet) {
        this.TempSet = TempSet;
    }

    // the state of ready
    private String ReadyState = "false";
    public String getReadyState() {
        return ReadyState;
    }
    public void setReadyState(String ReadyState) {
        this.ReadyState = ReadyState;
    }

}

    /*GlobalDynamicStrings gds = (GlobalDynamicStrings)getActivity().getApplication();
    GlobalDynamicStrings gds = (GlobalDynamicStrings)this.getApplication();
    String user = "ito";
    gds.setUsername(user);*/

    /*GlobalDynamicStrings gds = (GlobalDynamicStrings)getActivity().getApplication();
    String updatedUser = gds.getUsername();
    TextView textView = (TextView) view.findViewById(R.id.textView3);
    textView.setText(updatedUser);*/