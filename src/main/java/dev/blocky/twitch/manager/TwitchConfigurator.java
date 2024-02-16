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
import edu.umd.cs.findbugs.annotations.NonNull;

import java.sql.SQLException;
import java.util.HashSet;

public class TwitchConfigurator
{
    private final HashSet<String> openedChats;
    private final TwitchClient client;

    public TwitchConfigurator( @NonNull TwitchClient client) throws SQLException
    {
        this.openedChats = SQLUtils.getOpenedChats();
        this.client = client;
    }

    public void configure()
    {
        TwitchChat chat = client.getChat();

        chat.sendMessage("ApuJar", "ppCircle Trying to connect to Twitch websocket...");

        int chatCount = 1;

        for (String chatToOpen : openedChats)
        {
            try
            {
                chat.joinChannel(chatToOpen);
                chatCount++;
            }
            catch (Exception e)
            {
                String error = e.getMessage();
                chat.sendMessage("ApuJar", STR."Weird Error while trying to connect to \{chatToOpen}'s chat FeelsGoodMan \{error}");

                e.printStackTrace();
            }
        }

        chat.sendMessage("ApuJar", STR."TriFi Successfully connected to \{chatCount} chats.");
    }
}
