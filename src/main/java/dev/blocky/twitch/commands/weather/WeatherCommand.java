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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.maps.MapAdress;
import dev.blocky.api.entities.maps.MapProperty;
import dev.blocky.api.entities.maps.MapSearch;
import dev.blocky.api.entities.openmeteo.OpenMeteo;
import dev.blocky.api.entities.openmeteo.OpenMeteoCurrentWeather;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.WeatherUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.getActualChannel;
import static dev.blocky.twitch.utils.TwitchUtils.removeElements;

public class WeatherCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        HashSet<Integer> weatherLocationUserIIDs = SQLUtils.getWeatherLocationUserIDs();

        if (messageParts.length == 1 && !weatherLocationUserIIDs.contains(eventUserIID))
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a location or set an anonymous location by sending me a whisper with the input 'setlocation <YOUR_LOCATION_HERE>'.");
            return;
        }

        double latitude = SQLUtils.getLatitude(eventUserIID);
        double longitude = SQLUtils.getLatitude(eventUserIID);

        String locationName = SQLUtils.getLocationName(eventUserIID);
        String cityName = SQLUtils.getCityName(eventUserIID);
        String countryCode = SQLUtils.getCountryCode(eventUserIID);

        if (messageParts.length > 1)
        {
            String location = removeElements(messageParts, 1);

            if (location.isBlank())
            {
                chat.sendMessage(channelName, "FeelsMan Please specify a location or set an anonymous location by sending me a whisper with the input 'setlocation <YOUR_LOCATION_HERE>'.");
                return;
            }

            MapSearch mapSearch = ServiceProvider.getSearchedMaps(location);
            List<MapAdress> mapAdresses = mapSearch.getAddresses();

            if (mapAdresses.isEmpty())
            {
                chat.sendMessage(channelName, STR."UNLUCKY No location called '\{location}' found.");
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

        if (weatherLocationUserIIDs.contains(eventUserIID) && messageParts.length == 1)
        {
            boolean hideLocation = SQLUtils.hidesLocation(eventUserIID);

            if (hideLocation)
            {
                chat.sendMessage(channelName, STR."FeelsGoodMan Secret location Susge \{weather}");
                return;
            }
        }

        String location = cityName;

        if (cityName == null)
        {
            location = locationName;
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, STR."FeelsGoodMan \{location} \{emoji} \{weather}");
    }
}
