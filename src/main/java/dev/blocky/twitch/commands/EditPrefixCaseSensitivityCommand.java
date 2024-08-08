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
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class EditPrefixCaseSensitivityCommand implements ICommand
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
            sendChatMessage(channelID, "FeelsMan Please specify a boolean. (Either true or false)");
            return;
        }

        String caseSensitivityValue = messageParts[1];

        if (!caseSensitivityValue.matches("^true|false$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid value specified. (Choose between true or false)");
            return;
        }

        boolean caseInsensitive = Boolean.parseBoolean(caseSensitivityValue);

        Prefix prefix = SQLUtils.getPrefix(channelIID);
        String actualPrefix = prefix.getPrefix();

        List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(channelIID);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(modCheckerMods, eventUserIID);

        if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
        {
            sendChatMessage(channelID, "NOIDONTTHINKSO You can't set the prefix case-sensitivity, because you aren't the broadcaster or a moderator.");
            return;
        }

        if (actualPrefix.equals("#"))
        {
            sendChatMessage(channelID, "NOIDONTTHINKSO You don't even have a custom prefix for this chat.");
            return;
        }

        SQLite.onUpdate(STR."UPDATE customPrefixes SET caseInsensitive = '\{caseInsensitive}' WHERE userID = \{channelID}");

        sendChatMessage(channelID, STR."8-) Successfully edited prefix case-sensitivity to '\{caseInsensitive}'.");
    }
}
