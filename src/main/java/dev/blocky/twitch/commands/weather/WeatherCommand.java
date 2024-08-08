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

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.maps.MapAdress;
import dev.blocky.api.entities.maps.MapProperty;
import dev.blocky.api.entities.maps.MapSearch;
import dev.blocky.api.entities.openmeteo.OpenMeteo;
import dev.blocky.api.entities.openmeteo.OpenMeteoCurrentWeather;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Location;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.WeatherUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class WeatherCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        Location location = SQLUtils.getLocation(eventUserIID);

        if (messageParts.length == 1 && location == null)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a location or set an anonymous location by sending me a whisper with the input 'setlocation <YOUR_LOCATION_HERE>'.");
            return;
        }

        double latitude = -1;
        double longitude = -1;

        String locationName = null;
        String cityName = null;
        String countryCode = null;

        if (messageParts.length == 1)
        {
            latitude = location.getLatitude();
            longitude = location.getLatitude();

            locationName = location.getLocationName();
            cityName = location.getCityName();
            countryCode = location.getCountryCode();
        }

        if (messageParts.length > 1)
        {
            String userLocation = removeElements(messageParts, 1);

            if (userLocation.isBlank())
            {
                sendChatMessage(channelID, "FeelsMan Please specify a location or set an anonymous location by sending me a whisper with the input 'setlocation <YOUR_LOCATION_HERE>'.");
                return;
            }

            MapSearch mapSearch = ServiceProvider.getSearchedMaps(userLocation);
            List<MapAdress> mapAdresses = mapSearch.getAddresses();

            if (mapAdresses.isEmpty())
            {
                sendChatMessage(channelID, STR."UNLUCKY No location called '\{userLocation}' found.");
                return;
            }

            MapAdress mapAdress = mapAdresses.getFirst();
            MapProperty mapProperty = mapAdress.getProperty();

            latitude = mapProperty.getLatitude();
            longitude = mapProperty.getLongitude();
            locationName = mapProperty.getFormatted();
            cityName = mapProperty.getCity();
            countryCode = mapProperty.getCountryCode();
        }

        String emoji = "\uD83C\uDFF4";

        if (countryCode != null)
        {
            String code = countryCode.toUpperCase();

            emoji = code.chars()
                    .map(ch -> ch - 0x41 + 0x1F1E6)
                    .mapToObj(Character::toString)
                    .collect(Collectors.joining());
        }

        OpenMeteo openMeteo = ServiceProvider.getOpenMeteoCurrentWeather(latitude, longitude);
        OpenMeteoCurrentWeather currentWeather = openMeteo.getCurrentWeather();

        boolean isDay = currentWeather.isDay();

        int humidity = currentWeather.getHumidity();
        int cloudCover = currentWeather.getCloudCover();
        int windDirection = currentWeather.getWindDirection();

        float temperature = currentWeather.getTemperature();
        float feelsLike = currentWeather.getFeelsLike();
        float rain = currentWeather.getRain();
        float snowfall = currentWeather.getSnowfall();
        float windSpeed = currentWeather.getWindSpeed();

        StringBuilder weatherBuilder = new StringBuilder();

        Map<String, Boolean> dayTimeEmojis = WeatherUtils.getDayTimeEmojis(isDay);
        Map<String, Boolean> temperatureEmojis = WeatherUtils.getTemperatureEmojis(temperature);
        Map<String, Boolean> feelsLikeEmojis = WeatherUtils.getTemperatureEmojis(feelsLike);
        Map<String, Boolean> cloudEmojis = WeatherUtils.getCloudEmojis(snowfall, rain, cloudCover);
        Map<String, Boolean> rainEmojis = WeatherUtils.getRainEmojis(rain);
        Map<String, Boolean> snowfallEmojis = WeatherUtils.getSnowfallEmojis(snowfall);

        weatherBuilder.append("Time of day:").append(" ");
        WeatherUtils.appendEmojis(weatherBuilder, dayTimeEmojis);

        weatherBuilder.append("Temperature:").append(STR." \{temperature} \u00B0C ");
        WeatherUtils.appendEmojis(weatherBuilder, temperatureEmojis);

        weatherBuilder.append("Feels like:").append(STR." \{feelsLike} \u00B0C ");
        WeatherUtils.appendEmojis(weatherBuilder, feelsLikeEmojis);

        weatherBuilder.append("Humidity:").append(STR." \{humidity}% \uD83C\uDF2B\uFE0F ");
        weatherBuilder.append("Cloud cover:").append(STR." \{cloudCover}% ");
        WeatherUtils.appendEmojis(weatherBuilder, cloudEmojis);

        WeatherUtils.appendIfNotZero(weatherBuilder, "Rain", rain, "mm", rainEmojis);
        WeatherUtils.appendIfNotZero(weatherBuilder, "Snowfall", snowfall, "cm", snowfallEmojis);

        weatherBuilder.append("Wind speed:").append(STR." \{windSpeed} km/h \uD83C\uDF43 ");
        weatherBuilder.append("Wind direction:").append(STR." \{windDirection}\u00B0 \uD83C\uDF8F");

        String weather = weatherBuilder.toString();

        if (location != null && messageParts.length == 1)
        {
            boolean hideLocation = location.hidesLocation();

            if (hideLocation)
            {
                sendChatMessage(channelID, STR."FeelsGoodMan Secret location Susge \{weather}");
                return;
            }
        }

        String userLocation = cityName;

        if (cityName == null)
        {
            userLocation = locationName;
        }

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, STR."FeelsGoodMan \{userLocation} \{emoji} \{weather}");
    }
}
