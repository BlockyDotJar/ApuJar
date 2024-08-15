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
package dev.blocky.twitch.commands.admin;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.blockyjar.BlockyJarBibleEntry;
import dev.blocky.api.request.BlockyJarBibleBody;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class AddBibleEntryCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a entry.");
            return false;
        }

        String entryRaw = removeElements(messageParts, 1);
        String entry = handleIllegalCharacters(entryRaw);

        BlockyJarBibleBody body = new BlockyJarBibleBody(eventUserIID, entry, null, null);
        BlockyJarBibleEntry bibleEntry = ServiceProvider.postBibleEntry(channelIID, body);

        if (bibleEntry == null)
        {
            return false;
        }

        int page = bibleEntry.getPage();

        LocalDateTime now = LocalDateTime.now();

        SQLite.onUpdate(STR."INSERT INTO bible(page, entry, addedAt, updatedAt, userID, userLogin) VALUES(\{page}, '\{entry}', '\{now}', '\{now}', \{eventUserID}, '\{eventUserName}')");

        return sendChatMessage(channelID, STR."\{eventUserName} Successfully added entry #\{page} to our bible!");
    }
}
