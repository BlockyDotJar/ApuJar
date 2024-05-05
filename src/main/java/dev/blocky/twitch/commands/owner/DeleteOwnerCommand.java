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
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;

import static dev.blocky.twitch.utils.TwitchUtils.getUserAsString;

public class DeleteOwnerCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (eventUserIID != 755628467)
        {
            chat.sendMessage(channelName, "oop You are not my founder.");
            return;
        }

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        HashSet<String> ownerLogins = SQLUtils.getOwnerLogins();

        String ownerToDemote = getUserAsString(messageParts, 1);

        if (!ownerLogins.contains(ownerToDemote))
        {
            chat.sendMessage(channelName, STR."CoolStoryBob \{ownerToDemote} isn't even an owner.");
            return;
        }

        int ownerID = SQLUtils.getOwnerIDByLogin(ownerToDemote);
        ServiceProvider.deleteOwner(ownerID);

        SQLite.onUpdate(STR."DELETE FROM admins WHERE userLogin = '\{ownerToDemote}'");

        chat.sendMessage(channelName, STR."BloodTrail Successfully demoted \{ownerToDemote}.");
    }
}
