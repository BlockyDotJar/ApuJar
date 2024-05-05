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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;

public class IVRUser
{
    @SerializedName("banned")
    boolean isBanned;

    @SerializedName("banReason")
    String banReason;

    @SerializedName("displayName")
    String userDisplayName;

    @SerializedName("login")
    String userLogin;

    @SerializedName("id")
    int userID;

    @SerializedName("followers")
    int userFollowers;

    @SerializedName("chatColor")
    String userChatColor;

    @SerializedName("createdAt")
    Date createdAt;

    @SerializedName("roles")
    IVRUserRoles userRoles;

    @SerializedName("badges")
    ArrayList<IVRUserBadge> userBadges;

    @SerializedName("chatterCount")
    int chatterCount;

    @SerializedName("lastBroadcast")
    IVRUserStream lastBroadcast;

    public boolean isBanned()
    {
        return isBanned;
    }

    @Nullable
    public String getBanReason()
    {
        return banReason;
    }

    @NonNull
    public String getUserDisplayName()
    {
        return userDisplayName;
    }

    @NonNull
    public String getUserLogin()
    {
        return userLogin;
    }

    public int getUserID()
    {
        return userID;
    }

    public int getUserFollowers()
    {
        return userFollowers;
    }

    @Nullable
    public String getUserChatColor()
    {
        return userChatColor;
    }

    @NonNull
    public Date getCreatedAt()
    {
        return createdAt;
    }

    @NonNull
    public IVRUserRoles getUserRoles()
    {
        return userRoles;
    }

    @Nullable
    public ArrayList<IVRUserBadge> getUserBadges()
    {
        return userBadges;
    }

    public int getChatterCount()
    {
        return chatterCount;
    }

    @Nullable
    public IVRUserStream getLastBroadcast()
    {
        return lastBroadcast;
    }

    IVRUser()
    {
    }
}
