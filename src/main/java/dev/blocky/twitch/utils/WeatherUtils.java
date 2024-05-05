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
package dev.blocky.twitch.utils;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;

public class WeatherUtils
{
    @NonNull
    public static Map<String, Boolean> getDayTimeEmojis(boolean isDay)
    {
        return Map.of
                (
                        "\u2600\uFE0F", isDay,
                        "\uD83C\uDF03", !isDay
                );
    }

    @NonNull
    public static Map<String, Boolean> getTemperatureEmojis(float temperature)
    {
        return Map.of
                (
                        "\uD83D\uDD25", temperature >= 30,
                        "\uD83E\uDD75", temperature >= 15,
                        "\uD83C\uDF75", temperature < 15 && temperature > 0,
                        "\uD83E\uDD76", temperature <= 0
                );
    }

    @NonNull
    public static Map<String, Boolean> getCloudEmojis(float snowfall, float rain, int cloudCover)
    {
        return Map.of
                (
                        "\uD83C\uDF28\uFE0F", snowfall != 0.0,
                        "\uD83C\uDF27\uFE0F", rain != 0.0 && cloudCover >= 90,
                        "\uD83C\uDF26\uFE0F", rain != 0.0,
                        "\u2601\uFE0F", cloudCover == 100,
                        "\uD83C\uDF25\uFE0F", cloudCover >= 75,
                        "\u26C5", cloudCover >= 50,
                        "\uD83C\uDF24\uFE0F", cloudCover >= 25
                );
    }

    @NonNull
    public static Map<String, Boolean> getRainEmojis(float rain)
    {
        return Map.of
                (
                        "\uD83C\uDF0A", rain >= 50,
                        "\u2614", rain > 2.5,
                        "\uD83D\uDCA7", rain <= 2.5
                );
    }

    @NonNull
    public static Map<String, Boolean> getSnowfallEmojis(float snowfall)
    {
        return Map.of
                (
                        "\uD83E\uDDCA", snowfall >= 50,
                        "\u2603\uFE0F", snowfall >= 10,
                        "\u2744\uFE0F", snowfall >= 1
                );
    }

    public static void appendEmojis(@NonNull StringBuilder builder, @NonNull Map<String, Boolean> emojis)
    {
        Set<Entry<String, Boolean>> conditionEntries = emojis.entrySet();

        for (Entry<String, Boolean> conditions : conditionEntries)
        {
            String emoji = conditions.getKey();
            boolean condition = conditions.getValue();

            if (condition)
            {
                builder.append(emoji).append(" ");
                break;
            }
        }
    }

    public static void appendIfNotZero(@NonNull StringBuilder builder, @NonNull String weatherStat, float weatherStatValue, @NonNull String weatherStatUnit, @NonNull Map<String, Boolean> emojis)
    {
        if (weatherStatValue != 0.00)
        {
            builder.append(STR."\{weatherStat}:").append(STR." \{weatherStatValue} \{weatherStatUnit} ");

            if (emojis != null)
            {
                appendEmojis(builder, emojis);
            }
        }
    }
}
