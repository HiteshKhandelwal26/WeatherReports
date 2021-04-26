package com.hitesh.weatherreport.common;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

/*Background service created to check the permissions and load the current locality of the user for the 1st time by default*/
public class FetchLocationService extends Service implements LocationListener {

    public static String str_receiver = "location.service.receiver";
    private final Handler mHandler = new Handler();
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    Location location;
    long notify_interval = 1000;
    Intent intent;


    public FetchLocationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
    }

    private void fn_getlocation() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnable || isNetworkEnable) {
            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    fn_update(location);
                }
            }


            if (isGPSEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    fn_update(location);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    private void fn_update(Location location) {
        intent.putExtra(Constants.LAT, location.getLatitude() + "");
        intent.putExtra(Constants.LONG, location.getLongitude() + "");
        sendBroadcast(intent);
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            mHandler.post(FetchLocationService.this::fn_getlocation);
        }
    }
}