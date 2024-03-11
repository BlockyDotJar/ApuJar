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

import static dev.blocky.twitch.utils.SQLUtils.removeApostrophe;

public class SetPrefixCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a prefix.");
            return;
        }

        String rawPrefix = messageParts[1].strip();
        String prefix = removeApostrophe(rawPrefix);
        String actualPrefix = SQLUtils.getActualPrefix(channelID);

        IVR ivr = ServiceProvider.getIVRModVip(channelName);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            chat.sendMessage(channelName, "NOIDONTTHINKSO You can't set a prefix, because you aren't the broadcaster or moderator.");
            return;
        }

        if (actualPrefix.equals(prefix))
        {
            chat.sendMessage(channelName, "CoolStoryBob The new prefix matches exactly with the old one.");
            return;
        }

        if (prefix.isBlank())
        {
            chat.sendMessage(channelName, "monkaLaugh The new prefix can't contain the character ' haha");
            return;
        }

        if (!actualPrefix.equals("kok!") && prefix.equals("kok!"))
        {
            SQLite.onUpdate(STR."DELETE FROM customPrefixes WHERE userID = \{channelID}");

            chat.sendMessage(channelName, "8-) Successfully deleted prefix.");
            return;
        }

        if (actualPrefix.equals("kok!"))
        {
            SQLite.onUpdate(STR."INSERT INTO customPrefixes(userID, prefix) VALUES(\{channelID}, '\{prefix}')");

            chat.sendMessage(channelName, STR."8-) Successfully set prefix to \{prefix}");
            return;
        }

        SQLite.onUpdate(STR."UPDATE customPrefixes SET prefix = '\{prefix}' WHERE userID = \{channelID}");

        chat.sendMessage(channelName, STR."8-) Successfully set prefix to \{prefix}");
    }
}
