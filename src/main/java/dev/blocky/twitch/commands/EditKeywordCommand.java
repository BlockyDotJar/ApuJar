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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Keyword;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class EditKeywordCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify a keyword.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsGoodMan Please specify a message.");
            return;
        }

        List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(channelIID);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(modCheckerMods, eventUserIID);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "ManFeels You can't edit a keyword, because you aren't the broadcaster or a moderator.");
            return;
        }

        String kw = messageParts[1];

        String kwMessageRaw = removeElements(messageParts, 2);
        String kwMessage = handleIllegalCharacters(kwMessageRaw);

        if (kw.startsWith("/") || kwMessage.startsWith("/"))
        {
            sendChatMessage(channelID, "monkaLaugh The keyword/message can't start with a / (slash) haha");
            return;
        }

        Set<Keyword> keywords = SQLUtils.getKeywords(channelIID);

        boolean keywordExists = false;

        for (Keyword keyword : keywords)
        {
            String kwd = keyword.getName();
            String kwdMessage = keyword.getMessage();

            if (kwd.equals(kw))
            {
                keywordExists = true;

                if (kwdMessage.equals(kwMessageRaw))
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

        SQLite.onUpdate(STR."UPDATE customKeywords SET message = '\{kwMessage}' WHERE userID = \{channelIID} AND name = '\{kw}'");

        sendChatMessage(channelID, STR."SeemsGood Successfully edited keyword ' \{kw} '.");
    }
}
