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
package dev.blocky.twitch.commands.spotify;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ArtistsCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToGetTopArtistsFrom = getParameterUserAsString(messageParts, "(-s(hort)?)|(-l(ong)?)", eventUser);

        if (!isValidUsername(userToGetTopArtistsFrom))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetTopArtistsFrom = retrieveUserList(client, userToGetTopArtistsFrom);

        if (usersToGetTopArtistsFrom.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGetTopArtistsFrom}' found.");
            return;
        }

        User user = usersToGetTopArtistsFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        boolean hasLongParameter = Arrays.stream(messageParts).anyMatch("-long"::equalsIgnoreCase);
        boolean hasLParameter = Arrays.stream(messageParts).anyMatch("-l"::equalsIgnoreCase);

        boolean hasShortParameter = Arrays.stream(messageParts).anyMatch("-short"::equalsIgnoreCase);
        boolean hasSParameter = Arrays.stream(messageParts).anyMatch("-s"::equalsIgnoreCase);

        String timeRange = "medium_term";

        if (hasLongParameter || hasLParameter)
        {
            timeRange = "long_term";
        }

        if (hasShortParameter || hasSParameter)
        {
            timeRange = "short_term";
        }

        HashSet<Integer> spotifyUserIIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIIDs.contains(userIID))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{userDisplayName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(userIID);

        GetUsersTopArtistsRequest topArtistsRequest = spotifyAPI.getUsersTopArtists().limit(10).time_range(timeRange).build();
        Paging<Artist> topArtists = topArtistsRequest.execute();
        Artist[] artists = topArtists.getItems();

        int range = Math.min(artists.length, 10);

        List<String> topUserArtistsRaw = IntStream.range(0, range).mapToObj(i ->
        {
            int artistNumber = i + 1;

            Artist artist = artists[i];
            String artistName = artist.getName();

            return STR."\{artistNumber}. \{artistName}";
        }).toList();

        String topUserArtists = String.join(" | ", topUserArtistsRaw);

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, STR."FeelsOkayMan Here are \{userDisplayName}'s top 10 tracks \uD83D\uDC49 \{topUserArtists}");
    }
}
