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
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.tools.ToolsModVIP;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class ReceiveEventNotificationsCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a boolean. (Either true or false)");
            return;
        }

        String receiveValue = messageParts[1];

        if (!receiveValue.matches("^true|false$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid value specified. (Choose between true or false)");
            return;
        }

        boolean shouldBeEnabled = Boolean.parseBoolean(receiveValue);

        List<ToolsModVIP> toolsMods = ServiceProvider.getToolsMods(channelName);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(toolsMods, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "ManFeels You can't edit event notifcations, because you aren't the broadcaster or a moderator.");
            return;
        }

        Chat chat = SQLUtils.getChat(channelIID);
        boolean isEnabled = chat.hasEventsEnabled();

        receiveValue = shouldBeEnabled ? "enabled" : "disabled";

        if (shouldBeEnabled == isEnabled)
        {
            sendChatMessage(channelID, STR."4Head Event notifications had already been \{receiveValue}.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE chats SET eventsEnabled = \{shouldBeEnabled} WHERE userID = \{channelIID}");

        sendChatMessage(channelID, STR."SeemsGood Successfully \{receiveValue} event notifications for this chat.");
    }
}
