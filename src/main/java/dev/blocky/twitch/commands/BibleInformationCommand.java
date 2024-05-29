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
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.blockyjar.BlockyJarBibleEntry;
import dev.blocky.api.entities.blockyjar.BlockyJarUser;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class BibleInformationCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a bible page.");
            return;
        }

        String pageRaw = messageParts[1];

        if (!pageRaw.matches("^\\d+$"))
        {
            sendChatMessage(channelID, "oop Specified value isn't a number.");
            return;
        }

        int page = Integer.parseInt(pageRaw);

        if (page <= 0)
        {
            sendChatMessage(channelID, "oop Number can't be equal to 0 or negative.");
            return;
        }

        BlockyJarBibleEntry bibleEntry = ServiceProvider.getBibleEntry(channelIID, page);

        if (bibleEntry == null)
        {
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Date addedAt = bibleEntry.getAddedAt();
        Date updatedAt = bibleEntry.getAddedAt();

        String readableAddedAt = formatter.format(addedAt);
        String readableUpdatedAt = formatter.format(updatedAt);

        BlockyJarUser user = bibleEntry.getUser();
        String userLogin = user.getUserLogin();
        int userID = user.getUserID();

        String messageToSend = STR."\{eventUserName} #\{page} was added by \{userLogin} (\{userID}) was added on \{readableAddedAt}";

        if (!readableUpdatedAt.equals(readableAddedAt))
        {
            messageToSend = STR."\{messageToSend} and lastly updated on \{readableUpdatedAt}";
        }

        sendChatMessage(channelID, STR."\{messageToSend} FeelsOkayMan");
    }
}
