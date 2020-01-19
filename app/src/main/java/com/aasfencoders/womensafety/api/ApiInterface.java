package com.aasfencoders.womensafety.api;

import com.aasfencoders.womensafety.Class.RootModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ApiInterface {

    @Headers({"Authorization: key=AAAAsnu3WKI:APA91bFwFtwVDKZYXwRLma1w8Kf_ooae3oYA7DB2TDT2vt3i24JfR0eDZWTV3sdMkGpr1-HNEN6-_Y3u6A-EEIpinOXZZbTTzaM1U_6KNahpQiaadsytFxfMxtEWkR7IPrCiVJxUwOtB" , "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendLocation(@Body RootModel root);
}



