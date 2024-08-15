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
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class PartCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        String chatToPart = getUserAsString(messageParts, eventUserName);

        Map<Integer, String> owners = SQLUtils.getOwners();
        Collection<String> ownerLogins = owners.values();

        if (chatToPart.equalsIgnoreCase("ApuJar") || ownerLogins.contains(chatToPart))
        {
            sendChatMessage(channelID, "TriHard \u270A I'll stay here.");
            return false;
        }

        List<User> chatsToPart = retrieveUserList(client, chatToPart);
        User user = chatsToPart.getFirst();
        String userID = user.getId();

        if (!chatToPart.equalsIgnoreCase(eventUserName))
        {
            List<Badge> badges = event.getBadges();

            boolean hasModeratorPerms = badges.stream().map(Badge::getSetId).anyMatch(badgeID -> badgeID.equals("moderator"));

            Map<Integer, String> admins = SQLUtils.getAdmins();
            Set<Integer> adminIDs = admins.keySet();

            Set<Integer> ownerIDs = owners.keySet();

            if (messageParts.length > 1 && (!hasModeratorPerms && !adminIDs.contains(eventUserIID) && !ownerIDs.contains(eventUserIID)))
            {
                sendChatMessage(channelID, "ManFeels Can't leave channel, because you aren't broadcaster or mod at this channel.");
                return false;
            }
        }

        Set<Chat> chats = SQLUtils.getChats();

        boolean isInChannel = chats.stream().anyMatch(ch ->
        {
            String userLogin = ch.getUserLogin();
            return userLogin.equals(chatToPart);
        });

        if (!isInChannel)
        {
            sendChatMessage(channelID, STR."CoolStoryBob I'm not even in \{chatToPart}'s chat.");
            return false;
        }

        IEventSubSocket eventSocket = client.getEventSocket();

        eventSocket.unregister(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription
                (
                        builder -> builder.broadcasterUserId(userID).userId("896181679").build(),
                        null
                ));

        chat.leaveChannel(chatToPart);

        SQLite.onUpdate(STR."DELETE FROM chats WHERE userLogin = '\{chatToPart}'");

        return sendChatMessage(channelID, STR."MrDestructoid Successfully left from \{chatToPart}'s chat.");
    }
}

