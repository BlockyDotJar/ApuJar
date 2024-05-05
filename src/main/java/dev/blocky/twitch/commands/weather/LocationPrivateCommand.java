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
import dev.blocky.twitch.utils.SQLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.stream.Collectors;

import static dev.blocky.twitch.utils.TwitchUtils.sendPrivateMessage;

public class LocationPrivateCommand implements IPrivateCommand
{
    @Override
    public void onPrivateCommand(@NotNull PrivateMessageEvent event, @NotNull TwitchHelix helix, @NotNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        HashSet<Integer> weatherLocationUserIIDs = SQLUtils.getWeatherLocationUserIDs();

        if (!weatherLocationUserIIDs.contains(eventUserIID))
        {
            sendPrivateMessage(helix, eventUserID, "4Head No location found in the database for your user id.");
            return;
        }

        String locationName = SQLUtils.getLocationName(eventUserIID);
        String countryCode = SQLUtils.getCountryCode(eventUserIID);

        String emoji = "\uD83C\uDFF4";

        if (countryCode != null)
        {
            String code = countryCode.toUpperCase();

            emoji = code.chars()
                    .map(ch -> ch - 0x41 + 0x1F1E6)
                    .mapToObj(Character::toString)
                    .collect(Collectors.joining());
        }

        sendPrivateMessage(helix, eventUserID, STR.":D Your location is currently set to '\{locationName}' \{emoji}.");
    }
}
