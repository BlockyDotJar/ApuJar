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
import com.google.gson.JsonArray;
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
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.SeekToPositionInCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class YoinkCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "WideHardo Please specify the user.");
            return;
        }

        boolean skipToExactPosition = false;
        boolean skipToPosition = false;

        String progressMinutes = "00";
        String progressSeconds = "00";

        if (messageParts.length >= 3)
        {
            String progressValue = messageParts[2];

            if (progressValue.equalsIgnoreCase("WideHardo"))
            {
                skipToExactPosition = true;
            }

            if (progressValue.matches("^\\d{1,2}:\\d{1,2}$"))
            {
                String[] progressParts = progressValue.split(":");
                progressMinutes = progressParts[0];
                progressSeconds = progressParts[1];

                skipToPosition = true;
            }
        }

        String userToYoinkSongFrom = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToYoinkSongFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToYoinkSongFrom = retrieveUserList(client, userToYoinkSongFrom);

        if (usersToYoinkSongFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToYoinkSongFrom}' found.");
            return;
        }

        User user = usersToYoinkSongFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (userLogin.equals(eventUserName))
        {
            sendChatMessage(channelID, STR."4Head \{userDisplayName} you can't yoink your own song.");
            return;
        }

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

        SpotifyApi eventUserSpotifyAPI = SpotifyUtils.getSpotifyAPI(eventUserIID);

        GetUsersAvailableDevicesRequest deviceRequest = eventUserSpotifyAPI.getUsersAvailableDevices().build();
        Device[] devices = deviceRequest.execute();

        boolean anyActiveDevice = Arrays.stream(devices).anyMatch(Device::getIs_active);

        if (devices.length == 0 || !anyActiveDevice)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you aren't online on Spotify.");
            return;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        if (itemID == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you can't yoink local file songs.");
            return;
        }

        GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
        Track track = trackRequest.execute();
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

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        JsonArray uris = new JsonArray();
        uris.add(STR."spotify:track:\{trackID}");

        StartResumeUsersPlaybackRequest startTrackRequest = eventUserSpotifyAPI.startResumeUsersPlayback().uris(uris).build();
        startTrackRequest.execute();

        if (skipToExactPosition)
        {
            int PMS = currentlyPlaying.getProgress_ms();

            Duration progessDuration = Duration.ofMillis(PMS);

            int PSS = progessDuration.toSecondsPart();
            long PMM = progessDuration.toMinutes();

            progressSeconds = decimalFormat.format(PSS);
            progressMinutes = decimalFormat.format(PMM);

            TimeUnit.MILLISECONDS.sleep(250);

            SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = eventUserSpotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
            seekPositionRequest.execute();
        }

        if (skipToPosition)
        {
            int PSS = Integer.parseInt(progressSeconds);
            int PMM = Integer.parseInt(progressMinutes);

            progressSeconds = decimalFormat.format(PSS);
            progressMinutes = decimalFormat.format(PMM);

            if ((PMM > DMM && PSS > DSS) || (PMM == DMM && PSS > DSS))
            {
                sendChatMessage(channelID, "FeelsDankMan You can't skip to a position that is out of the songs range.");
                return;
            }

            Duration progressDuration = Duration.parse(STR."PT\{PMM}M\{PSS}S");
            long progressDurationMillis = progressDuration.toMillis();

            String progressMillis = String.valueOf(progressDurationMillis);
            int PMS = Integer.parseInt(progressMillis);

            TimeUnit.MILLISECONDS.sleep(250);

            SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = eventUserSpotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
            seekPositionRequest.execute();
        }

        String messageToSend = STR."AlienDance \{eventUserName} you yoinked \{userDisplayName}'s song '\{itemName}' by \{artists} from \{albumName} WideHardo (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        sendChatMessage(channelID, messageToSend);
    }
}
