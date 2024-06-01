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
package dev.blocky.twitch.commands.seventv;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.SevenTVEmoteSet;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVCurrentEmoteSetCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();

        String userToGetEmoteSetFrom = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGetEmoteSetFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetURLFrom = retrieveUserList(client, userToGetEmoteSetFrom);

        if (usersToGetURLFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetEmoteSetFrom}' found.");
            return;
        }

        User user = usersToGetURLFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userIID);

        if (sevenTVTwitchUser == null)
        {
            return;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVTwitchUser.getCurrentEmoteSet();

        if (sevenTVEmoteSet == null)
        {
            sendChatMessage(channelID, "FeelsGoodMan No emote active emote-set found.");
            return;
        }

        String sevenTVEmoteSetID = sevenTVEmoteSet.getEmoteSetID();

        sevenTVEmoteSet = ServiceProvider.getSevenTVEmoteSet(channelIID, sevenTVEmoteSetID);

        String emoteSetName = sevenTVEmoteSet.getEmoteSetName();

        int emoteCount = sevenTVEmoteSet.getEmoteCount();
        int emoteSetCapacity = sevenTVEmoteSet.getCapacity();

        sendChatMessage(channelID, STR."SeemsGood Here is your 7tv emote-set link for the '\{emoteSetName}' emote-set from \{userDisplayName} (Capacity: \{emoteCount}/\{emoteSetCapacity}) \uD83D\uDC49 https://7tv.app/emote-sets/\{sevenTVEmoteSetID}");
    }
}
