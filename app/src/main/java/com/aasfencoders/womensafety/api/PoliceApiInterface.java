package com.aasfencoders.womensafety.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PoliceApiInterface {

    @GET
    Call<JsonObject> fetchCount(@Url String url);
}
