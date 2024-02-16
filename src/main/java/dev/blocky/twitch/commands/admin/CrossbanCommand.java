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
import com.github.twitch4j.helix.domain.BanUserInput;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CrossbanCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String channelName = event.getChannel().getName();

        String message = event.getMessage();
        String[] msgParts = message.split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(channelName, "MEGALUL Please specify a user to crossban.");
            return;
        }

        String userToBan = getUserAsString(msgParts, 1);

        if (!isValidUsername(userToBan))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToBan = retrieveUserList(client, userToBan);

        if (usersToBan.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToBan}' found.");
            return;
        }

        String reason = null;

        if (msgParts.length >= 3)
        {
            String command = msgParts[0];
            String user = msgParts[1];

            int extraLength = user.startsWith("@") ? 1 : 0;

            reason = message.substring(command.length() + userToBan.length() + 2 + extraLength).strip();
        }

        User user = usersToBan.getFirst();

        HashSet<String> openedChats = SQLUtils.getOpenedChats();
        int bannedChats = openedChats.size();

        for (String openedChat : openedChats)
        {
            BanUserInput banUserInput = BanUserInput.builder()
                    .userId(user.getId())
                    .reason(reason)
                    .build();

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
                bannedChats--;
                continue;
            }

            try
            {
                client.getHelix().banUser(null, chatUser.getId(), "896181679", banUserInput).execute();
            }
            catch (Exception ignored)
            {
                bannedChats--;
            }
        }

        if (bannedChats == 0)
        {
            chat.sendMessage(channelName, STR."User '\{userToBan}' is already banned in every chat that i'm mod in NotLikeThis");
            return;
        }

        chat.sendMessage(channelName, STR."Successfully crossbanned '\{userToBan}' from \{bannedChats} chats LEL");
    }
}
