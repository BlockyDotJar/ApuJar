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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.entities.seventv.SevenTV;
import dev.blocky.api.entities.seventv.SevenTVData;
import dev.blocky.api.entities.seventv.SevenTVUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SevenTVUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVDenyCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
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
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToDeny = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToDeny))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToDeny = retrieveUserList(client, userToDeny);

        if (usersToDeny.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToDeny}' found.");
            return;
        }

        User user = usersToDeny.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();

        if (!channelName.equals(eventUserName))
        {
            chat.sendMessage(channelName, STR."NiceTry \{eventUserName} but you aren't the broadcaster of this channel.");
            return;
        }

        SevenTV sevenTV = SevenTVUtils.getUser(channelName);
        SevenTVData sevenTVData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVData.getUsers();
        List<SevenTVUser> sevenTVChannelUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, channelName);

        if (sevenTVChannelUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) user with name '\{userDisplayName}' found.");
            return;
        }

        sevenTV = SevenTVUtils.getUser(userDisplayName);
        sevenTVData = sevenTV.getData();
        sevenTVUsers = sevenTVData.getUsers();
        sevenTVChannelUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, userDisplayName);

        if (sevenTVChannelUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) user with name '\{userDisplayName}' found.");
            return;
        }

        String sevenTVAllowedUserIDs = SQLUtils.getSevenTVAllowedUserIDs(channelIID);

        if (sevenTVAllowedUserIDs == null)
        {
            chat.sendMessage(channelName, "Danki Allowed (7TV) user database entry is empty.");
            return;
        }

        String[] allowedUserIDs = sevenTVAllowedUserIDs.split(",");
        ArrayList<String> newAllowedUserIDList = new ArrayList<>();
        String neededUserID = null;

        for (String sevenTVAllowedUserID : allowedUserIDs)
        {
            if (sevenTVAllowedUserID.equals(userID))
            {
                neededUserID = userID;
                break;
            }

            newAllowedUserIDList.add(sevenTVAllowedUserID);
        }

        if (neededUserID == null)
        {
            chat.sendMessage(channelName, STR."Danki \{userDisplayName} isn't even set as allowed user in the database.");
            return;
        }

        if (newAllowedUserIDList.isEmpty())
        {
            SQLite.onUpdate(STR."DELETE FROM sevenTVUsers WHERE userID = \{channelIID}");
            chat.sendMessage(channelName, STR."MEGALUL Successfully removed \{userDisplayName}'s editor permissions.");
            return;
        }

        String newAllowedUserIDs = String.join(",", newAllowedUserIDList);

        SQLite.onUpdate(STR."UPDATE sevenTVUsers SET allowedUserIDs = '\{newAllowedUserIDs}' WHERE userID = \{channelIID}");

        chat.sendMessage(channelName, STR."MEGALUL Successfully removed \{userDisplayName}'s editor permissions.");
    }
}
