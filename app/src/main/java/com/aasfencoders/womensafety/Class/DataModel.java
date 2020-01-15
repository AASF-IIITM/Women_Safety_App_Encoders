package com.aasfencoders.womensafety.Class;

public class DataModel {

    private String latitude;
    private String longitude;
    private String phone;

    public DataModel(String latitude, String longitude , String phone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
    }


    public String getPhone() {
        return phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
