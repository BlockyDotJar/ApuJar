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
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.ToggleShuffleForUsersPlaybackRequest;

import java.util.HashSet;

public class ShuffleCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();
        EventUser user = event.getUser();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify if the song should be shuffled or not. (true/false)");
            return;
        }

        String shuffleStr = msgParts[1];

        if (!shuffleStr.matches("^true|false$"))
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Invalid value specified. (Choose between true or false)");
            return;
        }

        boolean shuffle = Boolean.parseBoolean(shuffleStr);

        int userID = Integer.parseInt(user.getId());

        HashSet<Integer> spotifyUserIDs = SQLUtils.getSpotifyUserIDs();

        if (!spotifyUserIDs.contains(userID))
        {
            chat.sendMessage(event.getChannel().getName(), STR."ManFeels No user called '\{user.getName()}' found in Spotify credential database. Sign in here TriHard https://blockydotjar.github.io/ApuJar-Website/oauth2/spotify.html");
            return;
        }

        SpotifyApi spotifyApi = SpotifyUtils.getSpotifyAPI(userID);

        GetUsersAvailableDevicesRequest devicesRequest = spotifyApi.getUsersAvailableDevices().build();
        Device[] devices = devicesRequest.execute();

        if (devices.length == 0)
        {
            chat.sendMessage(event.getChannel().getName(), STR."AlienUnpleased \{user.getName()} is not active at the moment.");
            return;
        }

        ToggleShuffleForUsersPlaybackRequest toggleShufflePlaybackRequest = spotifyApi.toggleShuffleForUsersPlayback(shuffle).build();
        toggleShufflePlaybackRequest.execute();

        String state = shuffle ? "Enabled" : "Disabled";

        chat.sendMessage(event.getChannel().getName(), STR."forsenAutismo \{state} shuffle for your Spotify songs.");
    }
}
