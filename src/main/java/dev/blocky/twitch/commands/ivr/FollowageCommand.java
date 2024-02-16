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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
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

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());
        String message = getSayableMessage(event.getMessage());

        String[] msgParts = message.split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a user to check.");
            return;
        }

        String userToCheck = getUserAsString(msgParts, 1);
        String secondUserToCheck = getSecondUserAsString(msgParts, event.getUser());

        if (userToCheck.equalsIgnoreCase(event.getUser().getName().toLowerCase()) && secondUserToCheck.equalsIgnoreCase(event.getUser().getName().toLowerCase()))
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan You can't follow yourself");
            return;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            chat.sendMessage(event.getChannel().getName(), STR."FeelsDankMan \{userToCheck} can't follow hisself/herself.");
            return;
        }

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            chat.sendMessage(event.getChannel().getName(), "o_O One or both usernames aren't matching with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), ":| One or both users not found.");
            return;
        }

        User user = users.getFirst();
        User secondUser = secondUsers.getFirst();

        IVRFI ivrfi = ServiceProvider.createIVRFISubAge(user.getDisplayName(), secondUser.getLogin());

        if (ivrfi.getFollowedAt() == null)
        {
            chat.sendMessage(event.getChannel().getName(), STR."Bad \{user.getDisplayName()} is not following \{secondUser.getDisplayName()} at the moment.");
            return;
        }

        Instant creationInstant = Instant.parse(ivrfi.getFollowedAt());
        Date followDate = Date.from(creationInstant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        String channelName = getActualChannel(channelToSend, event.getChannel().getName());

        chat.sendMessage(channelName, STR."Strong \{user.getDisplayName()} follows \{secondUser.getDisplayName()} since \{formatter.format(followDate)} Gladge");
    }
}
