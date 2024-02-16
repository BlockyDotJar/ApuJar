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
package dev.blocky.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IVRFI
{
    @JsonProperty("banned")
    public boolean banned;

    @JsonProperty("banReason")
    public String banReason;

    @JsonProperty("displayName")
    public String displayName;

    @JsonProperty("login")
    public String login;

    @JsonProperty("id")
    public String id;

    @JsonProperty("followers")
    public int followers;

    @JsonProperty("chatColor")
    public String chatColor;

    @JsonProperty("createdAt")
    public String createdAt;

    @JsonProperty("roles")
    public JsonNode roles;

    @JsonProperty("chatterCount")
    public int chatterCount;

    @JsonProperty("badges")
    public ArrayList<JsonNode> badges;

    @JsonProperty("lastBroadcast")
    public JsonNode lastBroadcast;

    @JsonProperty("followedAt")
    public String followedAt;

    @JsonProperty("streak")
    public JsonNode streak;

    @JsonProperty("cumulative")
    public JsonNode cumulative;

    @JsonProperty("meta")
    public JsonNode meta;

    @JsonProperty("mods")
    public ArrayList<JsonNode> mods;

    @JsonProperty("vips")
    public ArrayList<JsonNode> vips;

    @JsonProperty("founders")
    public ArrayList<JsonNode> founders;

    public boolean isTwitchGlobalBanned()
    {
        return banned;
    }

    @Nullable
    public String getTwitchGlobalBanReason()
    {
        return banReason;
    }

    @Nullable
    public String getDisplayName()
    {
        return displayName;
    }

    @Nullable
    public String getLogin()
    {
        return login;
    }

    @Nullable
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

    @Nullable
    public String getCreatedAt()
    {
        return createdAt;
    }

    @NonNull
    public JsonNode getRoles()
    {
        return roles;
    }

    public int getChatterCount()
    {
        return chatterCount;
    }

    @Nullable
    public ArrayList<JsonNode> getBadges()
    {
        return badges;
    }

    @Nullable
    public JsonNode getLastBroadcast()
    {
        return lastBroadcast;
    }

    @Nullable
    public String getFollowedAt()
    {
        return followedAt;
    }

    @Nullable
    public JsonNode getStreak()
    {
        return streak;
    }

    @Nullable
    public JsonNode getCumulative()
    {
        return cumulative;
    }

    @Nullable
    public JsonNode getMeta()
    {
        return meta;
    }

    @Nullable
    public ArrayList<JsonNode> getMods()
    {
        return mods;
    }

    @Nullable
    public ArrayList<JsonNode> getVips()
    {
        return vips;
    }

    @Nullable
    public ArrayList<JsonNode> getFounders()
    {
        return founders;
    }

    IVRFI()
    {
    }
}
