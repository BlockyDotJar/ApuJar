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
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;

import static dev.blocky.twitch.utils.TwitchUtils.removeElements;

public class SayCommand implements ICommand
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
            chat.sendMessage(channelName, "FeelsMan Please specify a message.");
            return;
        }

        String messageToSend = removeElements(messageParts, 1);

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

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
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because you aren't the broadcaster or a moderator.");
                return;
            }

            if (!selfModeratorPerms && !channelName.equalsIgnoreCase("ApuJar"))
            {
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because i'm not the broadcaster or a moderator.");
                return;
            }
        }

        chat.sendMessage(channelName, messageToSend);
    }
}
