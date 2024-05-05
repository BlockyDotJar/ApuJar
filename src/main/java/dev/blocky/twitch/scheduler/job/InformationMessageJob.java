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
import edu.umd.cs.findbugs.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class InformationMessageJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext context) throws JobExecutionException
    {
        TwitchClient client = Main.getTwitchClient();
        TwitchChat chat = client.getChat();

        chat.sendMessage("ApuJar", "BatChest To add me to your chat or to one that you are mod in, use the '#join' command and specify a channel if needed Okay To remove me use '#part' with the specified channel if needed FeelsOkayMan Also check out my commands \uD83D\uDC49 https://apujar.blockyjar.dev/commands/utility.html Okay By adding me to your chat, you agree with our Privacy Policy ( https://apujar.blockyjar.dev/legal/privacy-policy.html ) and our ToS ( https://apujar.blockyjar.dev/legal/terms-of-service.html ) Okayeg");
    }
}
