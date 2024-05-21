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
package dev.blocky.api.entities.maps;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MapProperty
{
    @SerializedName("city")
    String city;

    @SerializedName("country_code")
    String countryCode;

    @SerializedName("formatted")
    String formatted;

    @SerializedName("lon")
    double lon;

    @SerializedName("lat")
    double lat;

    @Nullable
    public String getCity()
    {
        return city;
    }

    public double getLongitude()
    {
        return lon;
    }

    public double getLatitude()
    {
        return lat;
    }

    @Nullable
    public String getCountryCode()
    {
        return countryCode;
    }

    @NonNull
    public String getFormatted()
    {
        return formatted;
    }

    MapProperty()
    {
    }
}