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
package dev.blocky.twitch.timer;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.HOURS;

public class BotAdditionAndPartageTimer
{
    public BotAdditionAndPartageTimer(@NonNull TwitchClient client)
    {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(() ->
        {
            TwitchChat chat = client.getChat();

            String messageToSend = "BatChest To add me to your chat or to one that you are mod in, use the 'kok!join' command and specify a channel if needed Okay To remove me use 'kok!part' with the specified channel if needed FeelsOkayMan Also check out my commands \uD83D\uDC49 https://blockydotjar.github.io/ApuJar-Website/bot-commands/utility-commands.html";

            chat.sendMessage("ApuJar", messageToSend);
        }, 2, HOURS);
    }
}
