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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserSpamCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a chat.");
            return;
        }

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a number of messages.");
            return;
        }

        String spamCount = messageParts[2];

        if (!StringUtils.isNumeric(spamCount))
        {
            chat.sendMessage(channelName, "ManFeels The second parameter isn't an integer.");
            return;
        }

        if (messageParts.length == 3)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a message.");
            return;
        }

        int messageCount = Integer.parseInt(spamCount);

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

        if (messageCount > 100 && !ownerIDs.contains(eventUserIID))
        {
            chat.sendMessage(channelName, "ManFeels Number can't be bigger than 100, because you aren't an owner.");
            return;
        }

        String chatToSpam = getUserAsString(messageParts, 1);

        if (!isValidUsername(chatToSpam))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> chatsToSpam = retrieveUserList(client, chatToSpam);

        if (chatsToSpam.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{chatToSpam}' found.");
            return;
        }

        User user = chatsToSpam.getFirst();
        String userDisplayName = user.getDisplayName();

        String messageToSend = removeElements(messageParts, 3);

        if (messageToSend.startsWith("/"))
        {
            if (!ownerIDs.contains(eventUserIID))
            {
                chat.sendMessage(channelName, "DatSheffy You don't have permission to use any kind of / (slash) commands through my account.");
                return;
            }

            IVR ivr = ServiceProvider.getIVRModVip(channelName);
            boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, "ApuJar");

            if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
            {
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because you aren't the broadcaster or moderator.");
                return;
            }

            if (!selfModeratorPerms)
            {
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because i'm not a moderator of this chat.");
                return;
            }
        }

        for (int i = 0; i < messageCount; i++)
        {
            chat.sendMessage(user.getLogin(), messageToSend);
            TimeUnit.MILLISECONDS.sleep(50);
        }

        chat.sendMessage(channelName, STR."SeemsGood Successfully spammed \{messageCount} messages in \{userDisplayName}'s chat.");
    }
}
