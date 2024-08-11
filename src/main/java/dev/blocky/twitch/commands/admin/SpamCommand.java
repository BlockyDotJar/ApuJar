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
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.removeElements;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SpamCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a number of messages.");
            return;
        }
        
        String spamCount = messageParts[1];

        if (!StringUtils.isNumeric(spamCount))
        {
            sendChatMessage(channelID, "ManFeels The first parameter isn't an integer.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a message.");
            return;
        }

        int messageCount = Integer.parseInt(spamCount);

        Map<Integer, String> owners = SQLUtils.getOwners();
        Set<Integer> ownerIDs = owners.keySet();

        if (messageCount > 100 && !ownerIDs.contains(eventUserIID))
        {
            sendChatMessage(channelID, "ManFeels Number can't be bigger than 100, because you aren't an owner.");
            return;
        }

        String messageToSend = removeElements(messageParts, 2);
        
        if (messageToSend.startsWith("/"))
        {
            if (!ownerIDs.contains(eventUserIID))
            {
                sendChatMessage(channelID, "DatSheffy You don't have permission to use any kind of / (slash) commands through my account.");
                return;
            }

            List<Badge> badges = event.getBadges();
            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(channelIID);

            boolean hasModeratorPerms = badges.stream().map(Badge::getSetId).anyMatch(badgeID -> badgeID.equals("moderator"));
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(modCheckerMods, 896181679);

            if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
            {
                sendChatMessage(channelID, "ManFeels You can't use / (slash) commands, because you aren't the broadcaster or a moderator.");
                return;
            }

            if (!selfModeratorPerms)
            {
                sendChatMessage(channelID, "ManFeels You can't use / (slash) commands, because i'm not a moderator of this chat.");
                return;
            }
        }

        for (int i = 0; i < messageCount; i++)
        {
            sendChatMessage(channelID, messageToSend);
            TimeUnit.MILLISECONDS.sleep(50);
        }

        sendChatMessage(channelID, STR."SeemsGood Successfully spammed \{messageCount} messages.");
    }
}
