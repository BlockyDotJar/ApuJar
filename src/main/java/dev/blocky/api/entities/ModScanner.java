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

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModScanner
{
    @JsonProperty("moderating")
    public final ArrayList<JsonNode> moderating = new ArrayList<>();

    @JsonProperty("viping")
    public final ArrayList<JsonNode> viping = new ArrayList<>();

    @JsonProperty("founding")
    public final ArrayList<JsonNode> founding = new ArrayList<>();

    @JsonProperty("moderators")
    public final ArrayList<JsonNode> moderators = new ArrayList<>();

    @JsonProperty("vips")
    public final ArrayList<JsonNode> vips = new ArrayList<>();

    @JsonProperty("founders")
    public final ArrayList<JsonNode> founders = new ArrayList<>();

    public ArrayList<JsonNode> getChannelModerators()
    {
        return moderators;
    }

    public ArrayList<JsonNode> getChannelVIPs()
    {
        return vips;
    }

    public ArrayList<JsonNode> getChannelFounders()
    {
        return founders;
    }

    public ArrayList<JsonNode> getUserModerators()
    {
        return moderating;
    }

    public ArrayList<JsonNode> getUserVIPs()
    {
        return viping;
    }

    public ArrayList<JsonNode> getUserFounders()
    {
        return founding;
    }

    public int getUserModeratorCount()
    {
        return moderating.size();
    }

    public int getUserVIPCount()
    {
        return viping.size();
    }

    public int getUserFounderCount()
    {
        return founding.size();
    }

    public int getChannelModeratorCount()
    {
        return moderators.size();
    }

    public int getChannelVIPCount()
    {
        return vips.size();
    }

    public int getChannelFounderCount()
    {
        return founders.size();
    }

    ModScanner()
    {
    }
}
