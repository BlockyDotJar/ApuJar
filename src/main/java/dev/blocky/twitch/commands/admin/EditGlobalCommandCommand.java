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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;

import static dev.blocky.twitch.utils.SQLUtils.removeApostrophe;
import static dev.blocky.twitch.utils.TwitchUtils.removeElements;

public class EditGlobalCommandCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a global command name.");
            return;
        }

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsGoodMan Please specify a message.");
            return;
        }

        String actualPrefix = SQLUtils.getActualPrefix(channelID);

        String gcNameRaw = messageParts[1].strip();
        String gcMessageRaw = removeElements(messageParts, 2);

        String gcName = removeApostrophe(gcNameRaw);
        String gcMessage = removeApostrophe(gcMessageRaw);

        if (gcMessage.isBlank())
        {
            chat.sendMessage(channelName, "monkaLaugh The global command name/message can't contain the character ' haha");
            return;
        }

        if (gcName.startsWith(actualPrefix))
        {
            gcName = gcName.substring(actualPrefix.length());
        }

        HashMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (!globalCommands.containsKey(gcName))
        {
            chat.sendMessage(channelName, STR."CoolStoryBob Global command '\{gcName}' doesn't exist.");
            return;
        }

        if (globalCommands.containsKey(gcName) && globalCommands.get(gcName).equals(gcMessage))
        {
            chat.sendMessage(channelName, STR."4Head The new value for '\{gcName}' does exactly match with the old one.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE globalCommands SET message = '\{gcMessage}' WHERE name = '\{gcName}'");

        chat.sendMessage(channelName, STR."SeemsGood Successfully edited global command '\{gcName}'");
    }
}
