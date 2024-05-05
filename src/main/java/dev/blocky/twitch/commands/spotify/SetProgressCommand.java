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
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.SeekToPositionInCurrentlyPlayingTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashSet;

public class SetProgressCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a value that matches with the RegEx. (^\\d{1,2}:\\d{1,2}$))");
            return;
        }

        String progressValue = messageParts[1];

        if (!progressValue.matches("^\\d{1,2}:\\d{1,2}$"))
        {
            chat.sendMessage(channelName, "FeelsDankMan Your specified progress must match with RegEx. (^\\d{1,2}:\\d{1,2}$)");
            return;
        }

        String[] progressParts = progressValue.split(":");
        String progressMinutes = progressParts[0];
        String progressSeconds = progressParts[1];

        int PMM = Integer.parseInt(progressMinutes);
        int PSS = Integer.parseInt(progressSeconds);

        HashSet<Integer> spotifyUserIIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIIDs.contains(eventUserIID))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{eventUserName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(eventUserIID);

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{eventUserName} you aren't listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int DMS = playlistItem.getDurationMs();

        Duration progressDuration = Duration.parse(STR."PT\{PMM}M\{PSS}S");
        Duration duration = Duration.ofMillis(DMS);

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        progressSeconds = decimalFormat.format(PSS);
        progressMinutes = decimalFormat.format(PMM);

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        if ((PMM > DMM && PSS > DSS) || (PMM == DMM && PSS > DSS))
        {
            chat.sendMessage(channelName, "FeelsDankMan You can't skip to a position that is out of the songs range.");
            return;
        }

        long progressDurationMillis = progressDuration.toMillis();

        String progressMillis = String.valueOf(progressDurationMillis);
        int PMS = Integer.parseInt(progressMillis);

        SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = spotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
        seekPositionRequest.execute();

        String messageToSend = STR."FeelsGoodMan Skipped song position to \{progressMinutes}:\{progressSeconds} (of \{durationMinutes}:\{durationSeconds}) pepeJAMJAM";

        chat.sendMessage(channelName, messageToSend);
    }
}
