package com.hitesh.weatherreport.ui.weatherReport;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.hitesh.weatherreport.R;
import com.hitesh.weatherreport.common.Constants;
import com.hitesh.weatherreport.common.Utils;
import com.hitesh.weatherreport.databinding.ActivityWeatherReportBinding;
import com.hitesh.weatherreport.di.APIClient;
import com.hitesh.weatherreport.roomDB.MyDatabase;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*The class gives user the Weather information, also it alows the user to mark it as Favorite City*/
public class WeatherReportActivity extends AppCompatActivity implements View.OnClickListener {

    private final Handler handler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mDataHandler = new Handler(Looper.getMainLooper());
    public int mPosition = -1;
    MyDatabase myDatabase;
    private String cityName = "";
    private boolean isNewTodo = false;
    private ActivityWeatherReportBinding mBinding;
    private JSONObject mJsonData;
    public WeatherReportActivity() {
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWeatherReportBinding.inflate(getLayoutInflater());
        setContentView(mBinding.rootLayout);

        if (savedInstanceState == null) {

            cityName = getIntent().getExtras().getString(Constants.CURRENT_CITY_NAME);
            mPosition = getIntent().getExtras().getInt(Constants.LIST_POSITION);
            boolean isFavCity = getIntent().getExtras().getBoolean(Constants.IS_FAVOURITE_CITY);
            myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, MyDatabase.DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();

            int todo_id = getIntent().getIntExtra("id", -100);

            if (todo_id == -100)
                isNewTodo = true;

            if (isFavCity) {
                mBinding.btnMarkFav.setBackgroundColor(getResources().getColor(R.color.colorHintText));
                mBinding.btnMarkFav.setEnabled(false);
                mBinding.btnMarkFav.setText(getResources().getString(R.string.markedFav));
            } else {
                mBinding.btnMarkFav.setBackgroundColor(getResources().getColor(R.color.white));
                mBinding.btnMarkFav.setText(getResources().getString(R.string.markFav));
                mBinding.btnMarkFav.setEnabled(true);
            }

            mBinding.btnMarkFav.setOnClickListener(this);

            /*If Network available, fetch it from the API to get the latest results, else fetch it from DB*/
            if (Utils.isNetworkConnected(this)) {
                updateWeatherData(cityName);
            } else {
                if (myDatabase.daoAccess().fetchWeatherDetails().size() != 0) {
                    showAllViews();
                    fetchAllDetails();
                } else {
                    hideAllViews();
                    Utils.loadSnackBar(getResources().getString(R.string.noDataStored), mBinding.rootView);
                }
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                //Sunny Day
                mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a01d_svg));
            } else {
                //Clear Night
                mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a01n_svg));
            }
        } else {
            switch (id) {
                case 2:
                    //weather_thunder
                    mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a11d_svg));
                    break;
                case 3:

                case 5:
                    //Rainy &
                    //weather_drizzle
                    mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a09d_svg));
                    break;

                case 6:
                    //weather_snowy
                    mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a13d_svg));
                    break;

                case 7:
                    //weather foggy
                    mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a50n_svg));
                    break;
                case 8:
                    //weather_cloudy
                    mBinding.weatherImage.setBackground(getResources().getDrawable(R.drawable.a04d_svg));
                    break;

            }
        }
    }

    /*To update the views from the JSON data received via API and update the Views*/
    @SuppressLint("SetTextI18n")
    private void renderWeather(JSONObject json) {
        try {
            this.mJsonData = json;
            mBinding.cityField.setText(json.optString(Constants.NAME).toUpperCase(Locale.US) + ", " +
                    json.optJSONObject(Constants.SYS).optString(Constants.COUNTRY));

            JSONObject details = json.getJSONArray(Constants.WEATHER).getJSONObject(0);
            JSONObject main = json.getJSONObject(Constants.MAIN);
            mBinding.textViewWeatherMain.setText(details.getString(Constants.DESC).toUpperCase(Locale.US));
            mBinding.textViewHumidity.setText(main.optString(Constants.HUMIDITY) + "%");
            mBinding.textViewPressure.setText(main.optString(Constants.PRESSURE) + " hPa");
            mBinding.textViewTemperature.setText(Utils.getFloat(main) + "°");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            mBinding.updatedField.setText(Constants.LAST_UPDATE + updatedOn);

            setWeatherIcon(details.getInt(Constants.ID),
                    json.getJSONObject(Constants.SYS).getLong(Constants.SUNRISE) * 1000,
                    json.getJSONObject(Constants.SYS).getLong(Constants.SUNSET) * 1000);

            if (mPosition == 0) {
                deleteWeatherRow(myDatabase.daoAccess().fetchWeatherDetails().get(mPosition));
                insertCityName();
            }
        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    /*Update all the views from DB - if there no network available*/
    private void updateAllViews(List<WeatherPOJO> mList) {
        mBinding.cityField.setText(mList.get(mPosition).cityNameCountry);
        mBinding.textViewWeatherMain.setText(mList.get(mPosition).description);
        mBinding.textViewHumidity.setText(mList.get(mPosition).humidity);
        mBinding.textViewPressure.setText(mList.get(mPosition).pressure);
        mBinding.textViewTemperature.setText(mList.get(mPosition).temperature);
        mBinding.updatedField.setText(mList.get(mPosition).lastUpdate);
        setWeatherIcon(mList.get(mPosition).ID,
                mList.get(mPosition).sunrise,
                mList.get(mPosition).sunset);
    }

    /*Fetch the Weather data from Server API and update the views*/
    void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                //loadCurrentData();
                final JSONObject json = APIClient.getJSON(WeatherReportActivity.this, city);
                if (json == null) {
                    handler.post(() -> {
                        Utils.loadSnackBar(getResources().getString(R.string.place_not_found), mBinding.rootLayout);
                        hideAllViews();
                    });
                } else {
                    handler.post(() -> {
                        showAllViews();
                        renderWeather(json);
                    });
                }
            }
        }.start();
    }


    @Override
    public void onClick(View view) {
        if (isNewTodo) {
            WeatherPOJO todo;
            todo = new WeatherPOJO();
            todo.cityName = cityName;
            insertCityName();
        }
    }


    private void hideAllViews() {
        mBinding.noDataLayout.setVisibility(View.VISIBLE);
        mBinding.upperlayout.setVisibility(View.GONE);
    }

    private void showAllViews() {
        mBinding.noDataLayout.setVisibility(View.GONE);
        mBinding.upperlayout.setVisibility(View.VISIBLE);
    }

    /*Insert the city name to DB*/
    public void insertCityName() {
        executor.execute(() -> {
            myDatabase.daoAccess().insertWeatherDetails(Utils.weatherLoadPojo(cityName, mJsonData));
            handler.post(() -> {
                mBinding.btnMarkFav.setBackgroundColor(getResources().getColor(R.color.colorHintText));
                mBinding.btnMarkFav.setEnabled(false);
                mBinding.btnMarkFav.setText(getResources().getString(R.string.markedFav));
            });
        });
    }

    /*Delete the city name to DB*/
    public void deleteWeatherRow(WeatherPOJO todo) {
        executor.execute(() -> myDatabase.daoAccess().deleteWeatherInfo(todo));
    }

    /*Fetch the Weather Details*/
    public void fetchAllDetails() {
        executor.execute(() -> mDataHandler.post(() -> updateAllViews(myDatabase.daoAccess().fetchWeatherDetails())));
    }
}