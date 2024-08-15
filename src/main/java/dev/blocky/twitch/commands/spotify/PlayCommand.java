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
import com.google.gson.JsonArray;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.SpotifyUser;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.SeekToPositionInCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class PlayCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify the name of the track.");
            return false;
        }

        boolean skipToPosition = false;

        String progressMinutes = "00";
        String progressSeconds = "00";

        String progressRaw = getParameterValue(messageParts, "-p(rogress)?=(\\d{2}):(\\d{2})");

        if (progressRaw != null)
        {
            String[] progressParts = progressRaw.split(":");

            progressMinutes = progressParts[0];
            progressSeconds = progressParts[1];

            skipToPosition = true;
        }

        String spotifyTrack = getParameterAsString(messageParts, "-p(rogress)?=(\\d{2}):(\\d{2})");

        if (spotifyTrack == null)
        {
            sendChatMessage(channelID, "FeelsMan Please specify the name of the track.");
            return false;
        }

        if (spotifyTrack.matches("^(https?://open.spotify.com/(intl-[a-z_-]+/)?track/)?[a-zA-Z\\d]{22}([\\w=?&-]+)?$"))
        {
            sendChatMessage(channelID, "FeelsOkayMan Please use the 'playlink' command instead.");
            return false;
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

        SearchTracksRequest searchTracksRequest = spotifyAPI.searchTracks(spotifyTrack)
                .includeExternal("audio")
                .limit(5)
                .build();

        Paging<Track> tracksRaw = searchTracksRequest.execute();
        Track[] tracks = tracksRaw.getItems();

        if (tracks.length == 0)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} your track wasn't found.");
            return false;
        }

        Track track = tracks[0];
        String trackName = track.getName();
        String trackID = track.getId();

        AlbumSimplified album = track.getAlbum();
        String albumName = album.getName();

        ArtistSimplified[] artistsSimplified = track.getArtists();

        CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                .map(ArtistSimplified::getName)
                .toArray(CharSequence[]::new);

        String artists = String.join(", ", artistsRaw);

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int DMS = track.getDurationMs();

        Duration duration = Duration.ofMillis(DMS);

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        int PSS = Integer.parseInt(progressSeconds);
        int PMM = Integer.parseInt(progressMinutes);

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        JsonArray uris = new JsonArray();
        uris.add(STR."spotify:track:\{trackID}");

        StartResumeUsersPlaybackRequest startRequest = spotifyAPI.startResumeUsersPlayback().uris(uris).build();
        startRequest.execute();

        if (skipToPosition)
        {
            progressSeconds = decimalFormat.format(PSS);
            progressMinutes = decimalFormat.format(PMM);

            if ((PMM > DMM && PSS > DSS) || (PMM == DMM && PSS > DSS))
            {
                sendChatMessage(channelID, "FeelsDankMan You can't skip to a position that is out of the songs range.");
                return false;
            }

            Duration progressDuration = Duration.parse(STR."PT\{PMM}M\{PSS}S");
            long progressDurationMillis = progressDuration.toMillis();

            String progressMillis = String.valueOf(progressDurationMillis);
            int PMS = Integer.parseInt(progressMillis);

            SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = spotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
            seekPositionRequest.execute();
        }

        String messageToSend = STR."lebronJAM \{eventUserName} you're now listening to '\{trackName}' by \{artists} from \{albumName} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        return sendChatMessage(channelID, messageToSend);
    }
}
