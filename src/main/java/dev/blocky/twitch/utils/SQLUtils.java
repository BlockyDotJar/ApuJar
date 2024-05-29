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

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;
import static java.util.Map.Entry;

public class SQLUtils
{
    @Nullable
    public static <T> T get(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz)
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            return result.getObject(columnLabel, clazz);
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> errorClazz = e.getClass();
            String errorClazzName = errorClazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{errorClazzName})");

            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    public static <T> Set<T> getAll(@NonNull String sql, @NonNull String columnLabel, @NonNull Class<T> clazz)
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
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> errorClazz = e.getClass();
            String errorClazzName = errorClazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{errorClazzName})");

            e.printStackTrace();
        }

        return Collections.emptySet();
    }

    @NonNull
    public static Map<String, Object> getMapped(@NonNull String sql, @NonNull Map<String, Class<?>> columnLabels, boolean nilCheck)
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

            if (allNil && nilCheck)
            {
                return null;
            }

            return map;
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    @NonNull
    public static List<Map<String, Object>> getAllMapped(@NonNull String sql, @NonNull Map<String, Class<?>> columnLabels)
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
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @NonNull
    public static TreeMap<String, String> getAllTreeMapped(@NonNull String sql, @NonNull List<String> columnLabels)
    {
        try (ResultSet result = SQLite.onQuery(sql))
        {
            TreeMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            while (result.next())
            {
                String firstColumnLabel = columnLabels.getFirst();
                String lastColumnLabel = columnLabels.getLast();

                String key = result.getString(firstColumnLabel);
                String value = result.getString(lastColumnLabel);

                map.put(key, value);
            }
            return map;
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }

        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    @NonNull
    public static <U, T> BidiMap<U, T> getAllBidiMapped(@NonNull String sql, @NonNull List<String> columnLabels, @NonNull Class<U> clazz, @NonNull Class<T> extraClazz)
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
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> errorClazz = e.getClass();
            String errorClazzName = errorClazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to execute a sql-query FeelsGoodMan \{error} (\{errorClazzName})");

            e.printStackTrace();
        }

        return new DualHashBidiMap<>();
    }

    @NonNull
    public static <T> T serializeResult(@NonNull Class<T> clazz, @NonNull Map<String, Object> results)
    {
        Gson gson = new Gson();
        String jsonToSerialize = gson.toJson(results);
        return gson.fromJson(jsonToSerialize, clazz);
    }

    @NonNull
    public static Set<Chat> getChats()
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
    public static Chat getChat(int userID)
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
    public static BidiMap<Integer, String> getAdmins()
    {
        return getAllBidiMapped("SELECT userID, userLogin FROM admins", List.of("userID", "userLogin"), Integer.class, String.class);
    }

    @NonNull
    public static BidiMap<Integer, String> getOwners()
    {
        return getAllBidiMapped("SELECT userID, userLogin FROM admins WHERE isOwner = TRUE", List.of("userID", "userLogin"), Integer.class, String.class);
    }

    @NonNull
    public static Set<Command> getCommands()
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
    public static Set<String> getAdminCommands()
    {
        return getCommands().stream()
                .filter(Command::requiresAdmin)
                .flatMap(command ->
                {
                    Set<String> commandAndAliases = command.getCommandAndAliases();
                    return commandAndAliases.stream();
                })
                .collect(Collectors.toSet());
    }

    @NonNull
    public static Set<String> getOwnerCommands()
    {
        return getCommands().stream()
                .filter(Command::requiresOwner)
                .flatMap(command ->
                {
                    Set<String> commandAndAliases = command.getCommandAndAliases();
                    return commandAndAliases.stream();
                })
                .collect(Collectors.toSet());
    }

    @NonNull
    public static Set<PrivateCommand> getPrivateCommands()
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

    @Nullable
    public static Prefix getPrefix(int userID)
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "prefix", String.class,
                        "caseInsensitive", Boolean.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM customPrefixes WHERE userID = \{userID}", columnLabels, false);

        return serializeResult(Prefix.class, results);
    }

    @NonNull
    public static TreeMap<String, String> getGlobalCommands()
    {
        return getAllTreeMapped("SELECT name, message FROM globalCommands", List.of("name", "message"));
    }

    @Nullable
    public static SpotifyUser getSpotifyUser(int userID)
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "accessToken", String.class,
                        "refreshToken", String.class,
                        "expiresOn", String.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM spotifyCredentials WHERE userID = \{userID}", columnLabels, true);

        return serializeResult(SpotifyUser.class, results);
    }

    @Nullable
    public static Set<String> getSevenTVAllowedUserIDs(int userID)
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
    public static Set<Keyword> getKeywords(int userID)
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "name", String.class,
                        "message", String.class,
                        "exactMatch", Boolean.class,
                        "caseInsensitive", Boolean.class
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
    public static Set<String> getEnabledEventNotificationChatLogins()
    {
        return getAll("SELECT userLogin FROM chats WHERE eventsEnabled = TRUE", "userLogin", String.class);
    }

    @Nullable
    public static Location getLocation(int userID)
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

        Map<String, Object> results = getMapped(STR."SELECT * FROM weatherLocations WHERE userID = \{userID}", columnLabels, true);

        return serializeResult(Location.class, results);
    }

    @Nullable
    public static TicTacToe getTicTacToeGame(int channelID)
    {
        Map<String, Class<?>> columnLabels = Map.of
                (
                        "playerIDs", String.class,
                        "board", String.class,
                        "nextUserID", String.class,
                        "round", Integer.class,
                        "startedAt", String.class
                );

        Map<String, Object> results = getMapped(STR."SELECT * FROM tictactoe WHERE userID = \{channelID}", columnLabels, true);

        return serializeResult(TicTacToe.class, results);
    }

    @NonNull
    public static Map<Integer, LocalDateTime> getTicTacToeStartTimes()
    {
        Map<Integer, String> results = getAllBidiMapped("SELECT userID, startedAt FROM tictactoe", List.of("userID", "startedAt"), Integer.class, String.class);
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
                ServiceProvider.patchUser(896181679, userID, body);
            }
        }
    }
}
