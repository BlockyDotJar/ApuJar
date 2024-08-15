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
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class DeletePrefixCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        List<Badge> badges = event.getBadges();

        boolean hasModeratorPerms = badges.stream().map(Badge::getSetId).anyMatch(badgeID -> badgeID.equals("moderator"));

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "ManFeels You can't delete a prefix, because you aren't the broadcaster or a moderator.");
            return false;
        }

        Prefix prefix = SQLUtils.getPrefix(channelIID);
        String actualPrefix = prefix.getPrefix();

        if (actualPrefix.equals("#"))
        {
            sendChatMessage(channelID, "CoolStoryBob You don't have a custom prefix.");
            return false;
        }

        SQLite.onUpdate(STR."DELETE FROM customPrefixes WHERE userID = \{channelID}");

        return sendChatMessage(channelID, "8-) Successfully deleted prefix.");
    }
}
