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

import dev.blocky.twitch.scheduler.job.StreamAwardsJob;
import org.joda.time.DateTime;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.HashSet;

@Deprecated
public class StreamAwardsScheduler
{
    public StreamAwardsScheduler() throws SchedulerException
    {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.start();

        JobDetail job = JobBuilder.newJob(StreamAwardsJob.class)
                .withIdentity("streamawards-2023-job", "streamawards")
                .build();

        DateTime streamAwardsPreStreamTime = new DateTime("2024-02-24T17:00:00+0100");
        DateTime streamAwardsMainShowTime = new DateTime("2024-02-24T19:00:00+0100");

        Date streamAwardsPreStream = streamAwardsPreStreamTime.toDate();
        Date streamAwardsMainShow = streamAwardsMainShowTime.toDate();

        HashSet<Trigger> triggers = new HashSet<>();

        CronScheduleBuilder preStreamCron = CronScheduleBuilder.cronSchedule("0 0,30,50 16 1 SAT 2024");

        CronTrigger preStreamCronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("streamawards-2023-crontrigger-pre", "streamawards")
                .withSchedule(preStreamCron)
                .forJob(job)
                .startNow()
                .build();

        triggers.add(preStreamCronTrigger);

        Trigger preStreamTrigger = TriggerBuilder.newTrigger()
                .withIdentity("streamawards-2023-trigger-pre", "streamawards")
                .startAt(streamAwardsPreStream)
                .forJob(job)
                .build();

        triggers.add(preStreamTrigger);

        CronScheduleBuilder mainShowCron = CronScheduleBuilder.cronSchedule("0 0,30,50 18 1 SAT 2024");

        CronTrigger mainShowCronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("streamawards-2023-crontrigger-main", "streamawards")
                .withSchedule(mainShowCron)
                .forJob(job)
                .startNow()
                .build();

        triggers.add(mainShowCronTrigger);

        Trigger mainShowTrigger = TriggerBuilder.newTrigger()
                .withIdentity("streamawards-2023-trigger-main", "streamawards")
                .startAt(streamAwardsMainShow)
                .forJob(job)
                .build();

        triggers.add(mainShowTrigger);

        scheduler.scheduleJob(job, triggers, false);
    }
}
