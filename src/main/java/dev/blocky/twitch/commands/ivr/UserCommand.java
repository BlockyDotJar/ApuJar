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
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();
        String channelName = event.getChannel().getName();

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());
        String message = getSayableMessage(event.getMessage());

        String[] msgParts = message.split(" ");
        String userToGet = getUserAsString(msgParts, event.getUser());

        if (!isValidUsername(userToGet))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<IVRFI> ivrfiList = ServiceProvider.createIVRFIUser(userToGet);

        if (ivrfiList.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToGet}' found.");
            return;
        }

        IVRFI ivrfi = ivrfiList.getFirst();

        Instant creationInstant = Instant.parse(ivrfi.getCreatedAt());
        Date createDate = Date.from(creationInstant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        String userInfo = "";

        if (ivrfi.isTwitchGlobalBanned())
        {
            String globalBanReason = ivrfi.getTwitchGlobalBanReason();
            userInfo = STR."\u26D4 BANNED \u26D4 (Reason: \{globalBanReason}) ";
        }

        String login = ivrfi.getLogin();
        String displayName = ivrfi.getDisplayName();
        String id = ivrfi.getId();
        String creationDate = formatter.format(createDate);
        String chatColor = ivrfi.getChatColor() == null ? "#FFFFFF" : ivrfi.getChatColor();

        userInfo = STR."\{userInfo} \uD83D\uDC49 Login: \{login}, Display: \{displayName}, ID: \{id}, Created: \{creationDate}, Chat-Color: \{chatColor}";

        ArrayList<JsonNode> badges = ivrfi.getBadges();

        if (!badges.isEmpty())
        {
            String globalBadge = ivrfi.getBadges().getFirst().get("title").asText();
            userInfo = STR."\{userInfo}, Global-Badge: \{globalBadge}";
        }

        if (!ivrfi.isTwitchGlobalBanned())
        {
            int follower = ivrfi.getFollowers();
            int chatterCount = ivrfi.getChatterCount();

            userInfo = STR."\{userInfo}, Follower: \{follower}, Chatter: \{chatterCount}";
        }

        boolean isAffiliate = ivrfi.getRoles().get("isAffiliate").asBoolean();
        boolean isPartner = ivrfi.getRoles().get("isPartner").asBoolean();

        if (isAffiliate || isPartner)
        {
            String broadcasterType = isAffiliate ? "affiliate" : "partner";
            userInfo = STR."\{userInfo}, Broadcaster-Type: \{broadcasterType}";
        }

        boolean isStaff = ivrfi.getRoles().get("isStaff").asBoolean();

        if (isStaff)
        {
            userInfo = STR."\{userInfo}, Type: staff";
        }

        JsonNode lastStream = ivrfi.getLastBroadcast().get("startedAt");

        if (!lastStream.isNull())
        {
            Instant lastStreamInstant = Instant.parse(lastStream.asText());
            Date lastStreamDate = Date.from(lastStreamInstant);
            String startedAt = formatter.format(lastStreamDate);

            userInfo = STR."\{userInfo}, Last-Stream: \{startedAt}";
        }

        channelName = getActualChannel(channelToSend, channelName);

        chat.sendMessage(channelName, userInfo);
    }
}
