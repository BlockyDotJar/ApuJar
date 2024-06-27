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
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVDenyCommand implements ICommand
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

        String userToDeny = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToDeny))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToDeny = retrieveUserList(client, userToDeny);

        if (usersToDeny.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToDeny}' found.");
            return;
        }

        User user = usersToDeny.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();

        if (!channelName.equals(eventUserName))
        {
            sendChatMessage(channelID, STR."NiceTry \{eventUserName} but you aren't the broadcaster of this channel.");
            return;
        }

        Set<String> sevenTVAllowedUserIDs = SQLUtils.getSevenTVAllowedUserIDs(channelIID);

        if (sevenTVAllowedUserIDs == null)
        {
            sendChatMessage(channelID, "Danki Allowed (7TV) user database entry is empty.");
            return;
        }

        List<String> newAllowedUserIDList = sevenTVAllowedUserIDs.stream()
                .filter(sevenTVAllowedUserID -> !sevenTVAllowedUserID.equals(userID))
                .toList();

        if (!sevenTVAllowedUserIDs.contains(userID))
        {
            sendChatMessage(channelID, STR."Danki \{userDisplayName} isn't even set as allowed user in the database.");
            return;
        }

        if (newAllowedUserIDList.isEmpty())
        {
            SQLite.onUpdate(STR."DELETE FROM sevenTVUsers WHERE userID = \{channelIID}");
            sendChatMessage(channelID, STR."MEGALUL Successfully removed \{userDisplayName}'s editor permissions.");
            return;
        }

        String newAllowedUserIDs = String.join(",", newAllowedUserIDList);

        SQLite.onUpdate(STR."UPDATE sevenTVUsers SET allowedUserIDs = '\{newAllowedUserIDs}' WHERE userID = \{channelIID}");

        sendChatMessage(channelID, STR."MEGALUL Successfully removed \{userDisplayName}'s editor permissions.");
    }
}
