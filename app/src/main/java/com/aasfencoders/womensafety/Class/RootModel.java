package com.aasfencoders.womensafety.Class;

import com.google.gson.annotations.SerializedName;

// The body of the data message sent through the Firebase Cloud Messaging (FCM)
public class RootModel {

    @SerializedName("to") //  "to" changed to token
    private String token;

    @SerializedName("data")
    private DataModel data;

    public RootModel(String token, DataModel data) {
        this.token = token;
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DataModel getData() {
        return data;
    }

    public void setData(DataModel data) {
        this.data = data;
    }
}
