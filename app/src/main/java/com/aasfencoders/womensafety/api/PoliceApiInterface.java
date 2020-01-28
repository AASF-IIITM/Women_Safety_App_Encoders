package com.aasfencoders.womensafety.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PoliceApiInterface {

    @GET("json?location=26.2183,78.1828&rankby=distance&types=police&sensor=false&key=AIzaSyDzbVaqexiRvDpSt3t9oO2kwEu34Qbm3QI")
    Call<JsonObject> fetchCount();
}
