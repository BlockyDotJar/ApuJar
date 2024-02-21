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
package dev.blocky.twitch.commands.modscanner;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modscanner.ModScanner;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ModScannerCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToLookup = getParameterUserAsString(messageParts, eventUser);

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

        String messageToSend = null;

        if (Arrays.stream(messageParts).noneMatch("-channel"::equalsIgnoreCase) && Arrays.stream(messageParts).noneMatch("-ch"::equalsIgnoreCase))
        {
            ModScanner modScanner = ServiceProvider.getModScannerUser(userToLookup);

            String userDisplayName = user.getDisplayName();
            String userLogin = user.getLogin();

            int modCount = modScanner.getUserModeratorCount();
            int vipCount = modScanner.getUserVIPCount();
            int founderCount = modScanner.getUserFounderCount();

            messageToSend = STR."PogChamp \{userDisplayName} is moderator in \{modCount}, vip in \{vipCount} and founder in \{founderCount} channel! o_O https://mod.sc/\{userLogin}";
        }

        if (Arrays.stream(messageParts).anyMatch("-channel"::equalsIgnoreCase) || Arrays.stream(messageParts).anyMatch("-ch"::equalsIgnoreCase))
        {
            ModScanner modScanner = ServiceProvider.getModScannerChannel(userToLookup);

            String userDisplayName = user.getDisplayName();

            int modCount = modScanner.getChannelModeratorCount();
            int vipCount = modScanner.getChannelVIPCount();
            int founderCount = modScanner.getChannelFounderCount();

            messageToSend = STR."PogChamp \{userDisplayName} has \{modCount} moderators, \{vipCount} vips and \{founderCount} founder in its channel! o_O https://mod.sc/channel/\{userDisplayName}";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, messageToSend);
    }
}
