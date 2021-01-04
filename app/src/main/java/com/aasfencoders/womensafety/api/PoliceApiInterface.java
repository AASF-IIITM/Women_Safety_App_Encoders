package com.aasfencoders.womensafety.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

// Api Interface for fetching the value from the appropriate URL
// We need to fetch data, so we are calling GET request
public interface PoliceApiInterface {

    @GET
    Call<JsonObject> fetchCount(@Url String url);
}
