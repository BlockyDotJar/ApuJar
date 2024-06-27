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
package dev.blocky.twitch.commands.rolelookup;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.tools.ToolsFounder;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class FounderLookupCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToLookup = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToLookup))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToLookup}' found.");
            return;
        }

        User user = usersToLookup.getFirst();
        String userLogin = user.getLogin();
        String userDisplayName = user.getDisplayName();
        String userBroadcasterType = user.getBroadcasterType();

        if (!userBroadcasterType.equals("affiliate") && !userBroadcasterType.equals("partner"))
        {
            sendChatMessage(channelID, STR."ManFeels \{userDisplayName} isn't even an affiliate or partner.");
            return;
        }

        List<ToolsFounder> toolsFounders = ServiceProvider.getToolsFounders(userLogin);

        if (toolsFounders == null)
        {
            sendChatMessage(channelID, STR."Sadeg No founders for user \{userDisplayName} found.");
            return;
        }

        if (toolsFounders.isEmpty())
        {
            sendChatMessage(channelID, STR."Sadeg No founders for user \{userDisplayName} found.");
            return;
        }

        int founderCount = toolsFounders.size();

        String messageToSend = STR."PogChamp \{userDisplayName} has \{founderCount} founder! o_O https://tools.2807.eu/founders?user=\{userLogin}";

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
