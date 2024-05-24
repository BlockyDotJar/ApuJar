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
import dev.blocky.api.entities.modscanner.ModScanner;
import dev.blocky.api.entities.modscanner.ModScannerUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.ModScannerFlags.*;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class FounderLookupCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();

        String userToLookup = getParameterUserAsString(messageParts, "-ch(annel)?", eventUser);

        if (!isValidUsername(userToLookup))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToLookup}' found.");
            return;
        }

        User user = usersToLookup.getFirst();
        String userLogin = user.getLogin();
        String userDisplayName = user.getDisplayName();
        String userBroadcasterType = user.getBroadcasterType();

        if (!userBroadcasterType.equals("affiliate") && !userBroadcasterType.equals("partner"))
        {
            sendChatMessage(channelID, STR."ManFeels \{userDisplayName} isn't even an affiliate or partner.");
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        String messageToSend = null;

        boolean hasChannelParameter = hasRegExParameter(messageParts, "-ch(annel)?");

        if (!hasChannelParameter)
        {
            ModScanner modScanner = ServiceProvider.getModScannerUser(userToLookup);

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long follower = 0;

            for (ModScannerUser msUser : modScanner.getUserFounders())
            {
                follower += msUser.getUserFollowers();

                int flags = msUser.getFlags();

                if (TWITCH_AFFILIATE.isInFlag(flags))
                {
                    affiliateCount += 1;
                }

                if (TWITCH_PARTNER.isInFlag(flags))
                {
                    partnerCount += 1;
                }

                if (TWITCH_STAFF.isInFlag(flags))
                {
                    staffCount += 1;
                }
            }

            String followerCount = decimalFormat.format(follower);
            int founderCount = modScanner.getUserFounderCount();

            messageToSend = STR."PogChamp \{userDisplayName} is founder in \{founderCount} channel! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCount}) o_O https://mod.sc/\{userLogin}";
        }

        if (hasChannelParameter)
        {
            ModScanner modScanner = ServiceProvider.getModScannerChannel(userToLookup);

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long follower = 0;

            for (ModScannerUser msUser : modScanner.getChannelFounders())
            {
                follower += msUser.getUserFollowers();

                int flags = msUser.getFlags();

                if (TWITCH_AFFILIATE.isInFlag(flags))
                {
                    affiliateCount += 1;
                }

                if (TWITCH_PARTNER.isInFlag(flags))
                {
                    partnerCount += 1;
                }

                if (TWITCH_STAFF.isInFlag(flags))
                {
                    staffCount += 1;
                }
            }

            String followerCount = decimalFormat.format(follower);
            int founderCount = modScanner.getChannelFounderCount();

            messageToSend = STR."PogChamp \{userDisplayName} has \{founderCount} founder! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCount}) o_O https://mod.sc/channel/\{userDisplayName}";
        }

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
