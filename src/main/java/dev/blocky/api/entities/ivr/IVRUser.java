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
    public boolean banned;

    @SerializedName("banReason")
    public String banReason;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("login")
    public String login;

    @SerializedName("id")
    public String id;

    @SerializedName("followers")
    public int followers;

    @SerializedName("chatColor")
    public String chatColor;

    @SerializedName("createdAt")
    public Date createdAt;

    @SerializedName("roles")
    public IVRUserRoles roles;

    @SerializedName("badges")
    public ArrayList<IVRUserBadge> badges;

    @SerializedName("chatterCount")
    public int chatterCount;

    @SerializedName("lastBroadcast")
    public IVRUserStream lastBroadcast;

    public boolean isBanned()
    {
        return banned;
    }

    @Nullable
    public String getBanReason()
    {
        return banReason;
    }

    @NonNull
    public String getDisplayName()
    {
        return displayName;
    }

    @NonNull
    public String getLogin()
    {
        return login;
    }

    @NonNull
    public String getId()
    {
        return id;
    }

    public int getFollowers()
    {
        return followers;
    }

    @Nullable
    public String getChatColor()
    {
        return chatColor;
    }

    @NonNull
    public Date getCreatedAt()
    {
        return createdAt;
    }

    @NonNull
    public IVRUserRoles getRoles()
    {
        return roles;
    }

    @Nullable
    public ArrayList<IVRUserBadge> getBadges()
    {
        return badges;
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
