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
import dev.blocky.api.entities.seventv.SevenTVUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVUserCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();

        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String userToGetURLFrom = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToGetURLFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToGetURLFrom = retrieveUserList(client, userToGetURLFrom);

        if (usersToGetURLFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetURLFrom}' found.");
            return false;
        }

        User user = usersToGetURLFrom.getFirst();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userIID);

        if (sevenTVTwitchUser == null)
        {
            return false;
        }

        SevenTVUser sevenTVUser = sevenTVTwitchUser.getUser();

        String sevenTVUserDisplayName = sevenTVTwitchUser.getUserDisplayName();
        String sevenTVUserID = sevenTVUser.getUserID();

        return sendChatMessage(channelID, STR."SeemsGood Here is your 7tv user link for \{sevenTVUserDisplayName} \uD83D\uDC49 https://7tv.app/users/\{sevenTVUserID}");
    }
}
