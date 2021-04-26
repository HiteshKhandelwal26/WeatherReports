package com.hitesh.weatherreport.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/*File created to access the common methods across the application*/
public class Utils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static Snackbar getSnackBar(String message, int duration, View view) {
        return Snackbar
                .make(view, message, duration);
    }

    public static void loadSnackBar(String msg, View view) {
        Snackbar mSnackBar = getSnackBar(msg, Snackbar.LENGTH_SHORT, view);
        mSnackBar.show();
    }

    /*Insert the json object data to Weather Model class*/
    public static WeatherPOJO weatherLoadPojo(String cityName, JSONObject json) {
        WeatherPOJO weatherPOJO = new WeatherPOJO();
        JSONObject details;
        try {
            details = json.getJSONArray(Constants.WEATHER).getJSONObject(0);

            JSONObject main = json.getJSONObject(Constants.MAIN);
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));

            weatherPOJO.cityName = cityName;
            weatherPOJO.cityNameCountry = json.optString(Constants.NAME).toUpperCase(Locale.US) + ", " +
                    json.getJSONObject(Constants.SYS).optString(Constants.COUNTRY);
            weatherPOJO.description = details.optString(Constants.DESC).toUpperCase(Locale.US);
            weatherPOJO.temperature = getFloat(main) + "°";
            weatherPOJO.humidity = main.optString(Constants.HUMIDITY) + "%";
            weatherPOJO.pressure = main.optString(Constants.PRESSURE) + " hPa";
            weatherPOJO.lastUpdate = Constants.LAST_UPDATE + updatedOn;
            weatherPOJO.ID = details.getInt(Constants.ID);
            weatherPOJO.sunrise = json.getJSONObject(Constants.SYS).getLong(Constants.SUNRISE) * 1000;
            weatherPOJO.sunset = json.getJSONObject(Constants.SYS).getLong(Constants.SUNSET) * 1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherPOJO;
    }

    public static float getFloat(JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble("temp");
    }
}
