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

import dev.blocky.api.ServiceProvider;
import dev.blocky.api.request.BlockyJarUserBody;
import dev.blocky.twitch.sql.SQLite;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    public static <U, T> HashMap<U, T> getAllMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<U> clazz, @NonNull Class<T> extraClazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            HashMap<U, T> map = new HashMap<>();

            while (result.next())
            {
                String firstColumnLabel = columnLabels.getFirst();
                String lastColumnLabel = columnLabels.getLast();

                U key = result.getObject(firstColumnLabel, clazz);
                T value = result.getObject(lastColumnLabel, extraClazz);

                map.put(key, value);
            }
            return map;
        }
    }

    @NonNull
    public static <T> HashMap<T, T> getAllMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<T> clazz) throws SQLException
    {
        return getAllMapped(sql, columnLabels, clazz, clazz);
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

    @Nullable
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
        return getAll("SELECT userLogin FROM chats", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<String> getAdminLogins() throws SQLException
    {
        return getAll("SELECT userLogin FROM admins", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<Integer> getAdminIDs() throws SQLException
    {
        return getAll("SELECT userID FROM admins", "userID", Integer.class);
    }

    public static int getAdminIDByLogin(@NonNull String userLogin) throws SQLException
    {
        return get(STR."SELECT userID FROM admins WHERE userLogin = '\{userLogin}'", "userID", Integer.class);
    }

    @NonNull
    public static HashSet<String> getAdminCommands() throws SQLException
    {
        return getAll("SELECT command FROM adminCommands WHERE requiresOwner = FALSE", "command", String.class);
    }

    @NonNull
    public static HashSet<String> getOwnerLogins() throws SQLException
    {
        return getAll("SELECT userLogin FROM admins WHERE isOwner = TRUE", "userLogin", String.class);
    }

    @NonNull
    public static HashSet<Integer> getOwnerIDs() throws SQLException
    {
        return getAll("SELECT userID FROM admins WHERE isOwner = TRUE", "userID", Integer.class);
    }

    public static int getOwnerIDByLogin(@NonNull String userLogin) throws SQLException
    {
        return get(STR."SELECT userID FROM admins WHERE isOwner = TRUE AND userLogin = '\{userLogin}'", "userID", Integer.class);
    }

    @NonNull
    public static HashSet<String> getOwnerCommands() throws SQLException
    {
        return getAll("SELECT command FROM adminCommands WHERE requiresOwner = TRUE", "command", String.class);
    }

    @NonNull
    public static String getPrefix(int userID) throws SQLException
    {
        String prefix = get(STR."SELECT prefix FROM customPrefixes WHERE userID = \{userID}", "prefix", String.class);
        return prefix == null ? "#" : prefix;
    }

    @NonNull
    public static HashMap<String, String> getGlobalCommands() throws SQLException
    {
        return getAllMapped("SELECT name, message FROM globalCommands", List.of("name", "message"), String.class);
    }

    @NonNull
    public static HashSet<Integer> getSpotifyUserIDs() throws SQLException
    {
        return getAll("SELECT userID FROM spotifyCredentials", "userID", Integer.class);
    }

    @NonNull
    public static String getSpotifyAccessToken(int userID) throws SQLException
    {
        return get(STR."SELECT accessToken FROM spotifyCredentials WHERE userID = \{userID}", "accessToken", String.class);
    }

    @NonNull
    public static String getSpotifyRefreshToken(int userID) throws SQLException
    {
        return get(STR."SELECT refreshToken FROM spotifyCredentials WHERE userID = \{userID}", "refreshToken", String.class);
    }

    @NonNull
    public static String getSpotifyExpiresOn(int userID) throws SQLException
    {
        return get(STR."SELECT expiresOn FROM spotifyCredentials WHERE userID = \{userID}", "expiresOn", String.class);
    }

    @NonNull
    public static String getSevenTVAllowedUserIDs(int userID) throws SQLException
    {
        return get(STR."SELECT allowedUserIDs FROM sevenTVUsers WHERE userID = \{userID}", "allowedUserIDs", String.class);
    }

    @NonNull
    public static List<Triple<String, String, Boolean>> getKeywords(int userID) throws SQLException
    {
        return getTriples(STR."SELECT name, message, exactMatch FROM customKeywords WHERE userID = \{userID}", List.of("name", "message", "exactMatch"), String.class, Boolean.class);
    }

    @NonNull
    public static HashSet<String> getEnabledEventNotificationChatLogins() throws SQLException
    {
        return getAll("SELECT userLogin FROM eventNotifications WHERE enabled = TRUE", "userLogin", String.class);
    }

    public static boolean hasEnabledEventNotifications(int userID) throws SQLException
    {
        return get(STR."SELECT enabled FROM eventNotifications WHERE userID = \{userID}", "enabled", Boolean.class);
    }

    @NonNull
    public static HashSet<Integer> getWeatherLocationUserIDs() throws SQLException
    {
        return getAll("SELECT userID FROM weatherLocations", "userID", Integer.class);
    }

    public static double getLatitude(int userID) throws SQLException
    {
        String latitude = get(STR."SELECT latitude FROM weatherLocations WHERE userID = \{userID}", "latitude", String.class);

        if (latitude == null)
        {
            return -1.0;
        }

        return Double.parseDouble(latitude);
    }

    public static double getLongitude(int userID) throws SQLException
    {
        String longitude = get(STR."SELECT longitude FROM weatherLocations WHERE userID = \{userID}", "longitude", String.class);

        if (longitude == null)
        {
            return -1.0;
        }

        return Double.parseDouble(longitude);
    }

    @NonNull
    public static String getLocationName(int userID) throws SQLException
    {
        return get(STR."SELECT locationName FROM weatherLocations WHERE userID = \{userID}", "locationName", String.class);
    }

    @Nullable
    public static String getCityName(int userID) throws SQLException
    {
        return get(STR."SELECT cityName FROM weatherLocations WHERE userID = \{userID}", "cityName", String.class);
    }

    @Nullable
    public static String getCountryCode(int userID) throws SQLException
    {
        return get(STR."SELECT countryCode FROM weatherLocations WHERE userID = \{userID}", "countryCode", String.class);
    }

    public static boolean hidesLocation(int userID) throws SQLException
    {
        return get(STR."SELECT hideLocation FROM weatherLocations WHERE userID = \{userID}", "hideLocation", Boolean.class);
    }

    @NonNull
    public static HashSet<Integer> getTicTacToeGames() throws SQLException
    {
        return getAll("SELECT userID FROM tictactoe", "userID", Integer.class);
    }

    @NonNull
    public static List<Integer> getTicTacToePlayerIDs(int channelID) throws SQLException
    {
        String playerIDsRaw = get(STR."SELECT playerIDs FROM tictactoe WHERE userID = \{channelID}", "playerIDs", String.class);
        int playerIDsLength = playerIDsRaw.length();

        String playerIDs = playerIDsRaw.substring(1, playerIDsLength - 1);
        String[] playerIDParts = playerIDs.split(", ");

        return Arrays.stream(playerIDParts)
                .mapToInt(Integer::parseInt)
                .boxed()
                .toList();
    }

    @NonNull
    public static int[] getTicTacToeBoard(int channelID) throws SQLException
    {
        String boardRaw = get(STR."SELECT board FROM tictactoe WHERE userID = \{channelID}", "board", String.class);
        int boardLength = boardRaw.length();

        String board = boardRaw.substring(1, boardLength - 1);
        String[] boardParts = board.split(", ");

        return Arrays.stream(boardParts)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public static int getTicTacToeNexUserID(int channelID) throws SQLException
    {
        return get(STR."SELECT nextUserID FROM tictactoe WHERE userID = \{channelID}", "nextUserID", Integer.class);
    }

    public static int getTicTacToeRound(int channelID) throws SQLException
    {
        return get(STR."SELECT round FROM tictactoe WHERE userID = \{channelID}", "round", Integer.class);
    }

    @NonNull
    public static String getTicTacToeStartedAt(int channelID) throws SQLException
    {
        return get(STR."SELECT startedAt FROM tictactoe WHERE userID = \{channelID}", "startedAt", String.class);
    }

    @NonNull
    public static Map<Integer, String> getTicTacToeStartTimes() throws SQLException
    {
        return getAllMapped("SELECT userID, startedAt FROM tictactoe", List.of("userID", "startedAt"), Integer.class, String.class);
    }

    @NonNull
    public static String removeApostrophe(@NonNull String prefix)
    {
        return StringUtils.remove(prefix, "'");
    }

    public static void correctUserLogin(int userID, @NonNull String newUserLogin) throws SQLException, IOException
    {
        List<String> tables = List.of("chats", "admins", "eventNotifications");

        for (String table : tables)
        {
            try (ResultSet result = SQLite.onQuery(STR."SELECT userLogin FROM \{table} WHERE userID = \{userID}"))
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

                BlockyJarUserBody body = new BlockyJarUserBody(userID, newUserLogin);
                ServiceProvider.patchUser(userID, body);
            }
        }
    }
}
