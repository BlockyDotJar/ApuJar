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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.lilb.LiLBChatter;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class IsInChatCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return false;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            sendChatMessage(channelID, "o_O One or both usernames aren't matching with RegEx R-)");
            return false;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            sendChatMessage(channelID, ":| One or both users not found.");
            return false;
        }

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        User secondUser = secondUsers.getFirst();
        String secondUserDisplayName = secondUser.getDisplayName();
        String secondUserLogin = secondUser.getLogin();

        LiLBChatter lilbChatter = ServiceProvider.getChatter(secondUserLogin);

        if (lilbChatter == null)
        {
            sendChatMessage(channelID, "FeelsOkayMan lilb API server error. dink lilb_lxryer");
            return false;
        }

        List<String> chatters = lilbChatter.getChatters();

        boolean isInChat = chatters.stream().anyMatch(userLogin::equalsIgnoreCase);

        channelID = getActualChannelID(channelToSend, channelID);

        if (!isInChat)
        {
            sendChatMessage(channelID, STR."mhm \{userDisplayName} is not in \{secondUserDisplayName}'s chat.");
            return false;
        }

        return sendChatMessage(channelID, STR."Susge \{userDisplayName} is currently in \{secondUserDisplayName}'s chat.");
    }
}
