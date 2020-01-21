package com.aasfencoders.womensafety.Class;

public class DataModel {

    private String latitude;
    private String longitude;
    private String phone;
    private String stamp;

    public DataModel(String latitude, String longitude , String phone , String stamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.stamp = stamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPhone() { return phone; }

    public String getStamp() { return stamp; }

}
