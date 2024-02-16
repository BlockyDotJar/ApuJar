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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.api.entities.ModScanner;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class VIPageCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsDankMan Please specify a user.");
            return;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUser);

        if (userToCheck.equalsIgnoreCase(eventUserName) && secondUserToCheck.equalsIgnoreCase(eventUserName))
        {
            chat.sendMessage(channelName, "DIESOFCRINGE You can't be vip in your own chat.");
            return;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            chat.sendMessage(channelName, STR."FeelsDankMan \{userToCheck} can't be vip in his/her own chat.");
            return;
        }

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            chat.sendMessage(channelName, "o_O One or both usernames don't match with RegEx R-)");
            return;
        }

        List<User> usersToCheck = retrieveUserList(client, userToCheck);
        List<User> secondsUserToCheck = retrieveUserList(client, secondUserToCheck);

        if (usersToCheck.isEmpty() || secondsUserToCheck.isEmpty())
        {
            chat.sendMessage(channelName, ":| One or both users not found.");
            return;
        }

        User user = usersToCheck.getFirst();
        String userLogin = user.getLogin();
        String userDisplayName = user.getDisplayName();

        User secondUser = secondsUserToCheck.getFirst();
        String secondUserLogin = secondUser.getLogin();
        String secondUserDisplayName = secondUser.getDisplayName();

        IVRFI ivrfi = ServiceProvider.createIVRFIModVip(secondUserLogin);
        boolean isVIP = false;

        String grantedAt = null;

        for (JsonNode vip : ivrfi.getVips())
        {
            String vipLogin = vip.get("login").asText();

            if (!vipLogin.equals(userLogin))
            {
                continue;
            }

            isVIP = true;
            grantedAt = vip.get("grantedAt").asText();
            break;
        }

        ModScanner modScanner = ServiceProvider.createModScannerChannel(secondUserLogin);

        for (JsonNode vip : modScanner.getChannelVIPs())
        {
            String vipLogin = vip.get("login").asText();

            if (!vipLogin.equals(userLogin))
            {
                continue;
            }

            if (grantedAt.equals("null"))
            {
                grantedAt = vip.get("grantedAt").asText();
            }
            break;
        }

        if (!isVIP)
        {
            chat.sendMessage(channelName, STR."forsenLaughingAtYou \{userDisplayName} is not vip in \{secondUserDisplayName}'s chat at the moment.");
            return;
        }

        String readableGrantDate = "(UNKNOWN_GRANT_DATE)";

        if (!grantedAt.equals("null"))
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

            Instant grantInstant = Instant.parse(grantedAt);
            Date grantDate = Date.from(grantInstant);
            readableGrantDate = formatter.format(grantDate);
        }

        String messageToSend = STR."NOWAYING \{userDisplayName} is vip in \{secondUserDisplayName}'s chat since \{readableGrantDate} PogU";
        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, messageToSend);
    }
}
