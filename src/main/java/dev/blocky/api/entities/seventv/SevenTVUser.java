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

import java.util.ArrayList;

public class SevenTVUser
{
    @SerializedName("id")
    String id;

    @SerializedName("username")
    String username;

    @SerializedName("display_name")
    String display_name;

    @SerializedName("connections")
    ArrayList<SevenTVUserConnection> connections;

    @SerializedName("editors")
    ArrayList<SevenTVUser> editors;

    @NonNull
    public String getID()
    {
        return id;
    }

    @Nullable
    public String getUsername()
    {
        return username;
    }

    @Nullable
    public String getDisplayName()
    {
        return display_name;
    }

    @Nullable
    public ArrayList<SevenTVUserConnection> getConnections()
    {
        return connections;
    }

    @Nullable
    public ArrayList<SevenTVUser> getEditors()
    {
        return editors;
    }

    SevenTVUser()
    {
    }
}
