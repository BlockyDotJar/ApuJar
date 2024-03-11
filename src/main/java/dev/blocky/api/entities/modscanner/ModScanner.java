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
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;

public class ModScanner
{
    @SerializedName("moderating")
    ArrayList<ModScannerUser> moderating;

    @SerializedName("viping")
    ArrayList<ModScannerUser> viping;

    @SerializedName("founding")
    ArrayList<ModScannerUser> founding;

    @SerializedName("moderators")
    ArrayList<ModScannerUser> moderators;

    @SerializedName("vips")
    ArrayList<ModScannerUser> vips;

    @SerializedName("founders")
    ArrayList<ModScannerUser> founders;

    @Nullable
    public ArrayList<ModScannerUser> getChannelModerators()
    {
        return moderators;
    }

    @Nullable
    public ArrayList<ModScannerUser> getChannelVIPs()
    {
        return vips;
    }

    @Nullable
    public ArrayList<ModScannerUser> getChannelFounders()
    {
        return founders;
    }

    @Nullable
    public ArrayList<ModScannerUser> getUserModerators()
    {
        return moderating;
    }

    @Nullable
    public ArrayList<ModScannerUser> getUserVIPs()
    {
        return viping;
    }

    @Nullable
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
