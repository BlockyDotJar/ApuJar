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
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.SeekToPositionInCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class ResumeCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        boolean skipToPosition = false;

        String prgMinutes = "00";
        String prgSeconds = "00";

        if (messageParts.length >= 2)
        {
            String progressValue = messageParts[1];

            if (progressValue.matches("^\\d{1,2}:\\d{1,2}$"))
            {
                String[] progressParts = progressValue.split(":");
                prgMinutes = progressParts[0];
                prgSeconds = progressParts[1];

                skipToPosition = true;
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

        if (currentDevice.getIs_private_session() || currentDevice.getIs_restricted())
        {
            sendChatMessage(channelID, "ManFeels You are either in a private session or you activated the web api restriction.");
            return false;
        }

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying != null && currentlyPlaying.getIs_playing())
        {
            sendChatMessage(channelID, STR."AlienDance \{eventUserName} you're already listening to a song / episode.");
            return false;
        }

        StartResumeUsersPlaybackRequest resumeRequest = spotifyAPI.startResumeUsersPlayback().build();
        resumeRequest.execute();

        if (currentlyPlaying == null)
        {
            currentlyPlaying = currentlyPlayingRequest.execute();
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();

        if (playlistItem == null)
        {
            sendChatMessage(channelID, "ManFeels Couldn't find any track or episode. Please check if you're banned on Spotify, or if your Spotify Premium license expired.");
            return false;
        }

        String itemID = playlistItem.getId();

        GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
        Track track = trackRequest.execute();

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int PMS = currentlyPlaying.getProgress_ms();

        Duration progessDuration = Duration.ofMillis(PMS);

        int PSS = progessDuration.toSecondsPart();
        long PMM = progessDuration.toMinutes();

        String progressSeconds = decimalFormat.format(PSS);
        String progressMinutes = decimalFormat.format(PMM);

        int DMS = track.getDurationMs();

        Duration duration = Duration.ofMillis(DMS);

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        if (skipToPosition)
        {
            PSS = Integer.parseInt(prgSeconds);
            PMM = Integer.parseInt(prgMinutes);

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
            PMS = Integer.parseInt(progressMillis);

            SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = spotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
            seekPositionRequest.execute();
        }

        return sendChatMessage(channelID, STR."jamm \{eventUserName} resumed his/her song or episode at position \{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}.");
    }
}
