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
package dev.blocky.twitch.commands.modchecker;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modchecker.ModCheckerBadge;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ModLookupCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToLookup = getParameterUserAsString(messageParts, "-ch(annel)?", eventUserName);

        if (!isValidUsername(userToLookup))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToLookup}' found.");
            return false;
        }

        User user = usersToLookup.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        List<ModCheckerUser> modCheckerUsers = ServiceProvider.getModCheckerUsers(userIID);

        if (modCheckerUsers == null || modCheckerUsers.isEmpty())
        {
            sendChatMessage(channelID, STR."ohh User \{userDisplayName} doesn't get logged by modChecker at the moment or the user opted himself/herself out from the tracking. Please try searching the user FeelsOkayMan \uD83D\uDC49 https://mdc.lol/c");
            return false;
        }

        List<ModCheckerBadge> modCheckerUserBadges = modCheckerUsers.getFirst().getBadges();
        List<String> modCheckerBadges = modCheckerUserBadges.stream().map(ModCheckerBadge::getBadgeName).toList();
        String readableBadges = String.join(", ", modCheckerBadges);

        String messageToSend = null;

        boolean hasChannelParameter = hasRegExParameter(messageParts, "-ch(annel)?");

        if (!hasChannelParameter)
        {
            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerUserMods(userIID);

            if (modCheckerMods == null || modCheckerMods.isEmpty())
            {
                sendChatMessage(channelID, STR."Sadeg No mods for user \{userDisplayName} found.");
                return false;
            }

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long followerCount = 0;

            for (ModCheckerUser mcUser : modCheckerMods)
            {
                followerCount += mcUser.getFollower();

                List<ModCheckerBadge> badges = mcUser.getBadges();

                for (ModCheckerBadge mcBadge : badges)
                {
                    int badgeID = mcBadge.getBadgeID();

                    if (badgeID == 1)
                    {
                        affiliateCount += 1;
                    }

                    if (badgeID == 2)
                    {
                        partnerCount += 1;
                    }

                    if (badgeID == 9)
                    {
                        staffCount += 1;
                    }
                }
            }

            int modCount = modCheckerMods.size();

            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            String followerCountFormatted = decimalFormat.format(followerCount);

            messageToSend = STR."PogChamp \{userDisplayName} is moderator in \{modCount} channel! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCountFormatted}) o_O https://mdc.lol/u/\{userLogin}";

            if (!readableBadges.isBlank())
            {
                messageToSend += STR." - Badges: \{readableBadges}";
            }
        }

        if (hasChannelParameter)
        {
            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(userIID);

            if (modCheckerMods == null || modCheckerMods.isEmpty())
            {
                sendChatMessage(channelID, STR."Sadeg No mods for user \{userDisplayName} found.");
                return false;
            }

            int affiliateCount = 0;
            int partnerCount = 0;
            int staffCount = 0;

            long followerCount = 0;

            for (ModCheckerUser mcUser : modCheckerMods)
            {
                followerCount += mcUser.getFollower();

                List<ModCheckerBadge> badges = mcUser.getBadges();

                for (ModCheckerBadge mcBadge : badges)
                {
                    int badgeID = mcBadge.getBadgeID();

                    if (badgeID == 1)
                    {
                        affiliateCount += 1;
                    }

                    if (badgeID == 2)
                    {
                        partnerCount += 1;
                    }

                    if (badgeID == 9)
                    {
                        staffCount += 1;
                    }
                }
            }

            int modCount = modCheckerMods.size();

            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            String followerCountFormatted = decimalFormat.format(followerCount);

            messageToSend = STR."PogChamp \{userDisplayName} has \{modCount} moderators! (Affiliate: \{affiliateCount}, Partner: \{partnerCount}, Staff: \{staffCount}, Follower: \{followerCountFormatted}) o_O https://mdc.lol/c/\{userLogin}";

            if (!readableBadges.isBlank())
            {
                messageToSend += STR." - Badges: \{readableBadges}";
            }
        }

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
