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
package dev.blocky.twitch.manager;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class TwitchConfigurator
{
    private final Set<Chat> chats;
    private final IEventSubSocket eventSocket;
    private final EventManager eventManager;
    private final TwitchChat chat;

    public TwitchConfigurator(@NonNull IEventSubSocket eventSocket, @NonNull EventManager eventManager, @NonNull TwitchChat chat)
    {
        this.chats = SQLUtils.getChats();
        this.eventSocket = eventSocket;
        this.eventManager = eventManager;
        this.chat = chat;
    }

    public void configure()
    {
        sendChatMessage("896181679", "ppCircle Trying to connect to Twitch websocket...");

        int chatCount = 0;

        ChannelJoinManager joinManager = new ChannelJoinManager(eventSocket, eventManager);

        for (Chat ch : chats)
        {
            String chatLogin = ch.getUserLogin();
            int chatID = ch.getUserID();

            try
            {
                if (!chatLogin.equalsIgnoreCase("ApuJar"))
                {
                    chat.joinChannel(chatLogin);
                }

                joinManager.joinChannel(chatID);

                chatCount++;
            }
            catch (Exception e)
            {
                String error = e.getMessage();

                Class<?> clazz = e.getClass();
                String clazzName = clazz.getName();

                if (chatCount >= 100)
                {
                    sendChatMessage(chatID, "heyy Because i'm in over 100 channels, it is possible, that i am not able to look into your chat anymore due to some Twitch API changes https://discuss.dev.twitch.com/t/54997 You might need to give moderator permission to me to work properly FeelsOkayMan");
                }

                sendChatMessage("896181679", STR."Weird Error while trying to connect to \{chatLogin}'s chat FeelsGoodMan \{error} (\{clazzName})");

                e.printStackTrace();
            }
        }

        sendChatMessage("896181679", STR."TriFi Successfully connected to \{chatCount} chats.");
    }
}
