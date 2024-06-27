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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class LoveCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts)
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();

        String userToLookup = getUserAsString(messageParts, eventUserName);
        String secondUserToLookup = getSecondUserAsString(messageParts, eventUserName);

        if ((eventUserName.equalsIgnoreCase(userToLookup) && eventUserName.equalsIgnoreCase(secondUserToLookup)) || userToLookup.equalsIgnoreCase(secondUserToLookup))
        {
            secondUserToLookup = "himself/herself";
        }

        if (!userToLookup.equalsIgnoreCase(eventUserName) && messageParts.length == 2)
        {
            String tempUserToLookup = secondUserToLookup;

            secondUserToLookup = userToLookup;
            userToLookup = tempUserToLookup;
        }

        Random random = new Random();
        int love = random.nextInt(0, 100);

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, STR."peepoLove \{userToLookup} loves \{secondUserToLookup} \{love}%.");
    }
}
