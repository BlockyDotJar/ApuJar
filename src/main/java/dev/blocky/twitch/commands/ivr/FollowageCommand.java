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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVRSubage;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class FollowageCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUser);

        if (userToCheck.equalsIgnoreCase(eventUserName) && secondUserToCheck.equals(eventUserName))
        {
            chat.sendMessage(channelName, "FeelsMan You can't follow yourself.");
            return;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            chat.sendMessage(channelName, STR."FeelsDankMan \{userToCheck} can't follow hisself/herself.");
            return;
        }

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            chat.sendMessage(channelName, "o_O One or both usernames aren't matching with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            chat.sendMessage(channelName, ":| One or both users not found.");
            return;
        }

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();

        User secondUser = secondUsers.getFirst();
        String secondUserDisplayName = secondUser.getDisplayName();
        String secondUserLogin = secondUser.getLogin();

        IVRSubage ivrSubage = ServiceProvider.getIVRSubage(userDisplayName, secondUserLogin);
        Date followedAt = ivrSubage.getFollowedAt();

        if (followedAt == null)
        {
            chat.sendMessage(channelName, STR."Bad \{userDisplayName} isn't following \{secondUserDisplayName} at the moment.");
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String readableFollowedAt = formatter.format(followedAt);

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, STR."Strong \{userDisplayName} follows \{secondUserDisplayName} since \{readableFollowedAt} Gladge");
    }
}
