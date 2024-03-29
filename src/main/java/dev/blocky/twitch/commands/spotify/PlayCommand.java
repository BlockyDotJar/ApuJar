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
import com.google.gson.JsonArray;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blocky.twitch.utils.TwitchUtils.getParameterAsString;

public class PlayCommand implements ICommand
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

        List<String> progresses = Arrays.stream(messageParts).filter(part ->
        {
            Pattern SPOTIFY_TRACK_PATTERN = Pattern.compile("-p(rogress)?=(\\d{2}):(\\d{2})", Pattern.CASE_INSENSITIVE);
            Matcher SPOTIFY_TRACK_MATCHER = SPOTIFY_TRACK_PATTERN.matcher(part);
            return SPOTIFY_TRACK_MATCHER.find();
        }).toList();

        boolean skipToPosition = false;

        String progressMinutes = "00";
        String progressSeconds = "00";

        if (!progresses.isEmpty())
        {
            String progressValueRaw = progresses.getFirst();

            int equalSign = progressValueRaw.indexOf('=');

            String progressValue = progressValueRaw.substring(equalSign + 1);
            String[] progressParts = progressValue.split(":");

            progressMinutes = progressParts[0];
            progressSeconds = progressParts[1];

            skipToPosition = true;
        }

        String spotifyTrack = getParameterAsString(messageParts, "-p(rogress)?=(\\d{2}):(\\d{2})");

        if (spotifyTrack == null)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a link or the id of the track.");
            return;
        }

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(eventUserIID))
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

        SearchTracksRequest searchTracksRequest = spotifyAPI.searchTracks(spotifyTrack).limit(5).includeExternal("audio").build();
        Paging<Track> tracksRaw = searchTracksRequest.execute();
        Track[] tracks = tracksRaw.getItems();

        if (tracks.length == 0)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{eventUserName} your track wasn't found.");
            return;
        }

        Track track = tracks[0];
        String trackName = track.getName();
        String trackID = track.getId();

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
                chat.sendMessage(channelName, "FeelsDankMan You can't skip to a position that is out of the songs range.");
                return;
            }

            Duration progressDuration = Duration.parse(STR."PT\{PMM}M\{PSS}S");
            long progressDurationMillis = progressDuration.toMillis();
            String progressMillis = String.valueOf(progressDurationMillis);
            int PMS = Integer.parseInt(progressMillis);

            SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = spotifyAPI.seekToPositionInCurrentlyPlayingTrack(PMS).build();
            seekPositionRequest.execute();
        }

        String messageToSend = STR."lebronJAM \{eventUserName} you're now listening to '\{trackName}' by \{artists} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        chat.sendMessage(channelName, messageToSend);
    }
}
