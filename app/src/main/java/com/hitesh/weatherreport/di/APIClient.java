package com.hitesh.weatherreport.di;

import android.content.Context;

import com.hitesh.weatherreport.R;
import com.hitesh.weatherreport.common.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String OPEN_WEATHER_MAP_API =
            Constants.BASE_URL + "weather?q=%s&appid=e6a179eef9fcb8c2f0e745ba1e221ec7&units=metric";

    public static JSONObject getJSON(Context context, String city) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(Constants.API_KEY,
                    context.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if (data.getInt("cod") != 200) {
                return null;
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    /*Added to load data through Retrofit*/
    public static Retrofit getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}