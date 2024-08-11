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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Command;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.TreeMap;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class AddGlobalCommandCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
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

        Prefix prefix = SQLUtils.getPrefix(channelIID);
        String actualPrefix = prefix.getPrefix();
        int prefixLength = actualPrefix.length();

        boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

        String gcNameRaw = messageParts[1];
        String gcMessageRaw = removeElements(messageParts, 2);

        if (gcNameRaw.startsWith("/") || gcMessageRaw.startsWith("/"))
        {
            sendChatMessage(channelID, "monkaLaugh The global command name/message can't start with a / (slash) haha");
            return;
        }

        if ((gcNameRaw.startsWith(actualPrefix) && !caseInsensitivePrefix) || (StringUtils.startsWithIgnoreCase(gcNameRaw, actualPrefix) && caseInsensitivePrefix))
        {
            if (!gcNameRaw.equals(actualPrefix))
            {
                gcNameRaw = gcNameRaw.substring(prefixLength);
            }
        }

        String gcName = handleIllegalCharacters(gcNameRaw);
        String gcMessage = handleIllegalCharacters(gcMessageRaw);

        TreeMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (globalCommands.containsKey(gcNameRaw))
        {
            sendChatMessage(channelID, STR."CoolStoryBob Global command '\{gcNameRaw}' does already exist.");
            return;
        }

        Set<Command> commands = SQLUtils.getCommands();

        for (Command command : commands)
        {
            Set<String> commandAndAliases = command.getCommandAndAliases();

            if (commandAndAliases.contains(gcNameRaw))
            {
                sendChatMessage(channelID, STR."FeelsDankMan A native bot command '\{gcNameRaw}' does already exist.");
                return;
            }
        }

        SQLite.onUpdate(STR."INSERT INTO globalCommands(name, message) VALUES('\{gcName}', '\{gcMessage}')");

        sendChatMessage(channelID, STR."SeemsGood Successfully created global command '\{gcName}'");
    }
}
