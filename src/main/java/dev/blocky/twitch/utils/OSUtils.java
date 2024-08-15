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
import org.apache.commons.lang3.SystemUtils;

import static java.lang.StringTemplate.STR;

public class OSUtils
{
    @NonNull
    public static String getFilePath(@NonNull String filePath)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return filePath == null ? "src/main/resources/" : STR."src/main/resources/\{filePath}";
        }

        return filePath == null ? "/usr/local/apujar/" : STR."/usr/local/apujar/\{filePath}";
    }
}
