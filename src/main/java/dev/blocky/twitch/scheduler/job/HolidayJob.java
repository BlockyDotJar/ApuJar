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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

@Deprecated
public class HolidayJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext context)
    {
        sendChatMessage("896181679", "peepoSummer BlockyDotJar is not at home from 29.06.2024 - 03.07.2024 because he is Lounging in Berchtesgaden Gladge (Der hs lässt mich hier ganz alleine schuften UltraMad ) If you need him, set him a reminder with Susgee bot, dm him on discord or send him a dm on Twitch Okayeg");
    }
}
