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
package dev.blocky.api.entities.modscanner;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModScanner
{
    @SerializedName("moderating")
    public final ArrayList<ModScannerUser> moderating = new ArrayList<>();

    @SerializedName("viping")
    public final ArrayList<ModScannerUser> viping = new ArrayList<>();

    @SerializedName("founding")
    public final ArrayList<ModScannerUser> founding = new ArrayList<>();

    @SerializedName("moderators")
    public final ArrayList<ModScannerUser> moderators = new ArrayList<>();

    @SerializedName("vips")
    public final ArrayList<ModScannerUser> vips = new ArrayList<>();

    @SerializedName("founders")
    public final ArrayList<ModScannerUser> founders = new ArrayList<>();

    public ArrayList<ModScannerUser> getChannelModerators()
    {
        return moderators;
    }

    public ArrayList<ModScannerUser> getChannelVIPs()
    {
        return vips;
    }

    public ArrayList<ModScannerUser> getChannelFounders()
    {
        return founders;
    }

    public ArrayList<ModScannerUser> getUserModerators()
    {
        return moderating;
    }

    public ArrayList<ModScannerUser> getUserVIPs()
    {
        return viping;
    }

    public ArrayList<ModScannerUser> getUserFounders()
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
