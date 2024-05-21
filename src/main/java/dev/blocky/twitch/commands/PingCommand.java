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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.serialization.Chat;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.Duration;
import java.util.Set;

import static dev.blocky.twitch.Main.startedAt;
import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.getActualChannelID;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class PingCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();
        
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        long now = System.currentTimeMillis();
        long uptime = now - startedAt;

        Duration uptimeDuration = Duration.ofMillis(uptime);

        long SS = uptimeDuration.toSecondsPart();
        long MM = uptimeDuration.toMinutesPart();
        long HH = uptimeDuration.toHoursPart();
        long DD = uptimeDuration.toDays();

        long ping = chat.getLatency();

        Set<Chat> chatLogins = SQLUtils.getChats();
        int realChats = chatLogins.size() + 1;

        String messageToSend = STR."ppPong [v2.0.0] WICKED Chat-Ping: \{ping}ms FeelsLateMan I'm active in \{realChats} chats Okay Uptime: \{DD}d \{HH}h \{MM}m \{SS}s FeelsOldMan";
        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
