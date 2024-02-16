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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.SkipUsersPlaybackToPreviousTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SkipToPreviousSongCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String channelName = event.getChannel().getName();

        String[] msgParts = event.getMessage().split(" ");

        String userToGetSongFrom = getUserAsString(msgParts, event.getUser());

        if (!isValidUsername(userToGetSongFrom))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetSongFrom = retrieveUserList(client, userToGetSongFrom);

        if (usersToGetSongFrom.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGetSongFrom}' found.");
            return;
        }

        User user = usersToGetSongFrom.getFirst();
        String userName = user.getDisplayName();
        int userID = Integer.parseInt(user.getId());

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(userID))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{userName}' found in Spotify credential database. Sign in here TriHard https://blockydotjar.github.io/ApuJar-Website/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(userID);

        GetUsersAvailableDevicesRequest devicesRequest = spotifyAPI.getUsersAvailableDevices().build();
        Device[] devices = devicesRequest.execute();

        if (devices.length == 0)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{userName} is not online on Spotify.");
            return;
        }

        SkipUsersPlaybackToPreviousTrackRequest skipToPreviousRequest = spotifyAPI.skipUsersPlaybackToPreviousTrack().build();
        skipToPreviousRequest.execute();

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{userName} is not listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
        Track track = trackRequest.execute();
        String trackID = track.getId();

        ArtistSimplified[] artistsSimplified = track.getArtists();

        CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                .map(ArtistSimplified::getName)
                .toArray(CharSequence[]::new);

        String artists = String.join(", ", artistsRaw);

        String messageToSend = STR."lebronJAM \{userName} is now listening to '\{itemName}' by \{artists} donkJAM https://open.spotify.com/track/\{trackID}";

        chat.sendMessage(channelName, messageToSend);
    }
}
