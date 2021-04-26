package com.hitesh.weatherreport.roomDB.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hitesh.weatherreport.roomDB.MyDatabase;

import java.io.Serializable;
/*Entity class, to create the Table name and the columns to save Weather information*/

@Entity(tableName = MyDatabase.TABLE_WEATHER_TODO)
public class WeatherPOJO implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int todo_id;
    public String cityName;
    public String cityNameCountry;
    public String description;
    public String temperature;
    public String humidity;
    public String pressure;
    public String lastUpdate;
    public int ID;
    public long sunrise;
    public long sunset;
}
