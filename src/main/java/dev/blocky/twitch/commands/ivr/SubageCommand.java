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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.*;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SubageCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUser);

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            chat.sendMessage(channelName, "o_O One or both usernames aren't matching with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            chat.sendMessage(channelName, ":| One or both users not found.");
            return;
        }

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();

        User secondUser = secondUsers.getFirst();
        String secondUserLogin = secondUser.getLogin();
        String secondUserDisplayName = secondUser.getDisplayName();
        String secondUserBroadcasterType = secondUser.getBroadcasterType();

        if (secondUserBroadcasterType.isEmpty())
        {
            chat.sendMessage(channelName, STR."ManFeels \{secondUserDisplayName} is not even an affiliate or partner.");
            return;
        }

        IVRSubage ivrSubage = ServiceProvider.getIVRSubage(userDisplayName, secondUserLogin);
        IVRSubageCumulative ivrSubageCumulative = ivrSubage.getCumulative();
        IVRSubageMeta ivrSubageMeta = ivrSubage.getMeta();

        int cumulativeMonths = ivrSubageCumulative.getMonths();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        if (ivrSubageMeta == null)
        {
            String messageToSend = STR."Bad \{userDisplayName} is not subscribing to \{secondUserDisplayName} at the moment NotLikeThis";

            if (ivrSubageCumulative != null)
            {
                Date end = ivrSubageCumulative.getEnd();
                String readableEnd = formatter.format(end);

                messageToSend = STR."\{messageToSend} (Subscribed for \{cumulativeMonths} month";

                if (cumulativeMonths > 1)
                {
                    messageToSend = STR."\{messageToSend}s";
                }

                messageToSend =  STR."\{messageToSend} before, Ended: \{readableEnd})";
            }

            chat.sendMessage(channelName, messageToSend);
            return;
        }

        Date endsAt = ivrSubageMeta.getEndsAt();
        String readableEndsAt = formatter.format(endsAt);

        String type = ivrSubageMeta.getType();
        int tier = ivrSubageMeta.getTier();

        String messageToSend = STR."Strong \{userDisplayName} is subscribing to \{secondUserDisplayName}";

        if (type.equals("paid"))
        {
            messageToSend = STR."\{messageToSend} with a tier \{tier} sub";
        }

        messageToSend = STR."\{messageToSend} since \{cumulativeMonths} month";

        if (cumulativeMonths > 1)
        {
            messageToSend = STR."\{messageToSend}s";
        }

        messageToSend = STR."\{messageToSend} (Ends: \{readableEndsAt}";

        IVRSubageStreak ivrSubageStreak = ivrSubage.getStreak();

        if (ivrSubageStreak != null)
        {
            int streakMonths = ivrSubageStreak.getMonths();

            messageToSend = STR."\{messageToSend}, Streak: \{streakMonths} month";

            if (streakMonths > 1)
            {
                messageToSend = STR."\{messageToSend}s";
            }
        }

        Date renewsAt = ivrSubageMeta.getRenewsAt();

        if (renewsAt != null)
        {
            String readableRenewsAt = formatter.format(renewsAt);
            messageToSend = STR."\{messageToSend}, Renews: \{readableRenewsAt}";
        }

        messageToSend = STR."\{messageToSend})";

        if (type.equals("gift"))
        {
            IVRSubageGiftMeta ivrSubageGiftMeta = ivrSubageMeta.getGiftMeta();
            IVRSubageGifter ivrSubageGifter = ivrSubageGiftMeta.getGifter();
            String gifterDisplayName = ivrSubageGifter.getDisplayName();

            Date giftDate = ivrSubageGiftMeta.getGiftDate();
            String readableGiftDate = formatter.format(giftDate);

            messageToSend = STR."\{messageToSend} \{gifterDisplayName} was so nice and gifted a tier \{tier} sub to \{userDisplayName} on \{readableGiftDate}";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, messageToSend);
    }
}
