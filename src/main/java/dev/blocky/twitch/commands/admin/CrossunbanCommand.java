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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.Main.helix;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CrossunbanCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        String userToUnban = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToUnban))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToUnban = retrieveUserList(client, userToUnban);

        if (usersToUnban.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToUnban}' found.");
            return;
        }

        User user = usersToUnban.getFirst();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (eventUserIID == userIID)
        {
            sendChatMessage(channelID, "FeelsDankMan You can't crossunban yourself.");
            return;
        }

        Set<Chat> chats = SQLUtils.getChats();
        int unbannedChats = chats.size();

        for (Chat chat : chats)
        {
            String chatLogin = chat.getUserLogin();

            List<User> chatUsers = retrieveUserList(client, chatLogin);
            User chatUser = chatUsers.getFirst();
            String chatUserID = chatUser.getId();
            int chatUserIID = Integer.parseInt(chatUserID);

            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(chatUserIID);
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(modCheckerMods, 896181679);

            if (!selfModeratorPerms)
            {
                unbannedChats--;
                continue;
            }

            try
            {
                helix.unbanUser(null, chatUserID, "896181679", userID).execute();
            }
            catch (Exception ignored)
            {
                unbannedChats--;
            }
        }

        if (unbannedChats == 0)
        {
            sendChatMessage(channelID, STR."User '\{userToUnban}' isn't banned in any chat that i'm mod in NotLikeThis");
            return;
        }

        sendChatMessage(channelID, STR."Successfully crossunbanned '\{userToUnban}' from \{unbannedChats} chats LEL");
    }
}
