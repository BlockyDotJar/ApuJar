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
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToLookup = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToLookup))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToLookup}' found.");
            return;
        }

        User user = usersToLookup.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        LiLBChatter lilbChatter = ServiceProvider.getChatter(channelName);
        List<String> chatters = lilbChatter.getChatters();

        boolean isInChat = chatters.stream().anyMatch(userLogin::equalsIgnoreCase);

        channelName = getActualChannel(channelToSend, channelName);

        if (isInChat)
        {
            chat.sendMessage(channelName, STR."mhm \{userDisplayName} is not in this chat.");
            return;
        }

        chat.sendMessage(channelName, STR."Susge \{userDisplayName} is currently in this chat.");
    }
}
