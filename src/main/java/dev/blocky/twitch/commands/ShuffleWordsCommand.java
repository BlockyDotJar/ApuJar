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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.blocky.twitch.utils.TwitchUtils.removeElementsAsArray;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class ShuffleWordsCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts)
    {
        String channelID = event.getBroadcasterUserId();

        if (messageParts.length < 3)
        {
            sendChatMessage(channelID, "FeelsDankMan Please specify at least 2 words.");
            return false;
        }

        String[] textPartsToDankifyRaw = removeElementsAsArray(messageParts, 1);
        List<String> textPartsToDankify = Arrays.stream(textPartsToDankifyRaw).collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(textPartsToDankify);

        String dankifiedText = String.join(" ", textPartsToDankify);

        return sendChatMessage(channelID, STR."Here is your new dank text FeelsOkayMan \uD83D\uDC49 \{dankifiedText}");
    }
}
