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
package dev.blocky.twitch.commands.stats;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.stats.StreamElementsChatStats;
import dev.blocky.api.entities.stats.StreamElementsChatter;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class TopChatterCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();

        String userToGetTopChatterFrom = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGetTopChatterFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetTopChatterFrom = retrieveUserList(client, userToGetTopChatterFrom);

        if (usersToGetTopChatterFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetTopChatterFrom}' found.");
            return;
        }

        User user = usersToGetTopChatterFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        StreamElementsChatStats streamElementsChatStats = ServiceProvider.getChatStats(userLogin, 5);

        if (streamElementsChatStats == null)
        {
            sendChatMessage(channelID, "UNLUCKY No streamelements chatstats for user found.");
            return;
        }

        List<StreamElementsChatter> chatters = streamElementsChatStats.getChatters();
        String topChatter = getChatterFormatted(chatters);

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, STR."peepoChat Here are the top chatter for \{userDisplayName} \uD83D\uDC49 \{topChatter}");
    }
}
