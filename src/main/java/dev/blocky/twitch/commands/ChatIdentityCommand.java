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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ChatIdentityCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToGetIdentityFrom = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGetIdentityFrom))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetIdentityFrom = retrieveUserList(client, userToGetIdentityFrom);

        if (usersToGetIdentityFrom.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGetIdentityFrom}' found.");
            return;
        }

        User user = usersToGetIdentityFrom.getFirst();
        String displayName = user.getDisplayName();
        String login = user.getLogin();

        String messageToSend = STR."CollectThemAll \{displayName}'s badges and 7tv paints can be found and tested here PogU \uD83D\uDC49 https://vanity.zonian.dev/?u=\{login}";
        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, messageToSend);
    }
}
