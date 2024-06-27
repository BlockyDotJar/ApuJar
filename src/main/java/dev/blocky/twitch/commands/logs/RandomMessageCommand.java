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
package dev.blocky.twitch.commands.logs;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class RandomMessageCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String userToCheck = getChannelAsString(messageParts, channelName);

        if (!isValidUsername(userToCheck))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToCheck = retrieveUserList(client, userToCheck);

        if (usersToCheck.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToCheck}' found.");
            return;
        }

        User user = usersToCheck.getFirst();
        String userLogin = user.getLogin();

        String randomMessage = ServiceProvider.getRandomMessage(channelIID, userLogin);

        if (randomMessage == null)
        {
            return;
        }

        String[] randomMessageParts = randomMessage.split(" ");

        String sentAt = randomMessage.substring(1, 20);
        String datePart = sentAt.substring(0, 10);
        String timePart = sentAt.substring(11);

        String[] dateParts = datePart.split("-");
        String year = dateParts[0];
        String month = dateParts[1];
        String day = dateParts[2];

        String sentAtReadable = STR."\{day}.\{month}.\{year} \{timePart}";

        String sender = randomMessageParts[3];
        String message = removeElements(randomMessageParts, 4);

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, STR."FeelsOkayMan \uD83D\uDC49 Sent on \{sentAtReadable} from \{sender} \{message}");
    }
}
