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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections4.BidiMap;

import java.util.Collection;
import java.util.Map;

import static dev.blocky.twitch.utils.TwitchUtils.getUserAsString;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class DeleteAdminCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return false;
        }

        Map<Integer, String> owners = SQLUtils.getOwners();
        Collection<String> ownerLogins = owners.values();

        BidiMap<Integer, String> admins = SQLUtils.getAdmins();
        Collection<String> adminLogins = admins.values();

        String adminToDemote = getUserAsString(messageParts, 1);

        if (ownerLogins.contains(adminToDemote))
        {
            sendChatMessage(channelID, "TriHard Won't demote an owner.");
            return false;
        }

        if (!adminLogins.contains(adminToDemote))
        {
            sendChatMessage(channelID, STR."CoolStoryBob \{adminToDemote} isn't even an admin.");
            return false;
        }

        int adminID = admins.getKey(adminToDemote);
        ServiceProvider.deleteAdmin(channelIID, adminID);

        SQLite.onUpdate(STR."DELETE FROM admins WHERE userLogin = '\{adminToDemote}'");

        return sendChatMessage(channelID, STR."BloodTrail Successfully demoted \{adminToDemote}.");
    }
}
