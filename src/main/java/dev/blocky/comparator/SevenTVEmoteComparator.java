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
package dev.blocky.comparator;

import dev.blocky.api.entities.seventv.SevenTVEmote;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Comparator;

public class SevenTVEmoteComparator implements Comparator<SevenTVEmote>
{
    private final String emoteName;

    public SevenTVEmoteComparator(@NonNull String emoteName)
    {
        this.emoteName = emoteName;
    }

    @Override
    public int compare(@NonNull SevenTVEmote firstSevenTVEmote, @NonNull SevenTVEmote secondSevenTVEmote)
    {
        String firstEmoteName = firstSevenTVEmote.getName();
        String secondEmoteName = secondSevenTVEmote.getName();

        if (firstEmoteName.startsWith(emoteName))
        {
            return secondEmoteName.startsWith(emoteName)? firstEmoteName.compareTo(secondEmoteName): -1;
        }
        return secondEmoteName.startsWith(emoteName)? 1: firstEmoteName.compareTo(secondEmoteName);
    }
}
