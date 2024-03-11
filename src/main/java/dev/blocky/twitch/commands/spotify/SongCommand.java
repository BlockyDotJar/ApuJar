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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SpotifyUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SongCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToGetSongFrom = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGetSongFrom))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetSongFrom = retrieveUserList(client, userToGetSongFrom);

        if (usersToGetSongFrom.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGetSongFrom}' found.");
            return;
        }

        User user = usersToGetSongFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        HashSet<Integer> spotifyUserIIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIIDs.contains(userIID))
        {
            chat.sendMessage(channelName, STR."ManFeels No user called '\{userDisplayName}' found in Spotify credential database FeelsDankMan The user needs to sign in here TriHard \uD83D\uDC49 https://apujar.blockyjar.dev/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyAPI = SpotifyUtils.getSpotifyAPI(userIID);

        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spotifyAPI.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();

        if (currentlyPlaying == null)
        {
            chat.sendMessage(channelName, STR."AlienUnpleased \{userDisplayName} isn't listening to a song.");
            return;
        }

        IPlaylistItem playlistItem = currentlyPlaying.getItem();
        String itemName = playlistItem.getName();
        String itemID = playlistItem.getId();

        GetTrackRequest trackRequest = spotifyAPI.getTrack(itemID).build();
        Track track = trackRequest.execute();
        String trackID = track.getId();

        ArtistSimplified[] artistsSimplified = track.getArtists();

        CharSequence[] artistsRaw = Arrays.stream(artistsSimplified)
                .map(ArtistSimplified::getName)
                .toArray(CharSequence[]::new);

        String artists = String.join(", ", artistsRaw);

        DecimalFormat decimalFormat = new DecimalFormat("00");

        int PMS = currentlyPlaying.getProgress_ms();
        int DMS = playlistItem.getDurationMs();

        Duration progressDuration = Duration.ofMillis(PMS);
        Duration duration = Duration.ofMillis(DMS);

        int PSS = progressDuration.toSecondsPart();
        long PMM = progressDuration.toMinutes();

        int DSS = duration.toSecondsPart();
        long DMM = duration.toMinutes();

        String progressSeconds = decimalFormat.format(PSS);
        String progressMinutes = decimalFormat.format(PMM);

        String durationSeconds = decimalFormat.format(DSS);
        String durationMinutes = decimalFormat.format(DMM);

        String messageToSend = STR."\{userDisplayName} is currently listening to '\{itemName}' by \{artists} donkJAM (\{progressMinutes}:\{progressSeconds}/\{durationMinutes}:\{durationSeconds}) https://open.spotify.com/track/\{trackID}";

        String beginEmote = "lebronJAM";

        if (!currentlyPlaying.getIs_playing())
        {
            beginEmote = "AlienUnpleased";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, STR."\{beginEmote} \{messageToSend}");
    }
}
