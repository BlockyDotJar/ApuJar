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
import se.michaelthelin.spotify.enums.CurrentlyPlayingType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.episodes.GetEpisodeRequest;
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
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "WideHardo Please specify the user.");
            return false;
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
            return false;
        }

        List<User> usersToYoinkSongFrom = retrieveUserList(client, userToYoinkSongFrom);

        if (usersToYoinkSongFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToYoinkSongFrom}' found.");
            return false;
        }

        User user = usersToYoinkSongFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (userLogin.equals(eventUserName))
        {
            sendChatMessage(channelID, STR."4Head \{userDisplayName} you can't yoink your own song / episode.");
            return false;
        }

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
            sendChatMessage(channelID, STR."ManFeels No current Spotify devices found for user \{userDisplayName}.");
            return false;
        }

        Device currentDevice = Arrays.stream(devices).filter(Device::getIs_active).findFirst().orElse(devices[0]);

        if (currentDevice.getIs_private_session() || currentDevice.getIs_restricted())
        {
            sendChatMessage(channelID, STR."ManFeels \{userDisplayName} is either in a private session or he has activated the web api restriction.");
            return false;
        }

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().additionalTypes("episode").build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{userDisplayName} isn't listening to a song / episode.");
            return false;
        }

        SpotifyApi eventUserSpotifyAPI = SpotifyUtils.getSpotifyAPI(eventUserIID);

        deviceRequest = eventUserSpotifyAPI.getUsersAvailableDevices().build();
        devices = deviceRequest.execute();

        boolean anyActiveDevice = Arrays.stream(devices).anyMatch(Device::getIs_active);

        if (devices.length == 0 || !anyActiveDevice)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you aren't online on Spotify.");
            return false;
        }

        currentDevice = Arrays.stream(devices).filter(Device::getIs_active).findFirst().orElse(devices[0]);

        if (currentDevice.getIs_private_session() || currentDevice.getIs_restricted())
        {
            sendChatMessage(channelID, "ManFeels You are either in a private session or you activated the web api restriction.");
            return false;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();

        if (playlistItem == null)
        {
            sendChatMessage(channelID, "ManFeels Couldn't find any track or episode. Please check if you're banned on Spotify, or if your Spotify Premium license expired.");
            return false;
        }

        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        if (itemID == null)
        {
            sendChatMessage(channelID, STR."AlienUnpleased \{eventUserName} you can't yoink local file songs / episodes.");
            return false;
        }

        CurrentlyPlayingType currentlyPlayingType = currentlyPlaying.getCurrentlyPlayingType();
        String playingType = currentlyPlayingType.getType();

        String trackID = null;
        String albumName = null;
        String artists = null;

        if (playingType.equals("track"))
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

        int DMS = playlistItem.getDurationMs();

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
                sendChatMessage(channelID, "FeelsDankMan You can't skip to a position that is out of the songs / episodes range.");
                return false;
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

        if (playingType.equals("episode"))
        {
            messageToSend = STR."Listening \{userDisplayName} you yoinked \{userDisplayName}'s podcast episode '\{itemName}' by \{artists} from the '\{albumName}' podcast WideHardo (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/episode/\{trackID}";
            return sendChatMessage(channelID, messageToSend);
        }

        return sendChatMessage(channelID, messageToSend);
    }
}
