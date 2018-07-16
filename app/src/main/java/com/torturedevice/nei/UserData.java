package com.torturedevice.nei;

class UserData {
    private String temperature;
    private String hms;
    private String tx;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHMS() {
        return hms;
    }

    public void setHMS(String hms) {
        this.hms = hms;
    }

    public String getTX() {
        return tx;
    }

    public void setTX(String tx) {
        this.tx = tx;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "temperature='" + temperature + '\'' +
                ", hms='" + hms + '\'' +
                ", tx='" + tx + '\'' +
                '}';
    }
}
