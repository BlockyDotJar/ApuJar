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
package dev.blocky.api.entities.wordle;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.Nullable;

public class Wordle
{
    @SerializedName("id")
    int id;

    @SerializedName("solution")
    String solution;

    @SerializedName("days_since_launch")
    int daysSinceLaunch;

    @SerializedName("editor")
    String editor;

    public int getID()
    {
        return id;
    }

    @Nullable
    public String getSolution()
    {
        return solution;
    }

    public int getDaysSinceLaunch()
    {
        return daysSinceLaunch;
    }

    @Nullable
    public String getEditor()
    {
        return editor;
    }

    Wordle()
    {
    }
}
