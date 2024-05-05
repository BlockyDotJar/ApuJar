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

import dev.blocky.twitch.scheduler.job.AprilFoolsJob;
import org.joda.time.DateTime;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

@Deprecated
public class AprilFoolsScheduler
{
    public AprilFoolsScheduler() throws SchedulerException
    {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.start();

        JobDetail job = JobBuilder.newJob(AprilFoolsJob.class)
                .withIdentity("april-fools-job", "april-fools")
                .build();

        DateTime aprilFoolsStartTime = new DateTime("2024-03-31T21:00:00.000Z");
        Date aprilFoolsStart = aprilFoolsStartTime.toDate();

        CronScheduleBuilder aprilFoolsCron = CronScheduleBuilder.cronSchedule("0 0 0 1 4 ? 2024");

        CronTrigger aprilFoolsCronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("april-fools-crontrigger", "april-fools")
                .withSchedule(aprilFoolsCron)
                .forJob(job)
                .startAt(aprilFoolsStart)
                .build();

        scheduler.scheduleJob(job, aprilFoolsCronTrigger);
    }
}
