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
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SpamCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a number of messages to spam.");
            return;
        }

        if (!StringUtils.isNumeric(msgParts[1]))
        {
            chat.sendMessage(event.getChannel().getName(), "ManFeels The first parameter is not even an integer.");
            return;
        }

        if (msgParts.length == 2)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a message to spam in the chat.");
            return;
        }

        int messageCount = Integer.parseInt(msgParts[1]);

        if (messageCount > 100 && !SQLUtils.getOwnerIDs().contains(Integer.parseInt(event.getUser().getId())))
        {
            chat.sendMessage(event.getChannel().getName(), "ManFeels Number can't be bigger than 100, because you aren't an owner.");
            return;
        }

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());

        String spamMessage = event.getMessage().substring(actualPrefix.length() + 5 + msgParts[1].length() + 1).strip();

        if (spamMessage.startsWith("/") && !SQLUtils.getOwnerIDs().contains(Integer.parseInt(event.getUser().getId())))
        {
            chat.sendMessage(event.getChannel().getName(), "DatSheffy You don't have permission to spam any kind of / (slash) commands through my account.");
            return;
        }

        IRCMessageEvent irc = event.getMessageEvent();
        Map<String, String> badges = irc.getBadges();
        Set<String> badgeKeys = badges.keySet();

        if (spamMessage.startsWith("/"))
        {
            if (!SQLUtils.getOwnerIDs().contains(Integer.parseInt(event.getUser().getId())))
            {
                chat.sendMessage(event.getChannel().getName(), "DatSheffy You don't have permission to use any kind of / (slash) commands through my account.");
                return;
            }

            IVRFI ivrfi = ServiceProvider.createIVRFIModVip(event.getUser().getName().toLowerCase());

            boolean isModSelf = false;

            for (JsonNode mod : ivrfi.getMods())
            {
                if (mod.get("login").asText().equals("apujar"))
                {
                    isModSelf = true;
                }
            }

            if (!badgeKeys.contains("broadcaster") && !badgeKeys.contains("moderator"))
            {
                chat.sendMessage(event.getChannel().getName(), "ManFeels You can't use / (slash) commands, because you aren't a broadcaster or a moderator.");
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
            chat.sendMessage(event.getChannel().getName(), spamMessage);
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }
}
