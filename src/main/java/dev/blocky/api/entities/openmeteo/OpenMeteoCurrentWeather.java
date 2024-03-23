/**
 * ApuJar - A useful bot for Twitch with many cool utility features.
 * Copyright (C) 2024 BlockyDotJar (aka. Dominic R.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.api.entities.openmeteo;

import com.google.gson.annotations.SerializedName;

public class OpenMeteoCurrentWeather
{
    @SerializedName("temperature_2m")
    float temperature;

    @SerializedName("relative_humidity_2m")
    int humidity;

    @SerializedName("apparent_temperature")
    float feelsLike;

    @SerializedName("is_day")
    int isDay;

    @SerializedName("rain")
    float rain;

    @SerializedName("snowfall")
    float snowfall;

    @SerializedName("cloud_cover")
    int cloudCover;

    @SerializedName("wind_speed_10m")
    float windSpeed;

    @SerializedName("wind_direction_10m")
    int windDirection;

    public float getTemperature()
    {
        return temperature;
    }

    public int getHumidity()
    {
        return humidity;
    }

    public float getFeelsLike()
    {
        return feelsLike;
    }

    public boolean isDay()
    {
        return isDay == 1;
    }

    public float getRain()
    {
        return rain;
    }

    public float getSnowfall()
    {
        return snowfall;
    }

    public int getCloudCover()
    {
        return cloudCover;
    }

    public float getWindSpeed()
    {
        return windSpeed;
    }

    public int getWindDirection()
    {
        return windDirection;
    }

    OpenMeteoCurrentWeather()
    {
    }
}
