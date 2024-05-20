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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
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
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();

        String userToGetURLFrom = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGetURLFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetURLFrom = retrieveUserList(client, userToGetURLFrom);

        if (usersToGetURLFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetURLFrom}' found.");
            return;
        }

        User user = usersToGetURLFrom.getFirst();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userIID);

        if (sevenTVTwitchUser == null)
        {
            return;
        }

        SevenTVUser sevenTVUser = sevenTVTwitchUser.getUser();

        String sevenTVUserDisplayName = sevenTVTwitchUser.getUserDisplayName();
        String sevenTVUserID = sevenTVUser.getUserID();

        sendChatMessage(channelID, STR."SeemsGood Here is your 7tv user link for \{sevenTVUserDisplayName} \uD83D\uDC49 https://7tv.app/users/\{sevenTVUserID}");
    }
}
