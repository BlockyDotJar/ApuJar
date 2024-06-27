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
package dev.blocky.twitch.commands.seventv;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SevenTVUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVAllowCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        String userToAllow = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToAllow))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToAllow = retrieveUserList(client, userToAllow);

        if (usersToAllow.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToAllow}' found.");
            return;
        }

        User user = usersToAllow.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (!channelName.equals(eventUserName))
        {
            sendChatMessage(channelID, STR."NiceTry \{eventUserName} but you aren't the broadcaster of this channel.");
            return;
        }

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userIID);

        if (sevenTVTwitchUser == null)
        {
            return;
        }

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(channelIID, userIID);

        if (isAllowedEditor)
        {
            sendChatMessage(channelID, STR."WHAT \{userDisplayName} is already able to update emotes.");
            return;
        }

        Set<String> sevenTVAllowedUserIDs = SQLUtils.getSevenTVAllowedUserIDs(channelIID);

        if (sevenTVAllowedUserIDs == null)
        {
            SQLite.onUpdate(STR."INSERT INTO sevenTVUsers(userID, allowedUserIDs) VALUES(\{channelIID}, '\{userID}')");
            sendChatMessage(channelID, STR."POGGERS Successfully added \{userDisplayName} as editor.");
            return;
        }

        String sevenTVAllowedUserIDsFormatted = String.join(",", sevenTVAllowedUserIDs);
        String newAllowedUserIDs = STR."\{sevenTVAllowedUserIDsFormatted},\{userIID}";

        SQLite.onUpdate(STR."UPDATE sevenTVUsers SET allowedUserIDs = '\{newAllowedUserIDs}' WHERE userID = \{channelIID}");

        sendChatMessage(channelID, STR."POGGERS Successfully added \{userDisplayName} as editor.");
    }
}
