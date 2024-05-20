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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class EditGlobalCommandCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a global command name.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsGoodMan Please specify a message.");
            return;
        }

        String actualPrefix = SQLUtils.getPrefix(channelIID);

        String gcNameRaw = messageParts[1];
        String gcMessageRaw = removeElements(messageParts, 2);

        String gcName = removeIllegalCharacters(gcNameRaw);
        String gcMessage = removeIllegalCharacters(gcMessageRaw);

        if (gcMessage.isBlank())
        {
            sendChatMessage(channelID, "monkaLaugh The global command name/message can't only contain the character ' haha");
            return;
        }

        if (gcName.startsWith("/") || gcMessage.startsWith("/"))
        {
            sendChatMessage(channelID, "monkaLaugh The global command name/message can't start with a / (slash) haha");
            return;
        }

        if (gcName.startsWith(actualPrefix))
        {
            gcName = gcName.substring(actualPrefix.length());
        }

        Map<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (!globalCommands.containsKey(gcName))
        {
            sendChatMessage(channelID, STR."CoolStoryBob Global command '\{gcName}' doesn't exist.");
            return;
        }

        if (globalCommands.containsKey(gcName) && globalCommands.get(gcName).equals(gcMessage))
        {
            sendChatMessage(channelID, STR."4Head The new value for '\{gcName}' does exactly match with the old one.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE globalCommands SET message = '\{gcMessage}' WHERE name = '\{gcName}'");

        sendChatMessage(channelID, STR."SeemsGood Successfully edited global command '\{gcName}'");
    }
}
