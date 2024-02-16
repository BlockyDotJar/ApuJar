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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserSpamCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a chat to spam the messages in.");
            return;
        }

        if (msgParts.length == 2)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a number of messages to spam.");
            return;
        }

        if (!StringUtils.isNumeric(msgParts[2]))
        {
            chat.sendMessage(event.getChannel().getName(), "ManFeels The second parameter is not even an integer.");
            return;
        }

        if (msgParts.length == 3)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a message to spam in the chat.");
            return;
        }

        int messageCount = Integer.parseInt(msgParts[2]);

        if (messageCount > 100 && !SQLUtils.getOwnerIDs().contains(Integer.parseInt(event.getUser().getId())))
        {
            chat.sendMessage(event.getChannel().getName(), "ManFeels Number can't be bigger than 100, because you aren't an owner.");
            return;
        }

        String chatToSpam = getUserAsString(msgParts, 1);

        if (!isValidUsername(chatToSpam))
        {
            chat.sendMessage(event.getChannel().getName(), "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, chatToSpam);

        if (users.isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), STR.":| No user called '\{chatToSpam}' found.");
            return;
        }

        User user = users.getFirst();

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());

        String spamMessage = event.getMessage().substring(actualPrefix.length() + msgParts[0].substring(actualPrefix.length()).length() + 1 + msgParts[2].length() + 1 + chatToSpam.length() + (msgParts[1].startsWith("@") ? 1 : 0)).strip();

        if (spamMessage.startsWith("/"))
        {
            if (!SQLUtils.getOwnerIDs().contains(Integer.parseInt(event.getUser().getId())))
            {
                chat.sendMessage(event.getChannel().getName(), "DatSheffy You don't have permission to spam any kind of / (slash) commands through my account.");
                return;
            }

            IVRFI ivrfi = ServiceProvider.createIVRFIModVip(user.getLogin());

            boolean isMod = false;
            boolean isModSelf = false;

            for (JsonNode mod : ivrfi.getMods())
            {
                if (mod.get("login").asText().equals(event.getUser().getName().toLowerCase()))
                {
                    isMod = true;
                    continue;
                }

                if (mod.get("login").asText().equals("apujar"))
                {
                    isModSelf = true;
                }
            }

            if (!isMod && !chatToSpam.equals(event.getUser().getName().toLowerCase()))
            {
                chat.sendMessage(event.getChannel().getName(), "ManFeels You can't use / (slash) commands, because you aren't a broadcaster or a moderator of this chat.");
                return;
            }

            if (!isModSelf)
            {
                chat.sendMessage(event.getChannel().getName(), "ManFeels You can't use / (slash) commands, because i'm not a broadcaster or a moderator of this chat.");
                return;
            }
        }

        for (int i = 0; i < messageCount; i++)
        {
            chat.sendMessage(user.getLogin(), spamMessage);
            TimeUnit.MILLISECONDS.sleep(500);
        }

        chat.sendMessage(event.getChannel().getName(), STR."SeemsGood Successfully spammed messages in \{user.getDisplayName()}'s chat.");
    }
}
