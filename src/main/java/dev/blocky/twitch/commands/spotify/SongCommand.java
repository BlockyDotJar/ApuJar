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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.SpotifyUser;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.CurrentlyPlayingType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.episodes.GetEpisodeRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SongCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToGetSongFrom = getParameterUserAsString(messageParts, "-e(mote)?=\\w+", eventUserName);

        if (!isValidUsername(userToGetSongFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToGetSongFrom = retrieveUserList(client, userToGetSongFrom);

        if (usersToGetSongFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetSongFrom}' found.");
            return false;
        }

        User user = usersToGetSongFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        SpotifyUser spotifyUser = SQLUtils.getSpotifyUser(userIID);

        if (spotifyUser == null)
        {
            sendChatMessage(channelID, STR."ManFeels No user called '\{userDisplayName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return false;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(userIID);

        GetUsersAvailableDevicesRequest deviceRequest = spotifyAPI.getUsersAvailableDevices().build();
        Device[] devices = deviceRequest.execute();

        if (devices.length == 0)
        {
            sendChatMessage(channelID, "ManFeels No current Spotify devices found.");
            return false;
        }

        Device currentDevice = Arrays.stream(devices).filter(Device::getIs_active).findFirst().orElse(devices[0]);

        if (currentDevice.getIs_private_session() || currentDevice.getIs_restricted())
        {
            sendChatMessage(channelID, "ManFeels You are either in a private session or you activated the web api restriction.");
            return false;
        }

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().additionalTypes("episode").build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{userDisplayName} isn't listening to a song / episode.");
            return false;
        }

        CurrentlyPlayingType currentlyPlayingType = currentlyPlaying.getCurrentlyPlayingType();
        String playingType = currentlyPlayingType.getType();

        String trackID = null;
        String albumName = null;
        String artists = null;

        IPlaylistItem playlistItem = currentlyPlaying.getItem();

        if (playlistItem == null)
        {
            sendChatMessage(channelID, "ManFeels Couldn't find any track or episode. Please check if you're banned on Spotify, or if your Spotify Premium license expired.");
            return false;
        }

        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        if (playingType.equals("track") && itemID != null)
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

        if (playingType.equals("episode"))
        {
            GetEpisodeRequest episodeRequest = spotifyAPI.getEpisode(itemID).build();
            Episode episode = episodeRequest.execute();

            trackID = episode.getId();

            ShowSimplified show = episode.getShow();
            albumName = show.getName();
            artists = show.getPublisher();
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

        if (playingType.equals("episode"))
        {
            messageToSend = STR."\{beginEmote} \{userDisplayName} is currently listening to the podcast episode '\{itemName}' by \{artists} from the '\{albumName}' podcast Listening (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/episode/\{trackID}";
            return sendChatMessage(channelID, messageToSend);
        }

        if (trackID == null)
        {
            messageToSend = STR."\{beginEmote} \{userDisplayName} is currently listening to '\{itemName}' (local file) donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds})";
            return sendChatMessage(channelID, messageToSend);
        }

        return sendChatMessage(channelID, messageToSend);
    }
}
