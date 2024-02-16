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
package dev.blocky.twitch.commands.ivr;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.IVRFI;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class CheckNameCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        String actualPrefix = SQLUtils.getActualPrefix(event.getChannel().getId());
        String message = getSayableMessage(event.getMessage());

        String[] msgParts = message.split(" ");

        if (msgParts.length == 1)
        {
            chat.sendMessage(event.getChannel().getName(), "FeelsMan Please specify a username to check.");
            return;
        }

        String userToCheck = getUserAsString(msgParts, 1);

        if (!isValidUsername(userToCheck))
        {
            chat.sendMessage(event.getChannel().getName(), "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<IVRFI> ivrfiList = ServiceProvider.createIVRFIUser(userToCheck);

        if (ivrfiList.isEmpty())
        {
            chat.sendMessage(event.getChannel().getName(), STR."Saved Username '\{userToCheck}' is available.");
            return;
        }

        String channelName = getActualChannel(channelToSend, event.getChannel().getName());

        chat.sendMessage(channelName, STR."monakS Username '\{userToCheck}' is used.");
    }
}
