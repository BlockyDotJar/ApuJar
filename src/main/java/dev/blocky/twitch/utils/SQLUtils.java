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

import com.google.gson.Gson;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.request.BlockyJarUserBody;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.serialization.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

public class SQLUtils
{
    @Nullable
    public static <T> T get(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            return result.getObject(columnLabel, clazz);
        }
    }

    @NonNull
    public static <T> Set<T> getAll(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            Set<T> set = new HashSet<>();

            while (result.next())
            {
                T object = result.getObject(columnLabel, clazz);
                set.add(object);
            }
            return set;
        }
    }

    @NonNull
    public static Map<String, Object> getMapped(@NonNull String sql, @NonNull Map<String, Class<?>> columnLabels) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            Map<String, Object> map = new HashMap<>();
            List<Boolean> nils = new ArrayList<>();

            for (String columnLabel : columnLabels.keySet())
            {
                Class<?> clazz = columnLabels.get(columnLabel);
                Object value = switch (clazz.getSimpleName())
                {
                    case "String" -> result.getString(columnLabel);
                    case "Integer" -> result.getInt(columnLabel);
                    case "Boolean" -> result.getBoolean(columnLabel);
                    case "Double" -> result.getDouble(columnLabel);
                    default -> null;
                };

                boolean wasNull = result.wasNull();

                nils.add(wasNull);
                map.put(columnLabel, value);
            }

            boolean allNil = nils.stream().allMatch(nil -> nil);

            if (allNil)
            {
                return null;
            }

            return map;
        }
    }

    @NonNull
    public static List<Map<String, Object>> getAllMapped(@NonNull String sql, @NonNull Map<String, Class<?>> columnLabels) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            List<Map<String, Object>> results = new ArrayList<>();

            while (result.next())
            {
                Map<String, Object> map = new HashMap<>();

                for (String columnLabel : columnLabels.keySet())
                {
                    Class<?> clazz = columnLabels.get(columnLabel);
                    Object value = result.getObject(columnLabel, clazz);

                    map.put(columnLabel, value);
                }

                results.add(map);
            }

            return results;
        }
    }

    @NonNull
    public static <U, T> BidiMap<U, T> getAllMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<U> clazz, @NonNull Class<T> extraClazz) throws SQLException
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            BidiMap<U, T> map = new DualHashBidiMap<>();

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
    public static <T> BidiMap<T, T> getAllMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<T> clazz) throws SQLException
    {
        return getAllMapped(sql, columnLabels, clazz, clazz);
    }

    @NonNull
    public static <T> T serializeResult(@NonNull Class<T> clazz, @NonNull Map<String, Object> results)
    {
        Gson gson = new Gson();
        String jsonToSerialize = gson.toJson(results);
        return gson.fromJson(jsonToSerialize, clazz);
    }

    @NonNull
    public static Set<Chat> getChats() throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "userID", Integer.class,
                        "userLogin", String.class,
                        "eventsEnabled", Boolean.class
                );

        List<Map<String, Object>> results = getAllMapped("SELECT * FROM chats", columnLabels);

        Set<Chat> chats = new HashSet<>();

        for (Map<String, Object> result : results)
        {
            Chat keyword = serializeResult(Chat.class, result);
            chats.add(keyword);
        }

        return chats;
    }

    @Nullable
    public static Chat getChat(int userID) throws SQLException
    {
        Set<Chat> chats = getChats();

        Optional<Chat> chat = chats.stream()
                .filter(ch ->
                {
                    int chatID = ch.getUserID();
                    return chatID == userID;
                })
                .findFirst();

        return chat.orElse(null);
    }

    @NonNull
    public static BidiMap<Integer, String> getAdmins() throws SQLException
    {
        return getAllMapped("SELECT userID, userLogin FROM admins", List.of("userID", "userLogin"), Integer.class, String.class);
    }

    @NonNull
    public static BidiMap<Integer, String> getOwners() throws SQLException
    {
        return getAllMapped("SELECT userID, userLogin FROM admins WHERE isOwner = TRUE", List.of("userID", "userLogin"), Integer.class, String.class);
    }

    @NonNull
    public static Set<Command> getCommands() throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "command", String.class,
                        "aliases", String.class,
                        "requiresAdmin", Boolean.class,
                        "requiresOwner", Boolean.class,
                        "class", String.class
                );

        List<Map<String, Object>> results = getAllMapped("SELECT * FROM commands", columnLabels);

        Set<Command> commands = new HashSet<>();

        for (Map<String, Object> result : results)
        {
            Command command = serializeResult(Command.class, result);
            commands.add(command);
        }

        return commands;
    }

    @NonNull
    public static Set<String> getAdminCommands() throws SQLException
    {
        return getCommands().stream()
                .filter(Command::requiresAdmin)
                .flatMap(command -> command.getCommandAndAliases().stream())
                .collect(Collectors.toSet());
    }

    @NonNull
    public static Set<String> getOwnerCommands() throws SQLException
    {
        return getCommands().stream()
                .filter(Command::requiresOwner)
                .flatMap(command -> command.getCommandAndAliases().stream())
                .collect(Collectors.toSet());
    }

    @NonNull
    public static Set<PrivateCommand> getPrivateCommands() throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "command", String.class,
                        "aliases", String.class,
                        "class", String.class
                );

        List<Map<String, Object>> results = getAllMapped("SELECT * FROM privateCommands", columnLabels);

        Set<PrivateCommand> privateCommands = new HashSet<>();

        for (Map<String, Object> result : results)
        {
            PrivateCommand privateCommand = serializeResult(PrivateCommand.class, result);
            privateCommands.add(privateCommand);
        }

        return privateCommands;
    }

    @NonNull
    public static String getPrefix(int userID) throws SQLException
    {
        String prefix = get(STR."SELECT prefix FROM customPrefixes WHERE userID = \{userID}", "prefix", String.class);
        return prefix == null ? "#" : prefix;
    }

    @NonNull
    public static Map<String, String> getGlobalCommands() throws SQLException
    {
        return getAllMapped("SELECT name, message FROM globalCommands", List.of("name", "message"), String.class);
    }

    @Nullable
    public static SpotifyUser getSpotifyUser(int userID) throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "accessToken", String.class,
                        "refreshToken", String.class,
                        "expiresOn", String.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM spotifyCredentials WHERE userID = \{userID}", columnLabels);

        return serializeResult(SpotifyUser.class, results);
    }

    @Nullable
    public static Set<String> getSevenTVAllowedUserIDs(int userID) throws SQLException
    {
        String allowedUserIDs = get(STR."SELECT * FROM sevenTVUsers WHERE userID = \{userID}", "allowedUserIDs", String.class);

        if (allowedUserIDs == null)
        {
            return null;
        }

        String[] sevenTVAllowedUserIDs = allowedUserIDs.split(",");

        if (ArrayUtils.isEmpty(sevenTVAllowedUserIDs))
        {
            return Collections.singleton(allowedUserIDs);
        }

        return Arrays.stream(sevenTVAllowedUserIDs).collect(Collectors.toSet());
    }

    @NonNull
    public static Set<Keyword> getKeywords(int userID) throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "name", String.class,
                        "message", String.class,
                        "exactMatch", Boolean.class
                );

        List<Map<String, Object>> results = getAllMapped(STR."SELECT * FROM customKeywords WHERE userID = \{userID}", columnLabels);

        Set<Keyword> keywords = new HashSet<>();

        for (Map<String, Object> result : results)
        {
            Keyword keyword = serializeResult(Keyword.class, result);
            keywords.add(keyword);
        }

        return keywords;
    }

    @NonNull
    public static Set<String> getEnabledEventNotificationChatLogins() throws SQLException
    {
        return getAll("SELECT userLogin FROM chats WHERE eventsEnabled = TRUE", "userLogin", String.class);
    }

    @Nullable
    public static Location getLocation(int userID) throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "latitude", Double.class,
                        "longitude", Double.class,
                        "locationName", String.class,
                        "cityName", String.class,
                        "countryCode", String.class,
                        "hideLocation", Boolean.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", columnLabels);

        return serializeResult(Location.class, results);
    }

    @Nullable
    public static TicTacToe getTicTacToeGame(int channelID) throws SQLException
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "playerIDs", String.class,
                        "board", String.class,
                        "nextUserID", String.class,
                        "round", Integer.class,
                        "startedAt", String.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM tictactoe WHERE userID = \{channelID}", columnLabels);

        return serializeResult(TicTacToe.class, results);
    }

    @NonNull
    public static Map<Integer, LocalDateTime> getTicTacToeStartTimes() throws SQLException
    {
        Map<Integer, String> results = getAllMapped("SELECT userID, startedAt FROM tictactoe", List.of("userID", "startedAt"), Integer.class, String.class);
        Set<Entry<Integer, String>> resultEntries = results.entrySet();

        return resultEntries.stream()
                .collect
                        (
                                Collectors.toMap
                                        (
                                                Entry::getKey,
                                                entry ->
                                                {
                                                    String value = entry.getValue();
                                                    return LocalDateTime.parse(value);
                                                }
                                        )
                        );
    }

    public static void correctUserLogin(int userID, @NonNull String newUserLogin) throws SQLException, IOException
    {
        List<String> tables = List.of("chats", "admins", "bible");

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
