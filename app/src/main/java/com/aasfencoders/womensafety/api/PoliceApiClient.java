package com.aasfencoders.womensafety.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PoliceApiClient {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/search/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
