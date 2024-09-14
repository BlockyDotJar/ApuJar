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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.*;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SubageCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return false;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            sendChatMessage(channelID, "o_O One or both usernames aren't matching with RegEx R-)");
            return false;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            sendChatMessage(channelID, ":| One or both users not found.");
            return false;
        }

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        User secondUser = secondUsers.getFirst();
        String secondUserLogin = secondUser.getLogin();
        String secondUserDisplayName = secondUser.getDisplayName();
        String secondUserBroadcasterType = secondUser.getBroadcasterType();

        if (secondUserBroadcasterType.isEmpty())
        {
            sendChatMessage(channelID, STR."ManFeels \{secondUserDisplayName} isn't even an affiliate or partner.");
            return false;
        }

        IVRSubage ivrSubage = ServiceProvider.getIVRSubage(userLogin, secondUserLogin);
        IVRSubageCumulative ivrCumulativeSubage = ivrSubage.getCumulativeSubage();
        IVRSubageMeta ivrSubageMeta = ivrSubage.getSubageMeta();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        if (ivrSubageMeta == null)
        {
            String messageToSend = STR."Bad \{userDisplayName} isn't subscribing to \{secondUserDisplayName} at the moment NotLikeThis";

            if (ivrCumulativeSubage != null)
            {
                int cumulativeSubMonths = ivrCumulativeSubage.getSubMonths();

                Date subEnd = ivrCumulativeSubage.getSubEnd();
                String readableEnd = formatter.format(subEnd);

                messageToSend += STR." (Subscribed for \{cumulativeSubMonths} month";

                if (cumulativeSubMonths > 1)
                {
                    messageToSend += "s";
                }

                messageToSend += STR." before, Ended: \{readableEnd})";
            }

            return sendChatMessage(channelID, messageToSend);
        }

        Date endsAt = ivrSubageMeta.getEndsAt();
        String readableEndsAt = formatter.format(endsAt);

        String subType = ivrSubageMeta.getSubType();
        int subTier = ivrSubageMeta.getSubTier();

        String messageToSend = STR."Strong \{userDisplayName} is subscribing to \{secondUserDisplayName}";

        if (subType.equals("paid"))
        {
            messageToSend += STR." with a tier \{subTier} sub";
        }

        int cumulativeSubMonths = ivrCumulativeSubage.getSubMonths();

        messageToSend += STR." since \{cumulativeSubMonths} month";

        if (cumulativeSubMonths > 1)
        {
            messageToSend += "s";
        }

        messageToSend += STR." (Ends: \{readableEndsAt}";

        IVRSubageStreak ivrSubageStreak = ivrSubage.getSubageStreak();

        if (ivrSubageStreak != null)
        {
            int subStreakMonths = ivrSubageStreak.getSubStreakMonths();

            messageToSend += STR.", Streak: \{subStreakMonths} month";

            if (subStreakMonths > 1)
            {
                messageToSend += "s";
            }
        }

        Date renewsAt = ivrSubageMeta.getRenewsAt();

        if (renewsAt != null)
        {
            String readableRenewsAt = formatter.format(renewsAt);
            messageToSend += STR.", Renews: \{readableRenewsAt}";
        }

        messageToSend += ")";

        if (subType.equals("gift"))
        {
            IVRSubageGiftMeta ivrSubGiftMeta = ivrSubageMeta.getSubGiftMeta();
            IVRSubageGifter ivrSubGifter = ivrSubGiftMeta.getSubGifter();
            String gifterDisplayName = ivrSubGifter.getUserDisplayName();

            Date giftDate = ivrSubGiftMeta.getGiftDate();
            String readableGiftDate = formatter.format(giftDate);

            messageToSend += STR." \{gifterDisplayName} was so nice and gifted a tier \{subTier} sub to \{userDisplayName} on \{readableGiftDate}";
        }

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
