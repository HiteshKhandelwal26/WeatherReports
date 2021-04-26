package com.hitesh.weatherreport.roomDB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.hitesh.weatherreport.roomDB.dao.DaoAccess;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;


/*Database class*/
@Database(entities = {WeatherPOJO.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

    public static final String DB_NAME = "app_db";
    public static final String TABLE_WEATHER_TODO = "weather_details";

    public abstract DaoAccess daoAccess();

}
