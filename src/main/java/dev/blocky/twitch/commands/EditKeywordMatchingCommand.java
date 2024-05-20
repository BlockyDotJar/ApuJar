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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import dev.blocky.twitch.utils.serialization.Keyword;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class EditKeywordMatchingCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a keyword.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a boolean. (Either true or false)");
            return;
        }

        String matchValue = messageParts[2];

        if (!matchValue.matches("^true|false$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid value specified. (Choose between true or false)");
            return;
        }

        boolean exactMatch = Boolean.parseBoolean(matchValue);

        IVR ivr = ServiceProvider.getIVRModVip(channelName);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "ManFeels You can't edit a keyword, because you aren't the broadcaster or moderator.");
            return;
        }

        String kw = messageParts[1];

        Set<Keyword> keywords = SQLUtils.getKeywords(channelIID);

        boolean keywordExists = false;

        for (Keyword keyword : keywords)
        {
            String kwd = keyword.getName();
            boolean kwdExactMatch = keyword.isExactMatch();

            if (kwd.equals(kw))
            {
                keywordExists = true;

                if (kwdExactMatch == exactMatch)
                {
                    sendChatMessage(channelID, STR."4Head The new value for '\{kw}' does exactly match with the old one.");
                    return;
                }

                break;
            }
        }

        if (!keywordExists)
        {
            sendChatMessage(channelID, STR."CoolStoryBob Keyword ' \{kw} ' doesn't exist.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE customKeywords SET exactMatch = \{exactMatch} WHERE userID = \{channelIID} AND name = '\{kw}'");

        sendChatMessage(channelID, STR."SeemsGood Successfully edited keyword exact matching to '\{exactMatch}'.");
    }
}
