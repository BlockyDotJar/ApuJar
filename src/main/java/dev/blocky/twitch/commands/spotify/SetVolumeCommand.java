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
import se.michaelthelin.spotify.requests.data.player.SetVolumeForUsersPlaybackRequest;

import java.util.HashSet;

public class SetVolumeCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();
        EventUser user = event.getUser();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a number to set the volume to.");
            return;
        }

        if (!msgParts[1].matches("^(-)?\\d+$"))
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsDankMan Your specified volume is not a number.");
            return;
        }

        int volume = Integer.parseInt(msgParts[1]);

        if (volume < 0 || volume > 100)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsDankMan Number can't be under 0 and over 100.");
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

        GetUsersAvailableDevicesRequest devicesRequest = spotifyApi.getUsersAvailableDevices().build();
        Device[] devices = devicesRequest.execute();

        if (devices.length == 0)
        {
            chat.sendMessage(event.getChannel().getName(), STR."AlienUnpleased \{user.getName()} you aren't listening to a song.");
            return;
        }

        SetVolumeForUsersPlaybackRequest volumeRequest = spotifyApi.setVolumeForUsersPlayback(volume).build();
        volumeRequest.execute();

        chat.sendMessage(event.getChannel().getName(), STR."pepeBASS \{user.getName()} set his/her volume to \{volume}% WAYTOODANK");
    }
}
