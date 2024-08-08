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
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static dev.blocky.twitch.Main.client;
import static dev.blocky.twitch.utils.TwitchUtils.handleNameChange;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class NameChangeJob implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            HashMap<Integer, String> chatIDToLogin = new HashMap<>();

            Set<Chat> chats = SQLUtils.getChats();
            List<String> chatIDs = chats.stream().map(chat ->
            {
                int chatID = chat.getUserID();
                return String.valueOf(chatID);
            }).toList();

            for (Chat chat : chats)
            {
                String chatLogin = chat.getUserLogin();
                int chatID = chat.getUserID();

                chatIDToLogin.put(chatID, chatLogin);
            }

            List<User> users = TwitchUtils.retrieveUserListByIds(client, chatIDs);

            for (User user : users)
            {
                String userLogin = user.getLogin();
                String userID = user.getId();
                int userIID = Integer.parseInt(userID);

                String oldLogin = chatIDToLogin.get(userIID);

                if (!oldLogin.equalsIgnoreCase(userLogin))
                {
                    handleNameChange(userIID, oldLogin, userLogin);
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Weird Error while trying to handle a name change FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }
}
