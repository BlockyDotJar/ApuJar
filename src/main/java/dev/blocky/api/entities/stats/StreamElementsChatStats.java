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
package dev.blocky.api.entities.stats;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public class StreamElementsChatStats
{
    @SerializedName("totalMessages")
    int totalMessageCount;

    @SerializedName("chatters")
    List<StreamElementsChatter> chatters;

    @SerializedName("twitchEmotes")
    List<StreamElementsEmote> twitchEmotes;

    @SerializedName("bttvEmotes")
    List<StreamElementsEmote> bttvEmotes;

    @SerializedName("ffzEmotes")
    List<StreamElementsEmote> ffzEmotes;

    @SerializedName("sevenTVEmotes")
    List<StreamElementsEmote> sevenTVEmotes;

    @SerializedName("uniqueChatters")
    int uniqueChatterCount;

    public int getTotalMessageCount()
    {
        return totalMessageCount;
    }

    @NonNull
    public List<StreamElementsChatter> getChatters()
    {
        return chatters;
    }

    @NonNull
    public List<StreamElementsEmote> getTwitchEmotes()
    {
        return twitchEmotes;
    }

    @NonNull
    public List<StreamElementsEmote> getBTTVEmotes()
    {
        return bttvEmotes;
    }

    @NonNull
    public List<StreamElementsEmote> getFFZEmotes()
    {
        return ffzEmotes;
    }

    @NonNull
    public List<StreamElementsEmote> getSevenTVEmotes()
    {
        return sevenTVEmotes;
    }

    public int getUniqueChatterCount()
    {
        return uniqueChatterCount;
    }

    StreamElementsChatStats()
    {
    }
}
