package com.aasfencoders.womensafety.Class;

/**
 *  Data Model class when sending location data to others
 *
 *  latitude : lat. of the current location data
 *  longitude : long. of the current location data
 *  phone : number of the connection
 *  stamp : Timestamp at the moment of sending
 */
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
