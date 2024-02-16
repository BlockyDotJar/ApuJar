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
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;

import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class PausePlaybackCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String[] msgParts = event.getMessage().split(" ");

        String userToGetSongFrom = getUserAsString(msgParts, event.getUser());

        if (!isValidUsername(userToGetSongFrom))
        {
            chat.sendMessage(event.getChannel().getName(), "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToGetSongFrom);

        if (users.isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), STR.":| No user called '\{userToGetSongFrom}' found.");
            return;
        }

        User user = users.getFirst();
        int userID = Integer.parseInt(user.getId());

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(userID))
        {
            chat.sendMessage(event.getChannel().getName(), STR."ManFeels No user called '\{user.getDisplayName()}' found in Spotify credential database. Sign in here TriHard https://blockydotjar.github.io/ApuJar-Website/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyApi = SpotifyUtils.getSpotifyAPI(userID);

        GetUsersCurrentlyPlayingTrackRequest currentSongRequest = spotifyApi.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentSong = currentSongRequest.execute();

        if (currentSong == null || !currentSong.getIs_playing())
        {
            chat.sendMessage(event.getChannel().getName(), STR."AlienUnpleased \{user.getDisplayName()} is not listening to a song.");
            return;
        }

        PauseUsersPlaybackRequest pausePlaybackRequest = spotifyApi.pauseUsersPlayback().build();
        pausePlaybackRequest.execute();

        chat.sendMessage(event.getChannel().getName(), STR."jamm \{user.getDisplayName()} paused his/her song.");
    }
}
