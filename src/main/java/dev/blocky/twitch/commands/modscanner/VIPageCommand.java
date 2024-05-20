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
package dev.blocky.twitch.commands.modscanner;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.api.entities.ivr.IVRModVIP;
import dev.blocky.api.entities.modscanner.ModScanner;
import dev.blocky.api.entities.modscanner.ModScannerUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class VIPageCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsDankMan Please specify a user.");
            return;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUser);

        if (userToCheck.equalsIgnoreCase(eventUserName) && secondUserToCheck.equalsIgnoreCase(eventUserName))
        {
            sendChatMessage(channelID, "DIESOFCRINGE You can't be vip in your own chat.");
            return;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            sendChatMessage(channelID, STR."FeelsDankMan \{userToCheck} can't be vip in his/her own chat.");
            return;
        }

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            sendChatMessage(channelID, "o_O One or both usernames don't match with RegEx R-)");
            return;
        }

        List<User> usersToCheck = retrieveUserList(client, userToCheck);
        List<User> secondsUserToCheck = retrieveUserList(client, secondUserToCheck);

        if (usersToCheck.isEmpty() || secondsUserToCheck.isEmpty())
        {
            sendChatMessage(channelID, ":| One or both users not found.");
            return;
        }

        User user = usersToCheck.getFirst();
        String userLogin = user.getLogin();
        String userDisplayName = user.getDisplayName();

        User secondUser = secondsUserToCheck.getFirst();
        String secondUserLogin = secondUser.getLogin();
        String secondUserDisplayName = secondUser.getDisplayName();

        IVR ivr = ServiceProvider.getIVRModVip(secondUserLogin);
        boolean isVIP = false;

        Date grantedAt = null;

        for (IVRModVIP ivrModVIP : ivr.getVIPs())
        {
            String vipLogin = ivrModVIP.getUserLogin();

            if (!vipLogin.equals(userLogin))
            {
                continue;
            }

            isVIP = true;
            grantedAt = ivrModVIP.getGrantedAt();
            break;
        }

        ModScanner modScanner = ServiceProvider.getModScannerChannel(secondUserLogin);

        for (ModScannerUser msUser : modScanner.getChannelVIPs())
        {
            String vipLogin = msUser.getUserLogin();

            if (!vipLogin.equals(userLogin))
            {
                continue;
            }

            if (grantedAt == null)
            {
                grantedAt = msUser.getGrantedAt();
            }
            break;
        }

        if (!isVIP)
        {
            sendChatMessage(channelID, STR."forsenLaughingAtYou \{userDisplayName} isn't vip in \{secondUserDisplayName}'s chat at the moment.");
            return;
        }

        String readableGrantDate = "(UNKNOWN_GRANT_DATE)";

        if (grantedAt != null)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            readableGrantDate = formatter.format(grantedAt);
        }

        String messageToSend = STR."NOWAYING \{userDisplayName} is vip in \{secondUserDisplayName}'s chat since \{readableGrantDate} PogU";
        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
