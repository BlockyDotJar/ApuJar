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
package dev.blocky.twitch.commands.spotify;

import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.helix.TwitchHelix;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;

import static dev.blocky.twitch.utils.TwitchUtils.sendPrivateMessage;

public class DeleteSpotifyUserPrivateCommand implements IPrivateCommand
{
    @Override
    public void onPrivateCommand(@NonNull PrivateMessageEvent event, @NonNull TwitchHelix helix, @NonNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        HashSet<Integer> spotifyUserIIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIIDs.contains(eventUserIID))
        {
            sendPrivateMessage(helix, eventUserID, "4Head No Spotify credentials found in the database with your id.");
            return;
        }

        SQLite.onUpdate(STR."DELETE FROM spotifyCredentials WHERE userID = \{eventUserIID}");

        sendPrivateMessage(helix, eventUserID, "SeemsGood Successfully removed Spotify credential from the database.");
    }
}
