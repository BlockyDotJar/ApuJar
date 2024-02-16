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
import dev.blocky.api.entities.ModScanner;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ModLookupCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        String userToLookup = getParameterUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToLookup))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToLookup}' found.");
            return;
        }

        User user = usersToLookup.getFirst();

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        String messageToSend = null;

        if (Arrays.stream(messageParts).noneMatch("-channel"::equalsIgnoreCase) && Arrays.stream(messageParts).noneMatch("-ch"::equalsIgnoreCase))
        {
            ModScanner modScanner = ServiceProvider.createModScannerUser(userToLookup);

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long follower = 0;

            for (JsonNode mod : modScanner.getUserModerators())
            {
                follower += mod.get("followers").asInt();

                String modLogin = mod.get("login").asText();

                List<User> modUsers = retrieveUserList(client, modLogin);
                User modUser = modUsers.getFirst();

                String broadcasterType = modUser.getBroadcasterType();
                String type = user.getType();

                if (broadcasterType.equals("affiliate"))
                {
                    affiliateCount += 1;
                }

                if (broadcasterType.equals("partner"))
                {
                    partnerCount += 1;
                }

                if (type.equals("staff"))
                {
                    staffCount += 1;
                }
            }

            String userDisplayName = user.getDisplayName();
            String userLogin = user.getLogin();

            String followerCount = decimalFormat.format(follower);
            int modCount = modScanner.getUserModeratorCount();

            messageToSend = STR."PogChamp \{userDisplayName} is moderator in \{modCount} channel! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCount}) o_O https://mod.sc/\{userLogin}";
        }

        if (Arrays.stream(messageParts).anyMatch("-channel"::equalsIgnoreCase) || Arrays.stream(messageParts).anyMatch("-ch"::equalsIgnoreCase))
        {
            ModScanner modScanner = ServiceProvider.createModScannerChannel(userToLookup);

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long follower = 0;

            for (JsonNode mod : modScanner.getChannelModerators())
            {
                follower += mod.get("followers").asInt();

                String modLogin = mod.get("login").asText();

                List<User> moderators = retrieveUserList(client, modLogin);
                User moderator = moderators.getFirst();

                String broadcasterType = moderator.getBroadcasterType();
                String type = user.getType();

                if (broadcasterType.equals("affiliate"))
                {
                    affiliateCount += 1;
                }

                if (broadcasterType.equals("partner"))
                {
                    partnerCount += 1;
                }

                if (type.equals("staff"))
                {
                    staffCount += 1;
                }
            }

            String userDisplayName = user.getDisplayName();

            String followerCount = decimalFormat.format(follower);
            int modCount = modScanner.getChannelModeratorCount();

            messageToSend = STR."PogChamp \{userDisplayName} has \{modCount} moderators! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCount}) o_O https://mod.sc/channel/\{userDisplayName}";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, messageToSend);
    }
}
