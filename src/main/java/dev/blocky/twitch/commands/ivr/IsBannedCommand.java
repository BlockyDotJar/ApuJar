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
package dev.blocky.twitch.commands.ivr;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVRUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class IsBannedCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        String userToCheck = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToCheck))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<IVRUser> ivrUsers = ServiceProvider.getIVRUser(userToCheck);

        if (ivrUsers.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToCheck}' found.");
            return;
        }

        IVRUser ivrUser = ivrUsers.getFirst();
        String userDisplayName = ivrUser.getUserDisplayName();

        if (!ivrUser.isBanned())
        {
            sendChatMessage(channelID, STR."Saved \{userDisplayName} isn't banned from Twitch at the moment.");
            return;
        }

        String userBanReason = ivrUser.getBanReason();

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, STR."monakS \{userDisplayName} is banned from Twitch \u26D4 (Reason: \{userBanReason}) \u26D4");
    }
}
