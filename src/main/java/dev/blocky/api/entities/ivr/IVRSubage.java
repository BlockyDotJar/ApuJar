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
package dev.blocky.api.entities.ivr;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Date;

public class IVRSubage
{
    @SerializedName("followedAt")
    public Date followedAt;

    @SerializedName("streak")
    public IVRSubageStreak streak;

    @SerializedName("cumulative")
    public IVRSubageCumulative cumulative;

    @SerializedName("meta")
    public IVRSubageMeta meta;

    @Nullable
    public Date getFollowedAt()
    {
        return followedAt;
    }

    @Nullable
    public IVRSubageStreak getStreak()
    {
        return streak;
    }

    @Nullable
    public IVRSubageCumulative getCumulative()
    {
        return cumulative;
    }

    @Nullable
    public IVRSubageMeta getMeta()
    {
        return meta;
    }

    IVRSubage()
    {
    }
}
