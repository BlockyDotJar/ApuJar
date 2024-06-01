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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class SevenTVEmote
{
    @SerializedName("id")
    String emoteID;

    @SerializedName("name")
    String emoteName;

    @SerializedName("flags")
    int emoteFlags;

    @SerializedName("listed")
    boolean isListed;

    @SerializedName("animated")
    boolean isAnimated;

    @SerializedName("data")
    SevenTVEmote data;

    @NonNull
    public String getEmoteID()
    {
        return emoteID;
    }

    @NonNull
    public String getEmoteName()
    {
        return emoteName;
    }

    public int getEmoteFlags()
    {
        return emoteFlags;
    }

    public boolean isListed()
    {
        return isListed;
    }

    public boolean isAnimated()
    {
        return isAnimated;
    }

    @Nullable
    public SevenTVEmote getData()
    {
        return data;
    }

    SevenTVEmote()
    {
    }
}
