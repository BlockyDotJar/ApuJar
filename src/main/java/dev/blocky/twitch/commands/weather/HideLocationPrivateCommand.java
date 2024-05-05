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
import com.github.twitch4j.helix.TwitchHelix;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

import static dev.blocky.twitch.utils.TwitchUtils.sendPrivateMessage;

public class HideLocationPrivateCommand implements IPrivateCommand
{
    @Override
    public void onPrivateCommand(@NotNull PrivateMessageEvent event, @NotNull TwitchHelix helix, @NotNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendPrivateMessage(helix, eventUserID, ";) Please specify a boolean. (Either true or false)");
            return;
        }

        String hideValue = messageParts[2];

        if (!hideValue.matches("^true|false$"))
        {
            sendPrivateMessage(helix, eventUserID, ":O Invalid value specified. (Choose between true or false)");
            return;
        }

        HashSet<Integer> weatherLocationUserIIDs = SQLUtils.getWeatherLocationUserIDs();

        if (!weatherLocationUserIIDs.contains(eventUserIID))
        {
            sendPrivateMessage(helix, eventUserID, "4Head No location found in the database for your user id.");
            return;
        }

        boolean hideLocation = Boolean.parseBoolean(hideValue);
        boolean hidesLocation = SQLUtils.hidesLocation(eventUserIID);

        if (hideLocation == hidesLocation)
        {
            sendPrivateMessage(helix, eventUserID, "4Head The new value does exactly match with the old one.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE weatherLocations SET hideLocation = \{hideLocation} WHERE userID = \{eventUserIID}");

        sendPrivateMessage(helix, eventUserID, STR.":O Successfully updated your location visibility to '\{hideLocation}'.");
    }
}
