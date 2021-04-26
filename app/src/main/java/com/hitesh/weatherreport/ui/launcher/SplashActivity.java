package com.hitesh.weatherreport.ui.launcher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hitesh.weatherreport.R;
import com.hitesh.weatherreport.common.FetchLocationService;
import com.hitesh.weatherreport.common.Utils;
import com.hitesh.weatherreport.databinding.SplashActivityBinding;
import com.hitesh.weatherreport.ui.dashboard.MainActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;


public class SplashActivity extends AppCompatActivity implements View.OnClickListener {
    private final int SETTINGS_REQUEST_CODE = 1;
    private final Handler mWaitHandler = new Handler();
    Animation animation;
    private SplashActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = SplashActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.rootLayout);
        startService(new Intent(this, FetchLocationService.class));
        checkPermissions();
        mBinding.buttonExplore.setOnClickListener(this);
    }

    /*Check for Location Permission*/
    private void checkPermissions() {
        String[] PERMISSIONS =
                {Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        try {
                            if (report != null) {
                                if (report.areAllPermissionsGranted()) {
                                    statusCheck();
                                } else {
                                    List<PermissionDeniedResponse> mDeniedResponses = report.getDeniedPermissionResponses();

                                    Snackbar mSnackBar = Utils.getSnackBar(getResources().getString(R.string.enable_permission_in_setting), Snackbar.LENGTH_INDEFINITE, mBinding.rootLayout);
                                    if (mDeniedResponses.get(0).isPermanentlyDenied()) {
                                        mSnackBar.setAction(getResources().getString(R.string.permission_settings_snackbar), v -> {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
                                        });
                                    } else {
                                        mSnackBar.setAction(getResources().getString(R.string.permission_enable_snackbar), v -> checkPermissions());
                                        mSnackBar.setActionTextColor(getResources().getColor(R.color.white));
                                    }
                                    mSnackBar.show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Exception: ", "" + e.toString());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(error -> Log.e("There was an error: ", "" + error.toString())).check();

    }

    /*Status check if GPS is turned ON*/
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mBinding.buttonExplore.setVisibility(View.VISIBLE);
            buildAlertMessageNoGps();
        } else {
            mBinding.buttonExplore.setVisibility(View.GONE);
            dropAnimation();
        }
    }


    /*Show ALert-Dialog if GPS is not ON*/
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.turnGPS));
        builder.setMessage(getResources().getString(R.string.turnGPSTxt));
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        builder.setNegativeButton("No", (dialog, id) -> {
            dialog.cancel();
            statusCheck();
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View view) {
        dropAnimation();
    }

    /*Load the Main Activity*/
    private void loadActivity() {
        mWaitHandler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 1000);
    }

    /*Show Animation on Button click*/
    private void dropAnimation() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_away_anim);
            mBinding.buttonExplore.startAnimation(animation);
            mBinding.imageViewMainCloud.startAnimation(animation);
            mBinding.imageViewBottomDrawable.startAnimation(animation);
            mBinding.imageViewBottomDrawableShadow.startAnimation(animation);
            mBinding.imageViewEllipse.startAnimation(animation);
            loadActivity();
        }, 500);

    }
}