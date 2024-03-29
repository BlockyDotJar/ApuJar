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
package dev.blocky.twitch.commands.owner;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static dev.blocky.twitch.utils.TwitchUtils.removeElements;

public class SQLCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please send some sql.");
            return;
        }

        String sql = removeElements(messageParts, 1);

        if (!StringUtils.startsWithIgnoreCase(sql, "SELECT"))
        {
            SQLite.onUpdate(sql);

            chat.sendMessage(channelName, "o7 Successfully executed your sql code.");
            return;
        }

        if (StringUtils.startsWithIgnoreCase(sql, "SELECT"))
        {
            try (ResultSet set = SQLite.onQuery(sql))
            {
                ResultSetMetaData resultMetaData = set.getMetaData();
                int columnCount = resultMetaData.getColumnCount();

                StringBuilder output = new StringBuilder();

                for (int col = 1; col <= columnCount; col++)
                {
                    String columnLabel = resultMetaData.getColumnLabel(col);
                    output.append(columnLabel).append(" ");
                }

                output.append(" - ");

                while (set.next())
                {
                    for (int col = 1; col <= columnCount; col++)
                    {
                        String object = set.getObject(col).toString();
                        output.append(object).append(" ");
                    }

                    output.append(" - ");
                }

                chat.sendMessage(channelName, output.toString());
            }
        }
    }
}
