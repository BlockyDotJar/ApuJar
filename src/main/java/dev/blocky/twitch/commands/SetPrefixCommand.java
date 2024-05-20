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
import edu.umd.cs.findbugs.annotations.NonNull;

import static dev.blocky.twitch.utils.TwitchUtils.removeIllegalCharacters;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SetPrefixCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify a prefix.");
            return;
        }

        String prefixRaw = messageParts[1];
        String prefix = removeIllegalCharacters(prefixRaw);
        String actualPrefix = SQLUtils.getPrefix(channelIID);

        IVR ivr = ServiceProvider.getIVRModVip(channelName);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "NOIDONTTHINKSO You can't set a prefix, because you aren't the broadcaster or moderator.");
            return;
        }

        if (prefix.equals("/"))
        {
            sendChatMessage(channelID, "monkaLaugh The new prefix can't be / (slash) haha");
            return;
        }

        if (actualPrefix.equals(prefix))
        {
            sendChatMessage(channelID, "CoolStoryBob The new prefix matches exactly with the old one.");
            return;
        }

        if (prefix.isBlank())
        {
            sendChatMessage(channelID, "monkaLaugh The new prefix can't contain the character ' haha");
            return;
        }

        if (!actualPrefix.equals("#") && prefix.equals("#"))
        {
            SQLite.onUpdate(STR."DELETE FROM customPrefixes WHERE userID = \{channelID}");

            sendChatMessage(channelID, "8-) Successfully deleted prefix.");
            return;
        }

        if (actualPrefix.equals("#"))
        {
            SQLite.onUpdate(STR."INSERT INTO customPrefixes(userID, prefix) VALUES(\{channelID}, '\{prefix}')");

            sendChatMessage(channelID, STR."8-) Successfully set prefix to \{prefix}");
            return;
        }

        SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '\{prefix}' WHERE userID = \{channelID}");

        sendChatMessage(channelID, STR."8-) Successfully set prefix to \{prefix}");
    }
}
