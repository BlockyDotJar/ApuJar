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
import java.util.HashSet;

public class SeekSongPositionCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();
        EventUser user = event.getUser();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a number in milliseconds to set the songs position for.");
            return;
        }

        if (!msgParts[1].matches("^(-)?\\d$"))
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsDankMan Your specified point is not a number.");
            return;
        }

        int point = Integer.parseInt(msgParts[1]);

        if (point < 0)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsDankMan Number can't be under 0.");
            return;
        }

        int userID = Integer.parseInt(user.getId());

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(userID))
        {
            chat.sendMessage(event.getChannel().getName(), STR."ManFeels No user called '\{user.getName()}' found in Spotify credential database. Sign in here TriHard https://blockydotjar.github.io/ApuJar-Website/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyApi = SpotifyUtils.getSpotifyAPI(userID);

        GetUsersCurrentlyPlayingTrackRequest currentSongRequest = spotifyApi.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentSong = currentSongRequest.execute();

        if (currentSong == null)
        {
            chat.sendMessage(event.getChannel().getName(), STR."AlienUnpleased \{user.getName()} is not listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentSong.getItem();

        int progessS = (point / 1000) % 60;
        int progessM = (point / 1000) / 60;

        int durationMs = playlistItem.getDurationMs();
        int durationS = (durationMs / 1000) % 60;
        int durationM = (durationMs / 1000) / 60;

        if ((progessM > durationM && progessS > durationS) || (progessM == durationM && progessS > durationS))
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsDankMan You can't skip to a position that is out of the songs range.");
            return;
        }

        SeekToPositionInCurrentlyPlayingTrackRequest seekPositionRequest = spotifyApi.seekToPositionInCurrentlyPlayingTrack(point).build();
        seekPositionRequest.execute();

        DecimalFormat decimalFormat = new DecimalFormat("00");

        String progess = STR."FeelsGoodMan Skipped song position to \{decimalFormat.format(progessM)}:\{decimalFormat.format(progessS)} (of \{decimalFormat.format(durationM)}:\{decimalFormat.format(durationS)}) pepeJAMJAM";

        chat.sendMessage(event.getChannel().getName(), progess);
    }
}
