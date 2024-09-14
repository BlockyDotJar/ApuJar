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
package dev.blocky.twitch.commands.admin;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class EchoCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a message.");
            return false;
        }

        String messageToSend = removeElements(messageParts, 1);

        if (messageToSend.startsWith("/") && !messageToSend.equals("/"))
        {
            List<Badge> badges = event.getBadges();

            boolean successfulExecution = handleSlashCommands(channelIID, eventUserIID, channelIID, messageParts, badges, 1);

            if (successfulExecution)
            {
                sendChatMessage(channelID, "GIGACHAD Successfully executed slash command.");
                return true;
            }

            sendChatMessage(channelID, "MONKA Slash command execution failed unexpectedly.");
            return false;
        }

        boolean isSendable = checkChatSettings(messageParts, channelName, channelID, channelID);

        if (!isSendable)
        {
            return false;
        }

        return sendChatMessage(channelID, messageToSend);
    }
}
