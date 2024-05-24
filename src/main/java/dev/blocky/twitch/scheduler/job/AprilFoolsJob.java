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
package dev.blocky.twitch.scheduler.job;

import com.github.twitch4j.chat.TwitchChat;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.Main.client;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

@Deprecated
public class AprilFoolsJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext context)
    {
        try
        {
            Set<String> chatLogins = SQLUtils.getEnabledEventNotificationChatLogins();

            TwitchChat chat = client.getChat();
            Map<String, String> chatIDs = chat.getChannelNameToChannelId();

            for (String chatLogin : chatLogins)
            {
                String chatID = chatIDs.get(chatLogin);

                for (int i = 0; i < 3;i++)
                {
                    sendChatMessage(chatID, STR."DinkDonk \{chatLogin} :tf: Sch\u00F6nen 1. April Tomfoolery SmokeTime");

                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to mass send a message FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }
}
