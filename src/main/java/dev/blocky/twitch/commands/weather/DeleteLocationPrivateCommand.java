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
package dev.blocky.twitch.commands.weather;

import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.serialization.Location;
import org.jetbrains.annotations.NotNull;

import static dev.blocky.twitch.utils.TwitchUtils.sendWhisper;

public class DeleteLocationPrivateCommand implements IPrivateCommand
{
    @Override
    public boolean onPrivateCommand(@NotNull PrivateMessageEvent event, @NotNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        Location location = SQLUtils.getLocation(eventUserIID);

        if (location == null)
        {
            sendWhisper(eventUserID, "4Head No location found in the database for your user id.");
            return false;
        }

        SQLite.onUpdate(STR."DELETE FROM weatherLocations WHERE userID = \{eventUserIID}");

        sendWhisper(eventUserID, ":O Successfully deleted your location from our database :D");
        return true;
    }
}
