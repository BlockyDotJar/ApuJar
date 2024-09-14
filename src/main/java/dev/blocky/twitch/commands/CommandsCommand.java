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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CommandsCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts)
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String chatToSend = getUserAsString(messageParts, eventUserName);
        List<User> chatsToSend = retrieveUserList(client, chatToSend);

        if (chatsToSend.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{chatToSend}' found.");
            return false;
        }

        User user = chatsToSend.getFirst();
        String userDisplayName = user.getDisplayName();

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, STR."\{userDisplayName} peepoHappy \uD83D\uDC49 Here you can see everything important about the bot https://apujar.blockyjar.dev/ FeelsOkayMan");
    }
}
