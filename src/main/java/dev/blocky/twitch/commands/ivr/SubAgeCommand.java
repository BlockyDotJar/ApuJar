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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SubAgeCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());
        String message = getSayableMessage(event.getMessage());

        String[] msgParts = message.split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a user to check.");
            return;
        }

        String userToCheck = getUserAsString(msgParts, 1);
        String secondUserToCheck = getSecondUserAsString(msgParts, event.getUser());

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            chat.sendMessage(event.getChannel().getName(), "o_O One or both usernames aren't matching with RegEx R-)");
            return;
        }

        List<User> users = retrieveUserList(client, userToCheck);
        List<User> secondUsers = retrieveUserList(client, secondUserToCheck);

        if (users.isEmpty() || secondUsers.isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), ":| One or both users not found.");
            return;
        }

        User user = users.getFirst();
        User secondUser = secondUsers.getFirst();

        if (secondUser.getBroadcasterType().isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), STR."ManFeels \{secondUser.getDisplayName()} is not even an affiliate or partner.");
            return;
        }

        IVRFI ivrfi = ServiceProvider.createIVRFISubAge(user.getDisplayName(), secondUser.getLogin());

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        if (ivrfi.getMeta().isNull())
        {
            String messageToSend = STR."Bad \{user.getDisplayName()} is not subscribing to \{secondUser.getDisplayName()} at the moment NotLikeThis";

            if (!ivrfi.getCumulative().isNull())
            {
                Instant endInstant = Instant.parse(ivrfi.getCumulative().get("end").asText());
                Date endDate = Date.from(endInstant);

                messageToSend = STR."\{messageToSend} (Subscribed for \{ivrfi.getCumulative().get("months").asInt()} month";

                if (ivrfi.getCumulative().get("months").asInt() > 1)
                {
                    messageToSend = STR."\{messageToSend}s";
                }

                messageToSend =  STR."\{messageToSend} before, Ended: \{formatter.format(endDate)})";
            }

            chat.sendMessage(event.getChannel().getName(), messageToSend);
            return;
        }

        Instant endInstant = Instant.parse(ivrfi.getMeta().get("endsAt").asText());
        Date endDate = Date.from(endInstant);

        String messageToSend = STR."Strong \{user.getDisplayName()} is subscribing to \{secondUser.getDisplayName()}";

        if (ivrfi.getMeta().get("type").asText().equals("paid"))
        {
            messageToSend = STR."\{messageToSend} with a tier \{ivrfi.getMeta().get("tier").asInt()} sub";
        }

        messageToSend = STR."\{messageToSend} since \{ivrfi.getCumulative().get("months").asInt()} month";

        if (ivrfi.getCumulative().get("months").asInt() > 1)
        {
            messageToSend = STR."\{messageToSend}s";
        }

        messageToSend = STR."\{messageToSend} (Ends: \{formatter.format(endDate)}";

        if (!ivrfi.getStreak().isNull())
        {
            int months = ivrfi.getStreak().get("months").asInt();

            messageToSend = STR."\{messageToSend}, Streak: \{months} month";

            if (months > 1)
            {
                messageToSend = STR."\{messageToSend}s";
            }
        }

        if (!ivrfi.getMeta().get("renewsAt").isNull())
        {
            Instant renewInstant = Instant.parse(ivrfi.getMeta().get("renewsAt").asText());
            Date renewDate = Date.from(renewInstant);

            messageToSend = STR."\{messageToSend}, Renews: \{formatter.format(renewDate)}";
        }

        messageToSend = STR."\{messageToSend})";

        if (ivrfi.getMeta().get("type").asText().equals("gift"))
        {
            JsonNode giftMeta = ivrfi.getMeta().get("giftMeta");
            JsonNode gifter = giftMeta.get("gifter");

            Instant giftInstant = Instant.parse(giftMeta.get("giftDate").asText());
            Date giftDate = Date.from(giftInstant);

            messageToSend = STR."\{messageToSend} \{gifter.get("displayName").asText()} was so nice and gifted a tier \{ivrfi.getMeta().get("tier").asInt()} sub to \{user.getDisplayName()} on \{formatter.format(giftDate)}";
        }

        String channelName = getActualChannel(channelToSend, event.getChannel().getName());

        chat.sendMessage(channelName, messageToSend);
    }
}
