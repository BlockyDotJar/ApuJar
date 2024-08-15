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
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.SeekToPositionInCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class PlayAlbumCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify the name of the album.");
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

        String spotifyAlbum = getParameterAsString(messageParts, "-p(rogress)?=(\\d{2}):(\\d{2})");

        if (spotifyAlbum == null)
        {
            sendChatMessage(channelID, "FeelsMan Please specify the name of the album.");
            return false;
        }

        if (spotifyAlbum.matches("^(https?://open.spotify.com/(intl-[a-z_-]+/)?album/)?[a-zA-Z\\d]{22}([\\w=?&-]+)?$"))
        {
            sendChatMessage(channelID, "FeelsOkayMan Please use the 'albumlink' command instead.");
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

        SearchAlbumsRequest searchAlbumsRequest = spotifyAPI.searchAlbums(spotifyAlbum)
                .includeExternal("audio")
                .limit(5)
                .build();

        Paging<AlbumSimplified> albumsRaw = searchAlbumsRequest.execute();
        AlbumSimplified[] albums = albumsRaw.getItems();

        if (albums.length == 0)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} your album wasn't found.");
            return false;
        }

        AlbumSimplified album = albums[0];
        String albumName = album.getName();
        String albumID = album.getId();

        StartResumeUsersPlaybackRequest startRequest = spotifyAPI.startResumeUsersPlayback()
                .context_uri(STR."spotify:album:\{albumID}")
                .build();

        startRequest.execute();

        TimeUnit.SECONDS.sleep(1);

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you aren't listening to a song.");
            return false;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        String trackID = null;
        String artists = null;

        if (itemID != null)
        {
            GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
            Track track = trackRequest.execute();
            trackID = track.getId();

            ArtistSimplified[] artistsSimplified = track.getArtists();

            CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                    .map(ArtistSimplified::getName)
                    .toArray(CharSequence[]::new);

            artists = String.join(", ", artistsRaw);
        }

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int DMS = playlistItem.getDurationMs();

        Duration duration = Duration.ofMillis(DMS);

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        int PSS = Integer.parseInt(progressSeconds);
        int PMM = Integer.parseInt(progressMinutes);

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

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

        String messageToSend = STR."lebronJAM \{eventUserName} you're now listening to '\{itemName}' by \{artists} from \{albumName} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        return sendChatMessage(channelID, messageToSend);
    }
}
