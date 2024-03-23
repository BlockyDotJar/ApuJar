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
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

public class QueueCommand implements ICommand
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
            chat.sendMessage(channelName, "FeelsMan Please specify a link or the id of the track.");
            return;
        }

        String spotifyTrack = messageParts[1];

        if (!spotifyTrack.matches("^(http(s)?://open.spotify.com/(intl-[a-z_-]+/)?track/)?[a-zA-Z\\d]{22}([\\w=?&-]+)?$"))
        {
            chat.sendMessage(channelName, "FeelsMan Invalid Spotify song link or id specified.");
            return;
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

        HashSet<Integer> spotifyUserIIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIIDs.contains(eventUserIID))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{eventUserName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(eventUserIID);

        GetUsersAvailableDevicesRequest deviceRequest = spotifyAPI.getUsersAvailableDevices().build();
        Device[] devices = deviceRequest.execute();

        if (devices.length == 0)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{eventUserName} you aren't online on Spotify.");
            return;
        }

        GetTrackRequest trackRequest = spotifyAPI.getTrack(spotifyTrack).build();
        Track track = trackRequest.execute();

        if (track == null)
        {
            chat.sendMessage(channelName, "ManFeels No track was found by the Spotify API.");
            return;
        }

        if (!track.getIsPlayable())
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{eventUserName} your track isn't playable for some reason.");
            return;
        }

        String trackName = track.getName();
        String trackID = track.getId();

        AddItemToUsersPlaybackQueueRequest addItemToPlaybackQueueRequest = spotifyAPI.addItemToUsersPlaybackQueue(STR."spotify:track:\{spotifyTrack}").build();
        addItemToPlaybackQueueRequest.execute();

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

        String queueItem = STR."notee Added '\{trackName}' by \{artists} donkJAM (\{durationMinutes}:\{durationSeconds}) to \{eventUserName}'s queue https://open.spotify.com/track/\{trackID}";

        chat.sendMessage(channelName, queueItem);
    }
}
