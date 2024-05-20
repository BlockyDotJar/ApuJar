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

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.serialization.Chat;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.sql.SQLException;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class TwitchConfigurator
{
    private final Set<Chat> chats;
    private final TwitchClient client;

    public TwitchConfigurator(@NonNull TwitchClient client) throws SQLException
    {
        this.chats = SQLUtils.getChats();
        this.client = client;
    }

    public void configure()
    {
        TwitchChat chat = client.getChat();

        sendChatMessage("896181679", "ppCircle Trying to connect to Twitch websocket...");

        int chatCount = 1;

        for (Chat ch : chats)
        {
            String chatLogin = ch.getUserLogin();
            int chatID = ch.getUserID();

            try
            {
                chat.joinChannel(chatLogin);
                chatCount++;
            }
            catch (Exception e)
            {
                String error = e.getMessage();

                if (chatCount >= 100)
                {
                    sendChatMessage(chatID, "heyy Because i'm in over 100 channels, it is possible, that i am not able to look into your chat anymore due to some Twitch API changes https://links.blockyjar.dev/m8mrybgQKR1 You might need to give moderator permission to me to work properly FeelsOkayMan");
                }

                sendChatMessage("896181679", STR."Weird Error while trying to connect to \{chatLogin}'s chat FeelsGoodMan \{error}");

                e.printStackTrace();
            }
        }

        sendChatMessage("896181679", STR."TriFi Successfully connected to \{chatCount} chats.");
    }
}
