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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.twitch.interfaces.ICommand;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FileSayCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a link to a text file.");
            return;
        }

        String link = messageParts[1];

        if (!link.startsWith("https://") && !link.startsWith("http://"))
        {
            chat.sendMessage(channelName, "FeelsMan Invalid link specified.");
            return;
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
                chat.sendMessage(channelName, STR."DankThink Server returned status code \{statusCode}.");
                return;
            }

            Headers headers = response.headers();
            String contentType = headers.get("Content-Type");

            if (contentType == null)
            {
                chat.sendMessage(channelName, "DankThink Server doesn't return any 'Content-Type' header.");
                return;
            }

            if (!contentType.contains("text/plain"))
            {
                chat.sendMessage(channelName, "DankThink Server doesn't return plain text as response.");
                return;
            }

            String body = response.body().string();

            if (body.isBlank())
            {
                chat.sendMessage(channelName, "DankThink Server returned empty response.");
                return;
            }

            String[] linesRaw = body.split("\n");
            String[] lines = Arrays.stream(linesRaw)
                    .filter(line -> !line.isBlank())
                    .map(String::strip)
                    .toArray(String[]::new);

            for (String line : lines)
            {
                chat.sendMessage(channelName, line);
                TimeUnit.SECONDS.sleep(1);
            }

            int lineCount = lines.length;

            chat.sendMessage(channelName, STR."WOW Successfully sent \{lineCount} lines.");
        }
        catch (UnknownHostException _)
        {
            chat.sendMessage(channelName, "FeelsMan Invalid link specified.");
        }
    }
}
