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
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
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

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToUnban = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToUnban))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToUnban = retrieveUserList(client, userToUnban);

        if (usersToUnban.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToUnban}' found.");
            return;
        }

        User user = usersToUnban.getFirst();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (eventUserIID == userIID)
        {
            chat.sendMessage(channelName, "FeelsDankMan You can't crossunban yourself.");
            return;
        }

        HashSet<String> chatLogins = SQLUtils.getChatLogins();
        int unbannedChats = chatLogins.size();

        for (String chatLogin : chatLogins)
        {
            List<User> chatUsers = retrieveUserList(client, chatLogin);
            User chatUser = chatUsers.getFirst();
            String chatUserDisplayName = chatUser.getDisplayName();
            String chatUserID = chatUser.getId();

            IVR ivr = ServiceProvider.getIVRModVip(chatUserDisplayName);
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, "ApuJar");

            if (!selfModeratorPerms)
            {
                unbannedChats--;
                continue;
            }

            try
            {
                TwitchHelix twitchHelix = client.getHelix();
                twitchHelix.unbanUser(null, chatUserID, "896181679", userID).execute();
            }
            catch (Exception ignored)
            {
                unbannedChats--;
            }
        }

        if (unbannedChats == 0)
        {
            chat.sendMessage(channelName, STR."User '\{userToUnban}' isn't banned in any chat that i'm mod in NotLikeThis");
            return;
        }

        chat.sendMessage(channelName, STR."Successfully crossunbanned '\{userToUnban}' from \{unbannedChats} chats LEL");
    }
}
