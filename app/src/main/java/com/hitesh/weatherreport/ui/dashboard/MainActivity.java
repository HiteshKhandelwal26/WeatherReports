package com.hitesh.weatherreport.ui.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.hitesh.weatherreport.R;
import com.hitesh.weatherreport.common.Constants;
import com.hitesh.weatherreport.common.FetchLocationService;
import com.hitesh.weatherreport.common.Utils;
import com.hitesh.weatherreport.databinding.ActivityMainBinding;
import com.hitesh.weatherreport.roomDB.MyDatabase;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;
import com.hitesh.weatherreport.ui.weatherReport.WeatherReportActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FavCitiesAdapter.ClickListener {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    int mPosition = -1;
    private MyDatabase myDatabase;
    private boolean isFavCity = false;
    private boolean isNewTodo = false;
    private FavCitiesAdapter recyclerViewAdapter;
    private Geocoder geocoder;
    private ActivityMainBinding mBinding;
    /*Receiver to fetch the current locality name of the user*/
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            double latitude = Double.parseDouble(intent.getStringExtra(Constants.LAT));
            double longitude = Double.parseDouble(intent.getStringExtra(Constants.LONG));

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException ignored) {
            }
            try {
                Address address;
                for (int i = 0; i < Objects.requireNonNull(addresses).size(); i++) {
                    address = addresses.get(0);
                    String mCityName = address.getLocality();
                    if (myDatabase.daoAccess().fetchWeatherDetails().size() != 0) {
                        if (!isNewTodo) initView(mCityName);
                    } else {
                        initView(mCityName);
                        unregisterReceiver(broadcastReceiver);
                    }
                    break;

                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.rootLayout);
        geocoder = new Geocoder(this, Locale.getDefault());
        int todo_id = getIntent().getIntExtra("id", -100);

        if (todo_id == -100)
            isNewTodo = true;

        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, MyDatabase.DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        mBinding.btnClick.setOnClickListener(this);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAdapter = new FavCitiesAdapter(this);
    }

    /*Invoke from onCreate */
    private void initView(String mCityName) {
        mBinding.actv.setText(mCityName);
        WeatherPOJO pojo = new WeatherPOJO();
        pojo.cityName = mCityName;
        insertCityNames(pojo);
    }

    @Override
    public void launchIntent(String cityName, int mPosition) {
        this.mPosition = mPosition;
        loadNextScreen(cityName, true, mPosition);
    }

    /*Function to remove the marked favorite city from the list*/
    public void removeItem(WeatherPOJO pojo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.alert));
        builder.setMessage(getResources().getString(R.string.unmarkText));
        builder.setPositiveButton(Constants.YES, (dialog, which) -> deleteCityName(pojo));
        builder.setNegativeButton(Constants.NO, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /*Insert the city name to DB*/
    public void insertCityNames(WeatherPOJO todo) {
        executor.execute(() -> {
            myDatabase.daoAccess().insertWeatherDetails(todo);
            handler.post(() -> {
                Utils.loadSnackBar(getResources().getString(R.string.markedFav), mBinding.rootLayout);
                loadCityName();
            });
        });
    }

    /*Delete the city name to DB*/
    public void deleteCityName(WeatherPOJO todo) {
        if (Utils.isNetworkConnected(this)) {
            executor.execute(() -> {
                myDatabase.daoAccess().deleteWeatherInfo(todo);
                handler.post(this::loadCityName);
            });
        } else {
            Utils.loadSnackBar(getResources().getString(R.string.noInternet), mBinding.rootLayout);
        }
    }

    /*Load the city name to the list of fav cities*/
    private void loadCityName() {
        new Thread(() -> {
            myDatabase.daoAccess().fetchWeatherDetails();
            runOnUiThread(() -> recyclerViewAdapter.updateTodoList(myDatabase.daoAccess().fetchWeatherDetails()));
        }).start();
    }

    @Override
    public void onClick(View view) {
        if (Utils.isNetworkConnected(this)) {
            if (!mBinding.actv.getText().toString().isEmpty()) {
                for (int i = 0; i < myDatabase.daoAccess().fetchWeatherDetails().size(); i++) {
                    isFavCity = myDatabase.daoAccess().fetchWeatherDetails().get(i).cityName.equalsIgnoreCase(mBinding.actv.getText().toString());
                    if (isFavCity) break;
                }
                loadNextScreen(mBinding.actv.getText().toString(), isFavCity, -1);
            } else {
                Utils.loadSnackBar(getResources().getString(R.string.emptyErr), mBinding.rootLayout);
            }
        } else {
            Utils.loadSnackBar(getResources().getString(R.string.noInternet), mBinding.rootLayout);
        }
    }

    /*To load the next screen- Weather information*/
    private void loadNextScreen(String mCityName, boolean isFavCity, int position) {
        Intent intent = new Intent(getApplicationContext(), WeatherReportActivity.class);
        Bundle b = new Bundle();
        b.putBoolean(Constants.IS_FAVOURITE_CITY, isFavCity);
        b.putString(Constants.CURRENT_CITY_NAME, mCityName);
        b.putInt(Constants.LIST_POSITION, position);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.recyclerView.setAdapter(recyclerViewAdapter);
        if (myDatabase.daoAccess().fetchWeatherDetails().size() == 0) {
            registerReceiver(broadcastReceiver, new IntentFilter(FetchLocationService.str_receiver));
            return;
        }
        recyclerViewAdapter.updateTodoList(myDatabase.daoAccess().fetchWeatherDetails());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}