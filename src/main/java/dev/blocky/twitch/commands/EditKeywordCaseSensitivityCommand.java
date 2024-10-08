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
import dev.blocky.twitch.serialization.Keyword;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class EditKeywordCaseSensitivityCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a keyword.");
            return false;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a boolean. (Either true or false)");
            return false;
        }

        String caseSensitivityValue = messageParts[2];

        if (!caseSensitivityValue.matches("^true|false$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid value specified. (Choose between true or false)");
            return false;
        }

        boolean caseInsensitive = Boolean.parseBoolean(caseSensitivityValue);

        List<Badge> badges = event.getBadges();
        boolean hasModeratorPerms = badges.stream().map(Badge::getSetId).anyMatch(badgeID -> badgeID.equals("moderator"));

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "ManFeels You can't edit the keyword case-sensitivity, because you aren't the broadcaster or a moderator.");
            return false;
        }

        String kw = messageParts[1];

        Set<Keyword> keywords = SQLUtils.getKeywords(channelIID);

        boolean keywordExists = false;

        for (Keyword keyword : keywords)
        {
            String kwd = keyword.getName();
            boolean kwdCaseInsensitive = keyword.isCaseInsensitive();

            if (kwd.equals(kw))
            {
                keywordExists = true;

                if (kwdCaseInsensitive == caseInsensitive)
                {
                    sendChatMessage(channelID, STR."4Head The new case-sensitivity for '\{kw}' does exactly match with the old one.");
                    return false;
                }

                break;
            }
        }

        if (!keywordExists)
        {
            sendChatMessage(channelID, STR."CoolStoryBob Keyword ' \{kw} ' doesn't exist.");
            return false;
        }

        SQLite.onUpdate(STR."UPDATE customKeywords SET caseInsensitive = \{caseInsensitive} WHERE userID = \{channelIID} AND name = '\{kw}'");

        return sendChatMessage(channelID, STR."SeemsGood Successfully edited keyword case-sensitivity to '\{caseInsensitive}'.");
    }
}
