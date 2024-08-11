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

import java.util.List;

public class SevenTVTwitchUser
{
    @SerializedName("display_name")
    String userDisplayName;

    @SerializedName("emote_set")
    SevenTVEmoteSet emoteSet;

    @SerializedName("user")
    SevenTVUser user;

    @SerializedName("cosmentics")
    List<SevenTVCosmetic> cosmentics;

    @Nullable
    public String getUserDisplayName()
    {
        return userDisplayName;
    }

    @Nullable
    public SevenTVEmoteSet getCurrentEmoteSet()
    {
        return emoteSet;
    }

    @Nullable
    public SevenTVUser getUser()
    {
        return user;
    }

    @Nullable
    public List<SevenTVCosmetic> getCosmentics()
    {
        return cosmentics;
    }

    SevenTVTwitchUser()
    {
    }
}
