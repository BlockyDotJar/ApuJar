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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class LoveCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts)
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        String userToLookup = getUserAsString(messageParts, eventUser);
        String secondUserToLookup = getSecondUserAsString(messageParts, eventUser);

        if ((eventUserName.equalsIgnoreCase(userToLookup) && eventUserName.equalsIgnoreCase(secondUserToLookup)) || userToLookup.equalsIgnoreCase(secondUserToLookup))
        {
            secondUserToLookup = "himself/herself";
        }

        if (!userToLookup.equalsIgnoreCase(eventUserName) && secondUserToLookup.equalsIgnoreCase(eventUserName))
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
