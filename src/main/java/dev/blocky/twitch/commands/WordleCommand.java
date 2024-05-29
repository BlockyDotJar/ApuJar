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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.wordle.Wordle;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class WordleCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        LocalDate now = LocalDate.now();

        String wordleDay = formatter.format(now);

        if (messageParts.length > 1)
        {
            wordleDay = removeElements(messageParts, 1);

            if (!wordleDay.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$"))
            {
                sendChatMessage(channelID, "no Date schema must match to DD.MM.YYYY (e.g. 21.04.2024)");
                return;
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
            return;
        }

        if (wordleDate.isAfter(now))
        {
            sendChatMessage(channelID, "LULE That day lies in the future.");
            return;
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

        sendChatMessage(channelID, STR."Nerd Your needed wordle (\{id}) answer is '\{solution}'. (Added by \{editor} | \{daysSinceLaunch} days since launch)");
    }
}
