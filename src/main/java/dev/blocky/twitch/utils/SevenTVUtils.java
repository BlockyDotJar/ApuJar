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
import dev.blocky.api.entities.seventv.*;
import dev.blocky.api.gql.SevenTVGQLBody;
import dev.blocky.comparator.SevenTVEmoteComparator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class SevenTVUtils
{
    @NonNull
    public static SevenTV getUser(@NonNull String userName) throws IOException
    {
        String query = """
                    query SearchUsers($query: String!) {
                        users(query: $query) {
                            id
                            username
                            display_name
                        }
                    }
                """;

        Map<String, Object> variables = Map.of
                (
                        "query", userName
                );

        SevenTVGQLBody gql = new SevenTVGQLBody("SearchUsers", variables, query);

        return ServiceProvider.postSevenTVGQL(gql);
    }

    @NonNull
    public static SevenTV searchEmotes(@NonNull String emoteName) throws IOException
    {
        String query = """
                query SearchEmotes($query: String!, $page: Int, $sort: Sort, $limit: Int, $filter: EmoteSearchFilter) {
                    emotes(query: $query, page: $page, sort: $sort, limit: $limit, filter: $filter) {
                        items {
                            id
                            name
                        }
                    }
                }
                """;

        Map<String, Object> sort = Map.of
                (
                        "value", "popularity",
                        "order", "DESCENDING"
                );

        Map<String, Object> filter = Map.of
                (
                        "exact_match", true,
                        "case_sensitive", true,
                        "ignore_tags", true,
                        "zero_width", false,
                        "animated", false,
                        "aspect_ratio", ""
                );

        Map<String, Object> variables = Map.of
                (
                        "query", emoteName,
                        "limit", 25,
                        "page", 1,
                        "sort", sort,
                        "filter", filter
                );

        SevenTVGQLBody gql = new SevenTVGQLBody("SearchEmotes", variables, query);

        return ServiceProvider.postSevenTVGQL(gql);
    }

    @NonNull
    public static SevenTV changeEmote(@NonNull SevenTVEmoteChangeAction action, @NonNull String emoteSetID, @NonNull String emoteID, @NonNull String emoteName) throws IOException
    {
        String query = """
                    mutation ChangeEmoteInSet($id: ObjectID!, $action: ListItemAction!, $emote_id: ObjectID!, $name: String) {
                        emoteSet(id: $id) {
                            id
                            emotes(id: $emote_id, action: $action, name: $name) {
                                id
                                name
                            }
                        }
                    }
                """;

        Map<String, Object> variables = Map.of
                (
                        "action", action.name(),
                        "id", emoteSetID,
                        "emote_id", emoteID,
                        "name", emoteName
                );

        SevenTVGQLBody gql = new SevenTVGQLBody("ChangeEmoteInSet", variables, query);

        return ServiceProvider.postSevenTVGQL(gql);
    }

    @NonNull
    public static List<SevenTVEmote> getFilteredEmotes(@NonNull List<SevenTVEmote> sevenTVEmotes, @NonNull String emoteName)
    {
        return sevenTVEmotes.stream()
                .filter(sevenTVEmote ->
                {
                    String sevenTVEmoteName = sevenTVEmote.getName();
                    return sevenTVEmoteName.equalsIgnoreCase(emoteName);
                })
                .sorted(new SevenTVEmoteComparator(emoteName))
                .toList();
    }

    @NonNull
    public static List<SevenTVUser> getFilteredUsers(@NonNull List<SevenTVUser> sevenTVUsers, @NonNull String userName)
    {
        return sevenTVUsers.stream()
                .filter(sevenTVUser ->
                {
                    String sevenTVUsername = sevenTVUser.getUsername();
                    return sevenTVUsername.equalsIgnoreCase(userName);
                })
                .toList();
    }

    @Nullable
    public static SevenTVUserConnection getSevenTVUserConnection(@NonNull SevenTVUser sevenTVUser)
    {
        ArrayList<SevenTVUserConnection> sevenTVUserConnections = sevenTVUser.getConnections();

        for (SevenTVUserConnection connection : sevenTVUserConnections)
        {
            String platform = connection.getPlatform();

            if (platform.equals("TWITCH"))
            {
                return connection;
            }
        }
        return null;
    }

    public static boolean isAllowedEditor(int channelID, int userID) throws SQLException
    {
        String allowedUserIDsRaw = SQLUtils.getSevenTVAllowedUserIDs(channelID);

        if (allowedUserIDsRaw != null)
        {
            List<Integer> allowedUserIIDs = null;

            if (allowedUserIDsRaw.contains(","))
            {
                String[] allowedUserIDs = allowedUserIDsRaw.split(",");
                allowedUserIIDs = Arrays.stream(allowedUserIDs)
                        .mapToInt(Integer::parseInt)
                        .boxed()
                        .toList();
            }

            if (allowedUserIIDs == null)
            {
                int allowedUserIID = Integer.parseInt(allowedUserIDsRaw);
                allowedUserIIDs = Collections.singletonList(allowedUserIID);
            }

            for (int allowedUserIID : allowedUserIIDs)
            {
                if (allowedUserIID == userID)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAllowedEditor(int channelID, int userID, @NonNull String sevenTVUserID, @NonNull String userName) throws SQLException, IOException
    {
        boolean isAllowedEditor = isAllowedEditor(channelID, userID);

        if (isAllowedEditor)
        {
            return true;
        }

        SevenTVUser sevenTVUser = ServiceProvider.getSevenTVUser(sevenTVUserID);
        ArrayList<SevenTVUser> sevenTVEditors = sevenTVUser.getEditors();

        SevenTV sevenTV = SevenTVUtils.getUser(userName);
        SevenTVData sevenTVData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVData.getUsers();
        List<SevenTVUser> sevenTVUsersFiltered = getFilteredUsers(sevenTVUsers, userName);

        if (sevenTVUsersFiltered.isEmpty())
        {
            return false;
        }

        sevenTVUser = sevenTVUsersFiltered.getFirst();
        sevenTVUserID = sevenTVUser.getID();

        for (SevenTVUser sevenTVEditor : sevenTVEditors)
        {
            String sevenTVEditorID = sevenTVEditor.getID();

            if (!sevenTVEditorID.equals(sevenTVUserID))
            {
                continue;
            }

            return true;
        }

        return false;
    }
}
