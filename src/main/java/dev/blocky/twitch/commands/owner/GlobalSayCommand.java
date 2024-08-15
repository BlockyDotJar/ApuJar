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
package dev.blocky.twitch.commands.owner;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.removeElements;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class GlobalSayCommand implements ICommand
{
    public static String channelToSend;

    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsGoodMan Please specify a message.");
            return false;
        }

        String messageToSend = removeElements(messageParts, 1);

        if (messageToSend.startsWith("/"))
        {
            sendChatMessage(channelID, "4Head / (slash) commands are not allowed in global commands.");
            return false;
        }

        Set<Chat> chats = SQLUtils.getChats();

        for (Chat chat : chats)
        {
            int chatIID = chat.getUserID();
            String chatID = String.valueOf(chatIID);

            sendChatMessage(chatID, messageToSend);

            TimeUnit.MILLISECONDS.sleep(50);
        }

        int chatCount = chats.size();

        return sendChatMessage(channelID, STR."SeemsGood Successfully sent message in \{chatCount} chats.");
    }
}
