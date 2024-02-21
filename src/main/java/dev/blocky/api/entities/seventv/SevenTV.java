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
package dev.blocky.api.entities.seventv;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;

public class SevenTV
{
    @SerializedName("data")
    SevenTVData data;

    @SerializedName("errors")
    ArrayList<SevenTVError> errors;

    @SerializedName("emotes")
    ArrayList<SevenTVEmote> emotes;

    @Nullable
    public SevenTVData getData()
    {
        return data;
    }

    @Nullable
    public ArrayList<SevenTVError> getErrors()
    {
        return errors;
    }

    @Nullable
    public ArrayList<SevenTVEmote> getEmotes()
    {
        return emotes;
    }

    SevenTV()
    {
    }
}
