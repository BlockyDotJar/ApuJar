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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVRUser;
import dev.blocky.api.entities.ivr.IVRUserBadge;
import dev.blocky.api.entities.ivr.IVRUserRoles;
import dev.blocky.api.entities.ivr.IVRUserStream;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToGet = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToGet))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<IVRUser> ivrUsers = ServiceProvider.getIVRUser(userToGet);

        if (ivrUsers.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGet}' found.");
            return;
        }

        IVRUser ivrUser = ivrUsers.getFirst();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date creationDate = ivrUser.getCreatedAt();
        String readableCreationDate = formatter.format(creationDate);

        String userInfo = "";

        if (ivrUser.isBanned())
        {
            String banReason = ivrUser.getBanReason();
            userInfo = STR."\u26D4 BANNED \u26D4 (Reason: \{banReason}) ";
        }

        String userLogin = ivrUser.getUserLogin();
        String userDisplayName = ivrUser.getUserDisplayName();
        String userID = ivrUser.getUserID();
        String userChatColor = ivrUser.getUserChatColor() == null ? "#FFFFFF" : ivrUser.getUserChatColor();

        userInfo = STR."\{userInfo} \uD83D\uDC49 Login: \{userLogin}, Display: \{userDisplayName}, ID: \{userID}, Created: \{readableCreationDate}, Chat-Color: \{userChatColor}";

        ArrayList<IVRUserBadge> ivrUserBadges = ivrUser.getUserBadges();

        if (!ivrUserBadges.isEmpty())
        {
            IVRUserBadge ivrUserBadge = ivrUserBadges.getFirst();
            String badgeName = ivrUserBadge.getBadgeName();
            userInfo = STR."\{userInfo}, Global-Badge: \{badgeName}";
        }

        if (!ivrUser.isBanned())
        {
            int userFollowers = ivrUser.getUserFollowers();
            int chatterCount = ivrUser.getChatterCount();

            userInfo = STR."\{userInfo}, Follower: \{userFollowers}, Chatter: \{chatterCount}";
        }

        IVRUserRoles ivrUserRoles = ivrUser.getUserRoles();

        boolean isAffiliate = ivrUserRoles.isAffiliate();
        boolean isPartner = ivrUserRoles.isPartner();

        if (isAffiliate || isPartner)
        {
            String broadcasterType = isAffiliate ? "affiliate" : "partner";
            userInfo = STR."\{userInfo}, Broadcaster-Type: \{broadcasterType}";
        }

        boolean isStaff = ivrUserRoles.isStaff();

        if (isStaff)
        {
            userInfo = STR."\{userInfo}, Type: staff";
        }

        IVRUserStream ivrUserStream = ivrUser.getLastBroadcast();
        Date startedAt = ivrUserStream.getStartedAt();

        if (startedAt != null)
        {
            String readableStartedAt = formatter.format(startedAt);
            userInfo = STR."\{userInfo}, Last-Stream: \{readableStartedAt}";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, userInfo);
    }
}
