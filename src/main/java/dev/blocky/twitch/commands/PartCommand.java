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
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.getUserAsString;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class PartCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        String chatToPart = getUserAsString(messageParts, eventUser);

        Map<Integer, String> owners = SQLUtils.getOwners();
        Collection<String> ownerLogins = owners.values();

        if (chatToPart.equalsIgnoreCase("ApuJar") || ownerLogins.contains(chatToPart))
        {
            sendChatMessage(channelID, "TriHard \u270A I'll stay here.");
            return;
        }

        if (!chatToPart.equalsIgnoreCase(eventUserName))
        {
            IVR ivr = ServiceProvider.getIVRModVip(chatToPart);
            boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

            Map<Integer, String> admins = SQLUtils.getAdmins();
            Set<Integer> adminIDs = admins.keySet();

            Set<Integer> ownerIDs = owners.keySet();

            if (messageParts.length > 1 && (!hasModeratorPerms && !adminIDs.contains(eventUserIID) && !ownerIDs.contains(eventUserIID)))
            {
                sendChatMessage(channelID, "ManFeels Can't leave channel, because you aren't broadcaster or mod at this channel.");
                return;
            }
        }

        if (!chat.isChannelJoined(chatToPart))
        {
            sendChatMessage(channelID, STR."CoolStoryBob I'm not even in \{chatToPart}'s chat.");
            return;
        }

        chat.leaveChannel(chatToPart);

        SQLite.onUpdate(STR."DELETE FROM chats WHERE userLogin = '\{chatToPart}'");

        sendChatMessage(channelID, STR."MrDestructoid Successfully left from \{chatToPart}'s chat.");
    }
}

