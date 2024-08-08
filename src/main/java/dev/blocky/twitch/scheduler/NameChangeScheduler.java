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
package dev.blocky.twitch.scheduler;

import dev.blocky.twitch.scheduler.job.NameChangeJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class NameChangeScheduler
{
    public NameChangeScheduler() throws SchedulerException
    {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.start();

        JobDetail job = JobBuilder.newJob(NameChangeJob.class)
                .withIdentity("name-change-job", "name-change")
                .build();

        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(1)
                .repeatForever();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("name-change-trigger", "name-change")
                .withSchedule(scheduleBuilder)
                .forJob(job)
                .startNow()
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
