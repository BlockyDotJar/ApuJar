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
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.TreeMap;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class DeleteGlobalCommandCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a global command.");
            return false;
        }

        Prefix prefix = SQLUtils.getPrefix(channelIID);
        String actualPrefix = prefix.getPrefix();
        int prefixLength = actualPrefix.length();

        boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

        String gcName = messageParts[1];

        if ((gcName.startsWith(actualPrefix) && !caseInsensitivePrefix) || (StringUtils.startsWithIgnoreCase(gcName, actualPrefix) && caseInsensitivePrefix))
        {
            if (!gcName.equals(actualPrefix))
            {
                gcName = gcName.substring(prefixLength);
            }
        }

        TreeMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (!globalCommands.containsKey(gcName))
        {
            sendChatMessage(channelID, STR."CoolStoryBob Global command '\{gcName}' doesn't exist.");
            return false;
        }

        SQLite.onUpdate(STR."DELETE FROM globalCommands WHERE name = '\{gcName}'");

        return sendChatMessage(channelID, STR."SeemsGood Successfully deleted global command '\{gcName}'");
    }
}
