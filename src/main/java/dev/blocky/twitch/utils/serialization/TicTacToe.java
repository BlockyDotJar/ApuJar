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
 *s
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.twitch.utils.serialization;

import com.google.gson.annotations.SerializedName;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TicTacToe
{
    @SerializedName("playerIDs")
    String playerIDs;

    @SerializedName("board")
    String board;

    @SerializedName("nextUserID")
    int nextUserID;

    @SerializedName("round")
    int round;

    @SerializedName("startedAt")
    String startedAt;

    @NonNull
    public List<Integer> getPlayerIDs()
    {
        String[] playerIDParts = playerIDs.split(",");

        return Arrays.stream(playerIDParts)
                .mapToInt(Integer::parseInt)
                .boxed()
                .toList();
    }

    public int[] getBoard()
    {
        String[] boardParts = board.split(",");

        return Arrays.stream(boardParts)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public int getNextUserID()
    {
        return nextUserID;
    }

    public int getRound()
    {
        return round;
    }

    @NonNull
    public LocalDateTime getStartedAt()
    {
        return LocalDateTime.parse(startedAt);
    }

    TicTacToe()
    {
    }
}
