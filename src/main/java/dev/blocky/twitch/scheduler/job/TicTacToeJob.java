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

import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static dev.blocky.twitch.Main.client;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class TicTacToeJob implements Job
{
    @Override
    public void execute(@NonNull JobExecutionContext context)
    {
        try
        {
            Map<Integer, LocalDateTime> ticTacToeStartTimes = SQLUtils.getTicTacToeStartTimes();

            for (int channelID : ticTacToeStartTimes.keySet())
            {
                LocalDateTime startedAt = ticTacToeStartTimes.get(channelID);

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime tenMinutesAgo = now.minusMinutes(10);

                if (startedAt.isBefore(tenMinutesAgo))
                {
                    SQLite.onUpdate(STR."DELETE FROM tictactoe WHERE userID = \{channelID}");

                    List<User> users = TwitchUtils.retrieveUserListByID(client, channelID);

                    User user = users.getFirst();
                    String userID = user.getId();

                    sendChatMessage(userID, "Waiting Deleted tictactoe round, beacause it lasted to long Okay");
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
