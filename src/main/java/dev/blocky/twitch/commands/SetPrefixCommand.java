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

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SetPrefixCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        boolean hasCaseInsensitiveParameter = hasRegExParameter(messageParts, "-(cis|case-insensitive)");

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a prefix.");
            return;
        }

        String userPrefixRaw = getParameterAsString(messageParts, "-(cis|case-insensitive)");
        String userPrefix = handleIllegalCharacters(userPrefixRaw);

        Prefix prefix = SQLUtils.getPrefix(channelIID);
        String actualPrefix = prefix.getPrefix();
        boolean isCaseInsensitive = prefix.isCaseInsensitive();

        List<Badge> badges = event.getBadges();
        boolean hasModeratorPerms = badges.stream().map(Badge::getSetId).anyMatch(badgeID -> badgeID.equals("moderator"));

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "NOIDONTTHINKSO You can't set a prefix, because you aren't the broadcaster or a moderator.");
            return;
        }

        if (userPrefix.equals("/"))
        {
            sendChatMessage(channelID, "monkaLaugh The new prefix can't be / (slash) haha");
            return;
        }

        if (actualPrefix.equals(userPrefixRaw))
        {
            sendChatMessage(channelID, "CoolStoryBob The new prefix matches exactly with the old one.");
            return;
        }

        if (!actualPrefix.equals("#") && userPrefix.equals("#"))
        {
            if (!isCaseInsensitive && !hasCaseInsensitiveParameter)
            {
                SQLite.onUpdate(STR."DELETE FROM customPrefixes WHERE userID = \{channelID}");
                sendChatMessage(channelID, "8-) Successfully reseted prefix.");
                return;
            }

            if (isCaseInsensitive)
            {
                SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '#' WHERE userID = \{channelID}");
                sendChatMessage(channelID, "8-) Successfully reseted prefix.");
                return;
            }

            if (hasCaseInsensitiveParameter)
            {
                SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '#', caseInsensitive = TRUE WHERE userID = \{channelID}");
                sendChatMessage(channelID, "8-) Successfully reseted prefix.");
                return;
            }
        }

        if (actualPrefix.equals("#"))
        {
            SQLite.onUpdate(STR."INSERT INTO customPrefixes(userID, prefix, caseInsensitive) VALUES(\{channelID}, '\{userPrefix}', \{hasCaseInsensitiveParameter})");

            sendChatMessage(channelID, STR."8-) Successfully set prefix to \{userPrefixRaw}");
            return;
        }

        if (!hasCaseInsensitiveParameter)
        {
            SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '\{userPrefix}' WHERE userID = \{channelID}");
        }

        if (hasCaseInsensitiveParameter)
        {
            SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '\{userPrefix}', caseInsensitive = TRUE WHERE userID = \{channelID}");
        }

        Prefix newPrefix = SQLUtils.getPrefix(channelIID);
        isCaseInsensitive = newPrefix.isCaseInsensitive();

        sendChatMessage(channelID, STR."8-) Successfully set prefix to ' \{userPrefixRaw} '. (Case-Insensitive: \{isCaseInsensitive})");
    }
}
