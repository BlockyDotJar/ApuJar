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
import com.neovisionaries.i18n.CountryCode;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.maps.MapSearch;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.blocky.twitch.utils.TwitchUtils.removeElements;
import static dev.blocky.twitch.utils.TwitchUtils.sendPrivateMessage;

public class SetLocationPrivateCommand implements IPrivateCommand
{
    @Override
    public void onPrivateCommand(@NotNull PrivateMessageEvent event, @NotNull TwitchHelix helix, @NotNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendPrivateMessage(helix, eventUserID, ";) Please specify a location.");
            return;
        }

        String location = removeElements(messageParts, 1);

        List<MapSearch> mapSearches = ServiceProvider.getSearchedMaps(location);

        if (mapSearches.isEmpty())
        {
            sendPrivateMessage(helix, eventUserID, STR.":/ No location called '\{location}' found.");
            return;
        }

        MapSearch mapSearch = mapSearches.getFirst();
        double latitude = mapSearch.getLatitude();
        double longitude = mapSearch.getLongitude();
        String locationName = mapSearch.getLocationName();

        String[] locationPartsRaw = locationName.split(",");
        List<String> locationParts = Arrays.stream(locationPartsRaw)
                .map(String::strip)
                .toList();

        String country = locationParts.getLast();

        List<CountryCode> countryCodes = CountryCode.findByName(country);

        String emoji = "\uD83C\uDFF4";

        if (!countryCodes.isEmpty())
        {
            CountryCode countryCode = countryCodes.getFirst();
            String code = countryCode.name();

            emoji = code.chars()
                    .map(ch -> ch - 0x41 + 0x1F1E6)
                    .mapToObj(Character::toString)
                    .collect(Collectors.joining());
        }

        double lat = SQLUtils.getLatitude(eventUserIID);
        double lon = SQLUtils.getLongitude(eventUserIID);

        if (lat == latitude && lon == longitude)
        {
            sendPrivateMessage(helix, eventUserID, STR."4Head The new location '\{locationName}' does exactly match with the old one.");
            return;
        }

        if (lat == -1.0 && lon == -1.0)
        {
            SQLite.onUpdate(STR."INSERT INTO weatherLocations(userID, latitude, longitude, locationName, hideLocation) VALUES(\{eventUserIID}, \{latitude}, \{longitude}, '\{locationName}', TRUE)");

            sendPrivateMessage(helix, eventUserID, STR.":) Successfully added '\{locationName}' \{emoji} as your location.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE weatherLocations SET latitude = \{latitude}, longitude = \{longitude}, locationName = '\{locationName}' WHERE userID = \{eventUserIID}");

        sendPrivateMessage(helix, eventUserID, STR.":O Successfully updated your location to '\{locationName}' \{emoji} . You can change the visibility of your location with 'hidelocation'. (Your location doesn't get revealed for other users, as long as you don't change the visibility with this command)");
    }
}
