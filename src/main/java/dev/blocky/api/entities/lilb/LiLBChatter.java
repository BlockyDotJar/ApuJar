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
package dev.blocky.api.entities.lilb;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class LiLBChatter
{
    @SerializedName("moderators")
    List<String> moderators;

    @SerializedName("vips")
    List<String> vips;

    @SerializedName("viewers")
    List<String> viewers;

    @NonNull
    public List<String> getChatters()
    {
        List<String> nonViewer = CollectionUtils.collate(moderators, viewers);
        return CollectionUtils.collate(nonViewer, viewers);
    }

    public int getModeratorCount()
    {
        return moderators.size();
    }

    public int getVIPCount()
    {
        return vips.size();
    }

    public int getViewerCount()
    {
        return viewers.size();
    }
}
