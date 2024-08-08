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
package dev.blocky.api.entities.modchecker;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Date;
import java.util.List;

public class ModCheckerUser
{
    @SerializedName("id")
    int userID;

    @SerializedName("badges")
    List<ModCheckerBadge> badges;

    @SerializedName("granted")
    Date grantedAt;

    @SerializedName("follower")
    long follower;

    public int getUserID()
    {
        return userID;
    }

    @NonNull
    public List<ModCheckerBadge> getBadges()
    {
        return badges;
    }

    @NonNull
    public Date getGrantedAt()
    {
        return grantedAt;
    }

    public long getFollower()
    {
        return follower;
    }
}
