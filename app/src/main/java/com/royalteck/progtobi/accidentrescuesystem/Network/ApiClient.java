package com.royalteck.progtobi.accidentrescuesystem.Network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by PROG. TOBI on 10-Jul-17.
 */

public class ApiClient {
    private static Retrofit retrofit = null;
    private static String BASE_URL = "https://estateform.000webhostapp.com/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new NetworkInterceptor())
                    // .cache(cache)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL).client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
}
