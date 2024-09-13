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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
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
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts)
    {
        IEventSubSocket eventSocket = client.getEventSocket();
        TwitchChat chat = client.getChat();
        
        String channelID = event.getBroadcasterUserId();

        long now = System.currentTimeMillis();
        long uptime = now - startedAt;

        Duration uptimeDuration = Duration.ofMillis(uptime);

        long SS = uptimeDuration.toSecondsPart();
        long MM = uptimeDuration.toMinutesPart();
        long HH = uptimeDuration.toHoursPart();
        long DD = uptimeDuration.toDays();

        long chatPing = chat.getLatency();
        long eventSubPing = eventSocket.getLatency();

        Set<Chat> chatLogins = SQLUtils.getChats();
        int realChats = chatLogins.size();

        String messageToSend = STR."ppPong [v3.5.0] WICKED IRC-Ping: \{chatPing}ms EventSub-Ping: \{eventSubPing}ms FeelsLateMan I'm active in \{realChats} chats Okay Uptime: \{DD}d \{HH}h \{MM}m \{SS}s FeelsOldMan";
        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
