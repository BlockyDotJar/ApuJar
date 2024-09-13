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
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.SpotifyUser;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.episodes.GetEpisodeRequest;
import se.michaelthelin.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class QueueCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a link or the id of the track.");
            return false;
        }

        String spotifyTrack = messageParts[1];

        if (!spotifyTrack.matches("^(https?://open.spotify.com/(intl-[a-z_-]+/)?track/)?[a-zA-Z\\d]{22}([\\w=?&-]+)?$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid Spotify song link or id specified.");
            return false;
        }

        if (spotifyTrack.length() != 22)
        {
            int lastSlashIndex = spotifyTrack.lastIndexOf('/');
            spotifyTrack = spotifyTrack.substring(lastSlashIndex + 1);

            if (spotifyTrack.contains("?"))
            {
                int firstQuestionMarkIndex = spotifyTrack.indexOf('?');
                spotifyTrack = spotifyTrack.substring(0, firstQuestionMarkIndex);
            }
        }

        SpotifyUser spotifyUser = SQLUtils.getSpotifyUser(eventUserIID);

        if (spotifyUser == null)
        {
            sendChatMessage(channelID, STR."ManFeels No user called '\{eventUserName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return false;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(eventUserIID);

        GetUsersAvailableDevicesRequest deviceRequest = spotifyAPI.getUsersAvailableDevices().build();
        Device[] devices = deviceRequest.execute();

        boolean anyActiveDevice = Arrays.stream(devices).anyMatch(Device::getIs_active);

        if (devices.length == 0 || !anyActiveDevice)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you aren't online on Spotify.");
            return false;
        }

        Device currentDevice = Arrays.stream(devices).filter(Device::getIs_active).findFirst().orElse(devices[0]);

        if (currentDevice.getIs_restricted())
        {
            sendChatMessage(channelID, "ManFeels Can't execute request, you activated the web api restriction.");
            return false;
        }

        String trackName = null;
        String trackID = null;

        String albumName = null;
        String artists = null;

        int DMS = -1;

        GetTrackRequest trackRequest = spotifyAPI.getTrack(spotifyTrack).build();
        Track track = trackRequest.execute();

        if (track != null)
        {
            trackName = track.getName();
            trackID = track.getId();

            AddItemToUsersPlaybackQueueRequest addItemToPlaybackQueueRequest = spotifyAPI.addItemToUsersPlaybackQueue(STR."spotify:track:\{spotifyTrack}").build();
            addItemToPlaybackQueueRequest.execute();

            AlbumSimplified album = track.getAlbum();
            albumName = album.getName();

            ArtistSimplified[] artistsSimplified = track.getArtists();

            CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                    .map(ArtistSimplified::getName)
                    .toArray(CharSequence[]::new);

            artists = String.join(", ", artistsRaw);

            DMS = track.getDurationMs();
        }

        if (track == null)
        {
            GetEpisodeRequest episodeRequest = spotifyAPI.getEpisode(spotifyTrack).build();
            Episode episode = episodeRequest.execute();

            if (episode == null)
            {
                sendChatMessage(channelID, "ManFeels No track or episode was found by the Spotify API.");
                return false;
            }

            AddItemToUsersPlaybackQueueRequest addItemToPlaybackQueueRequest = spotifyAPI.addItemToUsersPlaybackQueue(STR."spotify:episode:\{spotifyTrack}").build();
            addItemToPlaybackQueueRequest.execute();

            trackName = episode.getName();
            trackID = episode.getId();

            ShowSimplified show = episode.getShow();
            albumName = show.getName();
            artists = show.getPublisher();

            DMS = episode.getDurationMs();
        }

        DecimalFormat decimalFormat = new DecimalFormat("00");

        Duration duration = Duration.ofMillis(DMS);

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        String messageToSend = STR."\{eventUserName} notee Added '\{trackName}' by \{artists} from \{albumName} donkJAM (\{durationMinutes}:\{durationSeconds}) to \{eventUserName}'s queue https://open.spotify.com/track/\{trackID}";

        if (track == null)
        {
            messageToSend = STR."\{eventUserName} notee Added podcast episode '\{trackName}' by \{artists} from the '\{albumName}' podcast Listening (\{durationMinutes}:\{durationSeconds}) to \{eventUserName}'s queue https://open.spotify.com/episode/\{trackID}";
            return sendChatMessage(channelID, messageToSend);
        }

        return sendChatMessage(channelID, messageToSend);
    }
}
