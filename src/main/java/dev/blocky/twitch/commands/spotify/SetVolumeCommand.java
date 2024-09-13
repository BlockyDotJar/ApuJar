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
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.SetVolumeForUsersPlaybackRequest;

import java.util.Arrays;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SetVolumeCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify a number.");
            return false;
        }

        String volumeValue = messageParts[1];

        if (!volumeValue.matches("^\\d{1,3}$"))
        {
            sendChatMessage(channelID, "FeelsDankMan Your specified volume isn't a number.");
            return false;
        }

        int volume = Integer.parseInt(volumeValue);

        if (volume < 0 || volume > 100)
        {
            sendChatMessage(channelID, "FeelsDankMan Number can't be under 0 and over 100.");
            return false;
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

        if (currentDevice.getIs_restricted() || !currentDevice.getSupports_volume())
        {
            sendChatMessage(channelID, "ManFeels You either activated the web api restriction or your current device doesn't allow volume changing.");
            return false;
        }

        SetVolumeForUsersPlaybackRequest volumeRequest = spotifyAPI.setVolumeForUsersPlayback(volume).build();
        volumeRequest.execute();

        return sendChatMessage(channelID, STR."pepeBASS \{eventUserName} set his/her volume to \{volume}% WAYTOODANK");
    }
}
