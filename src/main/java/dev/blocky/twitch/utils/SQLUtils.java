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

import dev.blocky.twitch.sql.SQLite;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SQLUtils
{
    @NonNull
    public static <T> HashSet<T> getAll(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            HashSet<T> set = new HashSet<>();

            while (result.next())
            {
                T object = result.getObject(columnLabel, clazz);
                set.add(object);
            }
            return set;
        }
    }

    @NonNull
    public static <T> HashMap<T, T> getAllMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<T> clazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            HashMap<T, T> map = new HashMap<>();

            while (result.next())
            {
                String firstColumnLabel = columnLabels.getFirst();
                String lastColumnLabel = columnLabels.getLast();

                T key = result.getObject(firstColumnLabel, clazz);
                T value = result.getObject(lastColumnLabel, clazz);

                map.put(key, value);
            }
            return map;
        }
    }

    @NonNull
    public static <T, E> ArrayList<Triple<T, T, E>> getTriples(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<T> clazz, @NonNull Class<E> extraClazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            ArrayList<Triple<T, T, E>> triples = new ArrayList<>();

            while (result.next())
            {
                String firstColumnLabel = columnLabels.getFirst();
                String middleColumnLabel = columnLabels.get(1);
                String lastColumnLabel = columnLabels.getLast();

                T key = result.getObject(firstColumnLabel, clazz);
                T value = result.getObject(middleColumnLabel, clazz);
                E extraValue = result.getObject(lastColumnLabel, extraClazz);

                Triple<T, T, E> triple = Triple.of(key, value, extraValue);
                triples.add(triple);
            }
            return triples;
        }
    }

    @NonNull
    public static <T> T get(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            return result.getObject(columnLabel, clazz);
        }
    }

    @NonNull
    public static HashSet<String> getChatLogins() throws SQLException
    {
        return getAll("SELECT * from chats", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<String> getAdminLogins() throws SQLException
    {
        return getAll("SELECT * from admins WHERE isOwner = FALSE", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<Integer> getAdminIDs() throws SQLException
    {
        return getAll("SELECT * from admins WHERE isOwner = FALSE", "userID", Integer.class);
    }

    @NonNull
    public static HashSet<String> getAdminCommands() throws SQLException
    {
        return getAll("SELECT * from adminCommands WHERE requiresOwner = FALSE", "command", String.class);
    }

    @NonNull
    public static HashSet<String> getOwnerLogins() throws SQLException
    {
        return getAll("SELECT * from admins WHERE isOwner = TRUE", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<Integer> getOwnerIDs() throws SQLException
    {
        return getAll("SELECT * from admins WHERE isOwner = TRUE", "userID", Integer.class);
    }

    @NonNull
    public static HashSet<String> getOwnerCommands() throws SQLException
    {
        return getAll("SELECT * from adminCommands WHERE requiresOwner = TRUE", "command", String.class);
    }

    @NonNull
    public static String getPrefix(int userID) throws SQLException
    {
        return get(STR."SELECT * from customPrefixes WHERE userID = \{userID}", "prefix", String.class);
    }

    @NonNull
    public static HashMap<String, String> getGlobalCommands() throws SQLException
    {
        return getAllMapped("SELECT * from globalCommands", List.of("name", "message"), String.class);
    }

    @NonNull
    public static HashSet<Integer> getSpotifyUserIDs() throws SQLException
    {
        return getAll("SELECT * from spotifyCredentials", "userID", Integer.class);
    }

    @NonNull
    public static String getSpotifyAccessToken(int userID) throws SQLException
    {
        return get(STR."SELECT * from spotifyCredentials WHERE userID = \{userID}", "accessToken", String.class);
    }

    @NonNull
    public static String getSpotifyRefreshToken(int userID) throws SQLException
    {
        return get(STR."SELECT * from spotifyCredentials WHERE userID = \{userID}", "refreshToken", String.class);
    }

    @NonNull
    public static String getSpotifyExpiresOn(int userID) throws SQLException
    {
        return get(STR."SELECT * from spotifyCredentials WHERE userID = \{userID}", "expiresOn", String.class);
    }

    @NonNull
    public static String getSevenTVAllowedUserIDs(int userID) throws SQLException
    {
        return get(STR."SELECT * from sevenTVUsers WHERE userID = \{userID}", "allowedUserIDs", String.class);
    }

    @NonNull
    public static List<Triple<String, String, Boolean>> getKeywords(int userID) throws SQLException
    {
        return getTriples(STR."SELECT * from customKeywords WHERE userID = \{userID}", List.of("name", "message", "exactMatch"), String.class, Boolean.class);
    }

    @NonNull
    public static HashSet<String> getEnabledEventNotificationChatLogins() throws SQLException
    {
        return getAll("SELECT * from eventNotifications WHERE enabled = TRUE", "userLogin", String.class);
    }

    public static boolean hasEnabledEventNotifications(int userID) throws SQLException
    {
        return get(STR."SELECT * from eventNotifications WHERE userID = \{userID}", "enabled", Boolean.class);
    }

    @NonNull
    public static HashSet<Integer> getWeatherLocationUserIDs() throws SQLException
    {
        return getAll("SELECT * from weatherLocations", "userID", Integer.class);
    }

    public static double getLatitude(int userID) throws SQLException
    {
        String latitude = get(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", "latitude", String.class);

        if (latitude == null)
        {
            return -1.0;
        }

        return Double.parseDouble(latitude);
    }

    public static double getLongitude(int userID) throws SQLException
    {
        String longitude = get(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", "longitude", String.class);

        if (longitude == null)
        {
            return -1.0;
        }

        return Double.parseDouble(longitude);
    }

    @NonNull
    public static String getLocationName(int userID) throws SQLException
    {
        return get(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", "locationName", String.class);
    }

    public static boolean hidesLocation(int userID) throws SQLException
    {
        return get(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", "hideLocation", Boolean.class);
    }

    @NonNull
    public static String removeApostrophe(@NonNull String prefix)
    {
        return StringUtils.remove(prefix, "'");
    }

    @NonNull
    public static String getActualPrefix(@NonNull String userID) throws SQLException
    {
        int userIID = Integer.parseInt(userID);
        String customPrefix = getPrefix(userIID);
        return customPrefix == null ? "kok!" : customPrefix;
    }

    public static void correctUserLogin(int userID, @NonNull String newUserLogin) throws SQLException
    {
        List<String> tables = List.of("chats", "admins", "eventNotifications");

        for (String table : tables)
        {
            try (ResultSet result = SQLite.onQuery(STR."SELECT * from \{table} WHERE userID = \{userID}"))
            {
                String userLogin = result.getString("userLogin");

                if (userLogin == null || userLogin.isBlank())
                {
                    continue;
                }

                if (userLogin.equals(newUserLogin))
                {
                    continue;
                }

                SQLite.onUpdate(STR."UPDATE \{table} SET userLogin = '\{newUserLogin}' WHERE userID = \{userID}");
            }
        }
    }
}
