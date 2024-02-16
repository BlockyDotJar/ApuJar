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
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CurrentSongCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String message = event.getMessage();
        String[] msgParts = message.split(" ");

        String userToGetSongFrom = getUserAsString(msgParts, eventUser);

        if (!isValidUsername(userToGetSongFrom))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToGetSongFrom);

        if (users.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGetSongFrom}' found.");
            return;
        }

        User user = users.getFirst();
        String displayName = user.getDisplayName();
        String userID = user.getId();
        int id = Integer.parseInt(userID);

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(id))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{displayName}' found in Spotify credential database. Sign in here TriHard https://blockydotjar.github.io/ApuJar-Website/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyApi = SpotifyUtils.getSpotifyAPI(id);

        GetUsersCurrentlyPlayingTrackRequest currentSongRequest = spotifyApi.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentSong = currentSongRequest.execute();

        if (currentSong == null)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{displayName} is not listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentSong.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        GetTrackRequest trackRequest = spotifyApi.getTrack(itemID).build();
        Track track = trackRequest.execute();
        String trackID = track.getId();

        CharSequence[] artistsArray = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toArray(CharSequence[]::new);
        String artists = String.join(", ", artistsArray);

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int progressMs = currentSong.getProgress_ms();
        int progessS = (progressMs / 1000) % 60;
        int progessM = (progressMs / 1000) / 60;

        String progressSeconds = decimalFormat.format(progessS);
        String progressMinutes = decimalFormat.format(progessM);

        int durationMs = playlistItem.getDurationMs();
        int durationS = (durationMs / 1000) % 60;
        int durationM = (durationMs / 1000) / 60;

        String durationSeconds = decimalFormat.format(durationS);
        String durationMinutes = decimalFormat.format(durationM);

        String song = STR."\{displayName} is currently listening to '\{itemName}' by \{artists} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        if (!currentSong.getIs_playing())
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{song}");
            return;
        }

        chat.sendMessage(channelName, STR."lebronJAM \{song}");
    }
}
