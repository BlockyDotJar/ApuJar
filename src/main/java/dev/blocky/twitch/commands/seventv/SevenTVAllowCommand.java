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

public class SevenTVAllowCommand implements ICommand
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

        String userToAllow = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToAllow))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToAllow = retrieveUserList(client, userToAllow);

        if (usersToAllow.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToAllow}' found.");
            return;
        }

        User user = usersToAllow.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (!channelName.equals(eventUserName))
        {
            chat.sendMessage(channelName, STR."NiceTry \{eventUserName} but you aren't the broadcaster of this channel.");
            return;
        }

        SevenTV sevenTV = SevenTVUtils.getUser(userDisplayName);
        SevenTVData sevenTVEventUserData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVEventUsers = sevenTVEventUserData.getUsers();
        List<SevenTVUser> sevenTVEventUsersSorted = sevenTVEventUsers.stream()
                .filter(sevenTVEventUser ->
                {
                    String sevenTVUsername = sevenTVEventUser.getUsername();
                    return sevenTVUsername.equalsIgnoreCase(channelName);
                })
                .toList();

        if (sevenTVEventUsersSorted.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No user with name '\{userDisplayName}' found.");
            return;
        }

        SevenTVUser sevenTVEventUser = sevenTVEventUsersSorted.getFirst();
        String sevenTVEventUserID = sevenTVEventUser.getID();

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(chat, channelIID, userIID, sevenTVEventUserID, channelName, eventUserName);

        if (isAllowedEditor)
        {
            chat.sendMessage(channelName, STR."WHAT \{userDisplayName} is already able to update emotes.");
            return;
        }

        String sevenTVAllowedUserIDs = SQLUtils.getSevenTVAllowedUserIDs(channelIID);

        if (sevenTVAllowedUserIDs == null)
        {
            SQLite.onUpdate(STR."INSERT INTO sevenTVUsers(userID, allowedUserIDs) VALUES(\{channelIID}, '\{userID})'");
            chat.sendMessage(channelName, STR."POGGERS Successfully added \{userDisplayName} as editor.");
            return;
        }

        String newAllowedUserIDs = STR."\{sevenTVAllowedUserIDs},\{userIID}";

        SQLite.onUpdate(STR."UPDATE sevenTVUsers SET allowedUserIDs = '\{newAllowedUserIDs}' WHERE userID = \{channelIID}");

        chat.sendMessage(channelName, STR."POGGERS Successfully added \{userDisplayName} as editor.");
    }
}
