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

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import dev.blocky.twitch.Main;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Deprecated
public class AprilFoolsJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext context) throws JobExecutionException
    {
        TwitchClient client = Main.getTwitchClient();
        TwitchChat chat = client.getChat();

        try
        {
            HashSet<String> chatLogins = SQLUtils.getEnabledEventNotificationChatLogins();

            for (String chatLogin : chatLogins)
            {
                for (int i = 0; i < 3;i++)
                {
                    chat.sendMessage(chatLogin, STR."DinkDonk \{chatLogin} :tf: Sch\u00F6nen 1. April Tomfoolery SmokeTime");

                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();
            chat.sendMessage("ApuJar", STR."Weird Error while trying to mass send a message FeelsGoodMan \{error}");

            e.printStackTrace();
        }
    }
}
