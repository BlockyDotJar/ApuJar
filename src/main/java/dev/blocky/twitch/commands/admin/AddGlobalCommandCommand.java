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
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.HashSet;

import static dev.blocky.twitch.utils.SQLUtils.removeApostrophe;

public class AddGlobalCommandCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a global command name to set.");
            return;
        }

        if (msgParts.length == 2)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsGoodMan Please specify a message for the global command.");
            return;
        }

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());

        String gcName = removeApostrophe(msgParts[1].strip());
        String gcMessage = removeApostrophe(event.getMessage().substring(actualPrefix.length() + msgParts[0].substring(actualPrefix.length()).length() + 1 + gcName.length()).strip());

        if (gcName.isBlank() || gcMessage.isBlank())
        {
            chat.sendMessage(event.getChannel().getName(), "monkaLaugh The global command name/message can't contain ' because of some weird sql things.");
            return;
        }

        if (gcName.startsWith(actualPrefix))
        {
            gcName = gcName.substring(actualPrefix.length());
        }

        HashMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (globalCommands.containsKey(gcName))
        {
            chat.sendMessage(event.getChannel().getName(), STR."CoolStoryBob Global command '\{gcName}' does already exist.");
            return;
        }

        HashSet<String> commands = SQLUtils.getCommands();

        if (commands.contains(gcName))
        {
            chat.sendMessage(event.getChannel().getName(), STR."FeelsDankMan A bot command '\{gcName}' does already exist.");
            return;
        }

        SQLite.onUpdate(STR."INSERT INTO globalCommands(name, message, userID, loginName) VALUES('\{gcName}', '\{gcMessage}', \{event.getUser().getId()}, '\{event.getUser().getName().toLowerCase()}')");

        chat.sendMessage(event.getChannel().getName(), STR."SeemsGood Successfully created global command '\{gcName}'");
    }
}
