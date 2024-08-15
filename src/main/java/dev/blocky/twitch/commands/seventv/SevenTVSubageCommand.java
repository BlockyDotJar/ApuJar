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
package dev.blocky.twitch.commands.seventv;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.SevenTVSubage;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVSubageCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToCheck = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToCheck))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToCheck = retrieveUserList(client, userToCheck);

        if (usersToCheck.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToCheck}' found.");
            return false;
        }

        User user = usersToCheck.getFirst();
        String userDisplayName = user.getDisplayName();

        SevenTVSubage sevenTVSubage = ServiceProvider.getSevenTVSubage(userToCheck);

        if (sevenTVSubage == null)
        {
            sendChatMessage(channelID, STR."Bad \{userDisplayName} isn't subscribing to (7TV) at the moment NotLikeThis");
            return false;
        }

        String statusRaw = sevenTVSubage.getStatus();
        String status = statusRaw.toLowerCase();

        String unitRaw = sevenTVSubage.getUnit();
        String unit = unitRaw.toLowerCase();

        int age = sevenTVSubage.getAge();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Date startedAt = sevenTVSubage.getStartedAt();
        String readableStartedAt = formatter.format(startedAt);

        Date endsAt = sevenTVSubage.getEndAt();
        String readableEndsAt = formatter.format(endsAt);

        String messageToSend = STR."Strong \{userDisplayName} is subscribing to (7TV) \{unit}ly with an \{status} sub since \{age} days (Started: \{readableStartedAt}, Ends: \{readableEndsAt}";

        boolean willRenew = sevenTVSubage.willRenew();

        if (willRenew)
        {
            messageToSend += STR.", Renews: \{readableEndsAt}";
        }

        messageToSend += ")";

        String giftedBy = sevenTVSubage.getGiftedBy();

        if (giftedBy != null)
        {
            messageToSend += STR." \{giftedBy} was so nice and gifted a sub for a \{unit} to \{userDisplayName} on \{readableStartedAt}";
        }

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
