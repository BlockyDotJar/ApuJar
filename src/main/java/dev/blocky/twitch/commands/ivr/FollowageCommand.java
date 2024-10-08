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
package dev.blocky.twitch.commands.ivr;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVRSubage;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class FollowageCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return false;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUserName);

        if (userToCheck.equalsIgnoreCase(eventUserName) && secondUserToCheck.equals(eventUserName))
        {
            sendChatMessage(channelID, "FeelsMan You can't follow yourself.");
            return false;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            sendChatMessage(channelID, STR."FeelsDankMan \{userToCheck} can't follow hisself/herself.");
            return false;
        }

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

        IVRSubage ivrSubage = ServiceProvider.getIVRSubage(userLogin, secondUserLogin);
        Date followedAt = ivrSubage.getFollowedAt();

        if (followedAt == null)
        {
            return sendChatMessage(channelID, STR."Bad \{userDisplayName} isn't following \{secondUserDisplayName} at the moment.");
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String readableFollowedAt = formatter.format(followedAt);

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, STR."Strong \{userDisplayName} follows \{secondUserDisplayName} since \{readableFollowedAt} Gladge");
    }
}
