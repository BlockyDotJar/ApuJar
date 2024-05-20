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
package dev.blocky.twitch.commands.admin;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.helix.domain.OutboundFollow;
import com.github.twitch4j.helix.domain.OutboundFollowing;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.TwitchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.blocky.twitch.Main.helix;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UnfollowCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        String userToUnfollow = getUserAsString(messageParts, 1);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan please specify a user.");
            return;
        }

        if (!isValidUsername(userToUnfollow))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToUnfollow = retrieveUserList(client, userToUnfollow);

        if (usersToUnfollow.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToUnfollow}' found.");
            return;
        }

        User user = usersToUnfollow.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (userDisplayName.equalsIgnoreCase("ApuJar"))
        {
            sendChatMessage(channelID, "4Head I can't even follow myself.");
            return;
        }

        OutboundFollowing outboundFollowing = helix.getFollowedChannels(null, "896181679", userID, 100, null).execute();
        List<OutboundFollow> follows = outboundFollowing.getFollows();

        if (follows.isEmpty())
        {
            sendChatMessage(channelID, STR."WHAT I am not even following \{userDisplayName}.");
            return;
        }

        TwitchUtils.unfollowUser(userIID);

        sendChatMessage(channelID, STR."Happi Successfully unfollowed \{userDisplayName}.");
    }
}
