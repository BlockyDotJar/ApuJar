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
package dev.blocky.twitch.commands.owner;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class ExitCommand implements ICommand
{
    private final Logger logger = LoggerFactory.getLogger(ExitCommand.class);

    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        sendChatMessage(channelID, "ManFeels Preparing to shutdown...");

        for (int i = 5; i > 0; i--)
        {
            if (i != 1)
            {
                logger.info(STR."Bot stops in \{i} seconds.");
            }

            if (i == 1)
            {
                sendChatMessage(channelID, "GigaSignal Disconnecting from Twitch websocket...");

                logger.info("Bot stops in 1 second.");
            }

            TimeUnit.SECONDS.sleep(1);
        }

        client.close();
        SQLite.disconnect();

        System.exit(0);
    }
}
