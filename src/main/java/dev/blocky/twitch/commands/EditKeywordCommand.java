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
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

import static dev.blocky.twitch.utils.SQLUtils.removeApostrophe;
import static dev.blocky.twitch.utils.TwitchUtils.removeElements;

public class EditKeywordCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a keyword.");
            return;
        }

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsGoodMan Please specify a message.");
            return;
        }

        IVR ivr = ServiceProvider.getIVRModVip(channelName);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            chat.sendMessage(channelName, "ManFeels You can't edit a keyword, because you aren't a broadcaster or moderator.");
            return;
        }

        String kwRaw = messageParts[1].strip();
        String kwMessageRaw = removeElements(messageParts, 2);

        String kw = removeApostrophe(kwRaw);
        String kwMessage = removeApostrophe(kwMessageRaw);

        if (kw.isBlank() || kwMessage.isBlank())
        {
            chat.sendMessage(channelName, "monkaLaugh The keyword/message can't contain the character ' haha");
            return;
        }

        List<Triple<String, String, Boolean>> keywords = SQLUtils.getKeywords(channelIID);

        boolean keywordExists = false;

        for (Triple<String, String, Boolean> keyword : keywords)
        {
            String kwd = keyword.getLeft();
            String kwdMessage = keyword.getMiddle();

            if (kwd.equals(kw))
            {
                keywordExists = true;

                if (kwdMessage.equals(kwMessage))
                {
                    chat.sendMessage(channelName, STR."4Head The new value for '\{kw}' does exactly match with the old one.");
                    return;
                }

                break;
            }
        }

        if (!keywordExists)
        {
            chat.sendMessage(channelName, STR."CoolStoryBob Keyword ' \{kw} ' doesn't exist.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE customKeywords SET message = '\{kwMessage}' WHERE userID = \{channelIID} AND name = '\{kw}'");

        chat.sendMessage(channelName, STR."SeemsGood Successfully edited keyword ' \{kw} '.");
    }
}
