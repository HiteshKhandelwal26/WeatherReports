package com.hitesh.weatherreport.roomDB.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hitesh.weatherreport.roomDB.MyDatabase;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;

import java.util.List;

/*DAO Interface*/
@Dao
public interface DaoAccess {

    @Insert
    void insertWeatherDetails(WeatherPOJO weatherPOJO);

    @Query("SELECT * FROM " + MyDatabase.TABLE_WEATHER_TODO)
    List<WeatherPOJO> fetchWeatherDetails();

    @Delete
    void deleteWeatherInfo(WeatherPOJO todo);
}
