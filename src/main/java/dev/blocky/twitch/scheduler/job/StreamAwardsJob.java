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
import org.joda.time.LocalDateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.Main.client;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

@Deprecated
public class StreamAwardsJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext jobExecutionContext)
    {
        try
        {
            Date scheduledFireTime = jobExecutionContext.getScheduledFireTime();
            LocalDateTime fireTime = LocalDateTime.fromDateFields(scheduledFireTime);

            int fireHour = fireTime.getHourOfDay();
            int fireMinute = fireTime.getMinuteOfHour();

            String sentenceBegin = switch (fireHour)
            {
                case 16, 17 -> "Der Stream Awards Pre-Stream beginnt";
                case 18, 19 -> "Die Stream Awards beginnen";
                default -> null;
            };

            int remainingMinutes = 60 - fireMinute;

            String sentenceEnding = switch (fireMinute)
            {
                case 0, 60 ->
                {
                    if (fireHour == 16 || fireHour == 18)
                    {
                        yield "in einer Stunde";
                    }

                    yield "jetzt";
                }
                default -> STR."in \{remainingMinutes} Minuten";
            };

            Set<String> chatLogins = SQLUtils.getEnabledEventNotificationChatLogins();

            TwitchChat chat = client.getChat();
            Map<String, String> chatIDs = chat.getChannelNameToChannelId();

            for (String chatLogin : chatLogins)
            {
                String chatID = chatIDs.get(chatLogin);
                sendChatMessage(chatID, STR."Pag \{sentenceBegin} \{sentenceEnding} PauseChamp \uD83D\uDC49 https://twitch.tv/revedtv");
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
