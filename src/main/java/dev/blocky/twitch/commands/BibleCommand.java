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
import dev.blocky.api.entities.blockyjar.BlockyJarBible;
import dev.blocky.api.entities.blockyjar.BlockyJarBibleEntry;
import dev.blocky.api.entities.blockyjar.BlockyJarUser;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class BibleCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();

        String pageRaw = null;

        if (messageParts.length == 2)
        {
            pageRaw = messageParts[1];
        }

        BlockyJarBible bible;
        BlockyJarBibleEntry bibleEntry = null;

        if (pageRaw != null)
        {
            if (!pageRaw.matches("^\\d+$"))
            {
                sendChatMessage(channelID, "oop Specified value isn't a number.");
                return false;
            }

            int page = Integer.parseInt(pageRaw);

            if (page <= 0)
            {
                sendChatMessage(channelID, "oop Number can't be equal to 0 or negative.");
                return false;
            }

            bibleEntry = ServiceProvider.getBibleEntry(channelIID, page);
        }

        if (bibleEntry == null && pageRaw == null)
        {
            bible = ServiceProvider.getBible(channelIID, true, 1);

            List<BlockyJarBibleEntry> bibleEntries = bible.getBibleEntries();
            bibleEntry = bibleEntries.getFirst();
        }

        if (bibleEntry == null)
        {
            return false;
        }

        int page = bibleEntry.getPage();
        String entry = bibleEntry.getEntry();

        BlockyJarUser user = bibleEntry.getUser();
        String userLogin = user.getUserLogin();

        return sendChatMessage(channelID, STR."\{eventUserName} #\{page} added by \{userLogin} \uD83D\uDC49 \{entry}");
    }
}
