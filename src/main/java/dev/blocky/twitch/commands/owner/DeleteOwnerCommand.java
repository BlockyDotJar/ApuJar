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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections4.BidiMap;

import java.util.Collection;

import static dev.blocky.twitch.utils.TwitchUtils.getUserAsString;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class DeleteOwnerCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (eventUserIID != 755628467)
        {
            sendChatMessage(channelID, "oop You are not my founder.");
            return;
        }

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        BidiMap<Integer, String> owners = SQLUtils.getOwners();
        Collection<String> ownerLogins = owners.values();

        String ownerToDemote = getUserAsString(messageParts, 1);

        if (!ownerLogins.contains(ownerToDemote))
        {
            sendChatMessage(channelID, STR."CoolStoryBob \{ownerToDemote} isn't even an owner.");
            return;
        }

        int ownerID = owners.getKey(ownerToDemote);
        ServiceProvider.deleteOwner(channelIID, ownerID);

        SQLite.onUpdate(STR."DELETE FROM admins WHERE userLogin = '\{ownerToDemote}'");

        sendChatMessage(channelID, STR."BloodTrail Successfully demoted \{ownerToDemote}.");
    }
}
