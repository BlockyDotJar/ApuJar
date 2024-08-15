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
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Command;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class FileSayCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a link to a text file.");
            return false;
        }

        String link = messageParts[1];

        if (!link.startsWith("https://") && !link.startsWith("http://"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid link specified.");
            return false;
        }

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(link)
                .build();

        try (Response response = okHttpClient.newCall(request).execute())
        {
            int statusCode = response.code();

            if (statusCode != 200)
            {
                sendChatMessage(channelID, STR."DankThink Server returned status code \{statusCode}.");
                return false;
            }

            Headers headers = response.headers();
            String contentType = headers.get("Content-Type");

            if (!contentType.contains("text/plain"))
            {
                sendChatMessage(channelID, "DankThink Server doesn't return plain text as response.");
                return false;
            }

            String body = response.body().string();

            if (body.isBlank())
            {
                sendChatMessage(channelID, "DankThink Server returned empty response.");
                return false;
            }

            String[] linesRaw = body.split("\n");
            String[] lines = getFilteredParts(linesRaw);

            for (String line : lines)
            {
                if (line.startsWith("/") && !line.equals("/"))
                {
                    List<Badge> badges = event.getBadges();
                    handleSlashCommands(channelIID, eventUserIID, channelIID, messageParts, badges, 2);
                    continue;
                }

                Set<Command> commands = SQLUtils.getCommands();

                Prefix prefix = SQLUtils.getPrefix(channelIID);
                String actualPrefix = prefix.getPrefix();
                int prefixLength = actualPrefix.length();

                boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

                String[] linePartsRaw = line.split(" ");
                String[] lineParts = getFilteredParts(linePartsRaw);

                String command = lineParts[0];

                if ((command.startsWith(actualPrefix) && !caseInsensitivePrefix) ||
                        (StringUtils.startsWithIgnoreCase(command, actualPrefix) && caseInsensitivePrefix) && command.length() > prefixLength)
                {
                    command = command.substring(prefixLength).toLowerCase();

                    for (Command cmd : commands)
                    {
                        Set<String> commandsAndAliases = cmd.getCommandAndAliases();
                        ICommand commandOrAlias = cmd.getCommandAsClass();

                        if (commandsAndAliases.contains(command))
                        {
                            Set<String> adminCommands = SQLUtils.getAdminCommands();
                            Set<String> ownerCommands = SQLUtils.getOwnerCommands();

                            if (adminCommands.contains(command) || ownerCommands.contains(command))
                            {
                                sendChatMessage(channelID, "4Head Admin or owner commands aren't allowed to use here :P");
                                continue;
                            }

                            String message = line.substring(prefixLength);

                            String[] messagePartsRaw = message.split(" ");
                            messageParts = getFilteredParts(messagePartsRaw);

                            String[] prefixedMessagePartsRaw = command.split(" ");
                            prefixedMessageParts = getFilteredParts(prefixedMessagePartsRaw);

                            boolean isSayable = cmd.isSayable();

                            if (!isSayable)
                            {
                                sendChatMessage(channelID, "4Head Specified command isn't sayable :P");
                                return false;
                            }

                            commandOrAlias.onCommand(event, client, prefixedMessageParts, messageParts);
                        }
                    }
                }

                TreeMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

                if (globalCommands.containsKey(command))
                {
                    String message = globalCommands.get(command);
                    sendChatMessage(channelID, message);

                    TimeUnit.SECONDS.sleep(1);
                    continue;
                }

                if ((!line.startsWith(actualPrefix) && !caseInsensitivePrefix) ||
                        (!StringUtils.startsWithIgnoreCase(line, actualPrefix) && caseInsensitivePrefix))
                {
                    sendChatMessage(channelID, line);
                }

                TimeUnit.SECONDS.sleep(1);
            }

            int lineCount = lines.length;

            return sendChatMessage(channelID, STR."WOW Successfully sent \{lineCount} lines.");
        }
        catch (UnknownHostException _)
        {
            sendChatMessage(channelID, "FeelsMan Host of the website is unknown.");
            return false;
        }
    }
}
