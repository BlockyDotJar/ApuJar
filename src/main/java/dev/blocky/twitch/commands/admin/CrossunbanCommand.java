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

import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CrossunbanCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String channelName = event.getChannel().getName();

        String[] msgParts = event.getMessage().split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user to crossunban.");
            return;
        }

        String userToCrossunban = getUserAsString(msgParts, 1);

        if (!isValidUsername(userToCrossunban))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToUnban = retrieveUserList(client, userToCrossunban);

        if (usersToUnban.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToCrossunban}' found.");
            return;
        }

        User user = usersToUnban.getFirst();

        HashSet<String> openedChats = SQLUtils.getOpenedChats();
        int unbannedChats = openedChats.size();

        for (String openedChat : openedChats)
        {
            List<User> chatUsers = retrieveUserList(client, openedChat);
            User chatUser = chatUsers.getFirst();

            IVRFI ivrfi = ServiceProvider.createIVRFIModVip(openedChat);

            boolean isMod = false;

            for (JsonNode mod : ivrfi.getMods())
            {
                String login = mod.get("login").asText();

                if (login.equalsIgnoreCase("ApuJar"))
                {
                    isMod = true;
                }
            }

            if (!isMod)
            {
                unbannedChats--;
                continue;
            }

            try
            {
                client.getHelix().unbanUser(null, chatUser.getId(), "896181679", user.getId()).execute();
            }
            catch (Exception ignored)
            {
                unbannedChats--;
            }
        }

        if (unbannedChats == 0)
        {
            chat.sendMessage(channelName, STR."User '\{userToCrossunban}' is not banned in any chat that i'm mod in NotLikeThis");
            return;
        }

        chat.sendMessage(channelName, STR."Successfully crossunbanned '\{userToCrossunban}' from \{unbannedChats} chats LEL");
    }
}
