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
package dev.blocky.twitch.manager;

import com.github.philippheuer.events4j.api.IEventManager;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.serialization.PrivateCommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blocky.twitch.utils.TwitchUtils.getFilteredParts;
import static dev.blocky.twitch.utils.TwitchUtils.sendWhisper;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class PrivateCommandManager
{
    public PrivateCommandManager(@NonNull IEventManager eventManager)
    {
        eventManager.onEvent(PrivateMessageEvent.class, this::onPrivateMessage);
    }

    boolean onPrivateMessage(@NonNull String commandOrAlias, @NonNull PrivateMessageEvent event, @NonNull String[] messageParts) throws Exception
    {
        Set<PrivateCommand> privateCommands = SQLUtils.getPrivateCommands();

        for (PrivateCommand privateCmd : privateCommands)
        {
            Set<String> commandAndAliases = privateCmd.getCommandAndAliases();
            boolean commandExists = commandAndAliases.stream().anyMatch(commandOrAlias::equalsIgnoreCase);

            if (commandExists)
            {
                IPrivateCommand privateCommand = privateCmd.getCommandAsClass();
                privateCommand.onPrivateCommand(event, messageParts);
                return true;
            }
        }
        return false;
    }

    public void onPrivateMessage(@NonNull PrivateMessageEvent event)
    {
        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (eventUserIID == 896181679)
        {
            return;
        }

        try
        {
            String message = event.getMessage();

            Prefix prefix = SQLUtils.getPrefix(eventUserIID);
            String actualPrefix = prefix.getPrefix();
            boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

            Pattern PREFIX_PATTERN = Pattern.compile("(.*)?prefix(.*)?", CASE_INSENSITIVE);
            Matcher PREFIX_MATCHER = PREFIX_PATTERN.matcher(message);

            SQLUtils.correctUserLogin(eventUserIID, eventUserName);

            if (PREFIX_MATCHER.matches())
            {
                sendWhisper(eventUserID, STR."4Head The prefix for your chat is '\{actualPrefix}'. (Case-Insensitive: \{caseInsensitivePrefix})");
                return;
            }

            String commandRaw = message.strip();

            String[] messagePartsRaw = commandRaw.split(" ");
            String[] messageParts = getFilteredParts(messagePartsRaw);

            if (messageParts.length > 0)
            {
                String command = messageParts[0];

                if (!command.isBlank())
                {
                    onPrivateMessage(command, event, messageParts);
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendWhisper(eventUserID, STR."SirMad Error while trying to execute an command PogChamp \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }
}
