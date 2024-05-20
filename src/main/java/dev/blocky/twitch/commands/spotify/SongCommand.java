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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import dev.blocky.twitch.utils.serialization.SpotifyUser;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SongCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();

        String userToGetSongFrom = getParameterUserAsString(messageParts, "-e(mote)?=\\w+", eventUser);

        if (!isValidUsername(userToGetSongFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetSongFrom = retrieveUserList(client, userToGetSongFrom);

        if (usersToGetSongFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetSongFrom}' found.");
            return;
        }

        User user = usersToGetSongFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        SpotifyUser spotifyUser = SQLUtils.getSpotifyUser(userIID);

        if (spotifyUser == null)
        {
            sendChatMessage(channelID, STR."ManFeels No user called '\{userDisplayName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(userIID);

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{userDisplayName} isn't listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        String trackID = null;
        String albumName = null;
        String artists = null;

        if (itemID != null)
        {
            GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
            Track track = trackRequest.execute();
            trackID = track.getId();

            AlbumSimplified album = track.getAlbum();
            albumName = album.getName();

            ArtistSimplified[] artistsSimplified = track.getArtists();

            CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                    .map(ArtistSimplified::getName)
                    .toArray(CharSequence[]::new);

            artists = String.join(", ", artistsRaw);
        }

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int PMS = currentlyPlaying.getProgress_ms();
        int DMS = playlistItem.getDurationMs();

        Duration progressDuration = Duration.ofMillis(PMS);
        Duration duration = Duration.ofMillis(DMS);

        int PSS = progressDuration.toSecondsPart();
        long PMM = progressDuration.toMinutes();

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        String progressSeconds = decimalFormat.format(PSS);
        String progressMinutes = decimalFormat.format(PMM);

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        String beginEmote = "lebronJAM";
        String emote = getParameterValue(messageParts, "-e(mote)?=\\w+");

        if (emote != null)
        {
            beginEmote = emote;
        }

        if (!currentlyPlaying.getIs_playing())
        {
            beginEmote = "AlienUnpleased";
        }

        String messageToSend = STR."\{beginEmote} \{userDisplayName} is currently listening to '\{itemName}' by \{artists} from \{albumName} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        channelID = getActualChannelID(channelToSend, channelID);

        if (trackID == null)
        {
            messageToSend = STR."\{beginEmote} \{userDisplayName} is currently listening to '\{itemName}' (local file) donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds})";
        }

        sendChatMessage(channelID, messageToSend);
    }
}
