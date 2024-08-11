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
package dev.blocky.twitch.commands.admin;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.NamedUserChatColor;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

import static dev.blocky.twitch.Main.helix;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SetChatColorCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts)
    {
        String channelID = event.getBroadcasterUserId();

        NamedUserChatColor[] namedUserChatColorsRaw = NamedUserChatColor.values();
        List<String> namedUserChatColors = Arrays.stream(namedUserChatColorsRaw)
                .map(nucc ->
                {
                    String nuccNameRaw = nucc.name();
                    String nuccName = nuccNameRaw.replace("_", " ");
                    return nuccName.toLowerCase();
                })
                .toList();

        String namedUserChatColorsReadable = String.join(", ", namedUserChatColors);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, STR."FeelsMan Please specify a color. (Choose between: \{namedUserChatColorsReadable})");
            return;
        }

        String chatColorName = removeElements(messageParts, 1);
        NamedUserChatColor namedUserChatColor = getChatColor(chatColorName);

        if (namedUserChatColor == null)
        {
            sendChatMessage(channelID, STR."FeelsMan Invalid color specified. (Choose between: \{namedUserChatColorsReadable})");
            return;
        }

        String chatColor = namedUserChatColor.getHexCode();

        helix.updateUserChatColor(null, "896181679", chatColor).execute();

        sendChatMessage(channelID, STR."SeemsGood Successfully updated my chat color to \{chatColor}");
    }
}
