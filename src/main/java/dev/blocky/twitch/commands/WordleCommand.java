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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.wordle.Wordle;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class WordleCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate now = LocalDate.now();

        String wordleDay = now.format(formatter);

        if (messageParts.length > 1)
        {
            wordleDay = removeElements(messageParts, 1);

            if (!wordleDay.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$"))
            {
                sendChatMessage(channelID, "no Date schema must match to DD.MM.YYYY (e.g. 21.04.2024)");
                return false;
            }
        }

        String[] dateParts = wordleDay.split("\\.");

        String dayRaw = dateParts[0];
        String monthRaw = dateParts[1];
        String yearRaw = dateParts[2];

        int day = Integer.parseInt(dayRaw);
        int month = Integer.parseInt(monthRaw);
        int year = Integer.parseInt(yearRaw);

        LocalDate wordleRelease = LocalDate.of(2021, 6, 19);
        LocalDate wordleDate = LocalDate.of(year, month, day);

        if (wordleDate.isBefore(wordleRelease))
        {
            sendChatMessage(channelID, "LULE Wordle was released on 19.06.2021");
            return false;
        }

        if (wordleDate.isAfter(now))
        {
            sendChatMessage(channelID, "LULE That day lies in the future.");
            return false;
        }

        Wordle wordle = ServiceProvider.getWordle(yearRaw, monthRaw, dayRaw);

        int id = wordle.getID();
        int daysSinceLaunch = wordle.getDaysSinceLaunch();

        String solution = wordle.getSolution();
        String editor = wordle.getEditor();

        if (editor == null)
        {
            editor = "unknown";
        }

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, STR."Nerd Your needed wordle (\{id}) answer is '\{solution}'. (Added by \{editor} | \{daysSinceLaunch} days since launch)");
    }
}
