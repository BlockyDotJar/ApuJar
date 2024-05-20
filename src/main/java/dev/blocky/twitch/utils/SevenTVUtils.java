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
import dev.blocky.api.request.SevenTVGQLBody;
import dev.blocky.comparator.SevenTVEmoteComparator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SevenTVUtils
{
    @NonNull
    public static SevenTV searchEmotes(@NonNull String emoteName) throws IOException
    {
        String query = """
                query SearchEmotes($query: String!, $page: Int, $sort: Sort, $limit: Int, $filter: EmoteSearchFilter) {
                    emotes(query: $query, page: $page, sort: $sort, limit: $limit, filter: $filter) {
                        items {
                            id
                            name
                            flags
                            listed
                            animated
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
                        "limit", 50,
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
    public static SevenTV getUserCosmentics(@NonNull String sevenTVUserID) throws IOException
    {
        String query = """
                    query GetUserCosmetics($id: ObjectID!) {
                        user(id: $id) {
                            cosmetics {
                                id
                                kind
                                selected
                            }
                        }
                    }
                """;

        Map<String, Object> variables = Map.of("id", sevenTVUserID);

        SevenTVGQLBody gql = new SevenTVGQLBody("GetUserCosmetics", variables, query);

        return ServiceProvider.postSevenTVGQL(gql);
    }

    @NonNull
    public static SevenTV getCosmentics(@NonNull String cosmeticID) throws IOException
    {
        String query = """
                query GetCosmestics($list: [ObjectID!]) {
                    cosmetics(list: $list) {
                        paints {
                            id
                            name
                        }
                        badges {
                            id
                            name
                        }
                    }
                }
                """;


        List<String> list = Collections.singletonList(cosmeticID);
        Map<String, Object> variables = Map.of("list", list);

        SevenTVGQLBody gql = new SevenTVGQLBody("GetCosmestics", variables, query);

        return ServiceProvider.postSevenTVGQL(gql);
    }

    @NonNull
    public static List<SevenTVEmote> getFilteredEmotes(@NonNull List<SevenTVEmote> sevenTVEmotes, @NonNull String emoteName)
    {
        SevenTVEmoteComparator emoteComparator = new SevenTVEmoteComparator(emoteName);

        return sevenTVEmotes.stream()
                .filter(sevenTVEmote ->
                {
                    String sevenTVEmoteName = sevenTVEmote.getEmoteName();
                    return sevenTVEmoteName.equalsIgnoreCase(emoteName);
                })
                .sorted(emoteComparator)
                .toList();
    }

    private static boolean isAllowedEditorLocal(int channelID, int userID) throws SQLException
    {
        Set<String> allowedUserIDsRaw = SQLUtils.getSevenTVAllowedUserIDs(channelID);

        if (allowedUserIDsRaw != null)
        {
            List<Integer> allowedUserIIDs = allowedUserIDsRaw.stream()
                        .mapToInt(Integer::parseInt)
                        .boxed()
                        .toList();

            return allowedUserIIDs.stream().anyMatch(allowedUserIID -> allowedUserIID == userID);
        }

        return false;
    }

    public static boolean isAllowedEditor(int channelIID, int userID) throws SQLException, IOException
    {
        boolean isAllowedEditor = isAllowedEditorLocal(channelIID, userID);

        if (isAllowedEditor)
        {
            return true;
        }

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, channelIID);

        if (sevenTVTwitchUser == null)
        {
            return false;
        }

        SevenTVUser sevenTVUser = sevenTVTwitchUser.getUser();
        List<SevenTVUser> sevenTVEditors = sevenTVUser.getEditors();

        sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userID);

        if (sevenTVTwitchUser == null)
        {
            return false;
        }

        sevenTVUser = sevenTVTwitchUser.getUser();
        String sevenTVUserID = sevenTVUser.getUserID();

        return sevenTVEditors.stream().anyMatch(sevenTVEditor ->
        {
            String sevenTVEditorID = sevenTVEditor.getUserID();
            return sevenTVEditorID.equals(sevenTVUserID);
        });
    }

    public static boolean checkErrors(@NonNull String channelID, @Nullable List<SevenTVError> errors)
    {
        if (errors != null)
        {
            SevenTVError error = errors.getFirst();
            SevenTVErrorExtension errorExtension = error.getErrorExtension();
            String errorMessage = errorExtension.getErrorMessage();
            int errorCode = errorExtension.getErrorCode();

            sendChatMessage(channelID, STR."(7TV) error (\{errorCode}) undefined \ud83d\udc4d \{errorMessage}");
            return true;
        }

        return false;
    }
}
