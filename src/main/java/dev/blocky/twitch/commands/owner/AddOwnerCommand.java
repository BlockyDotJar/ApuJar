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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.request.BlockyJarUserBody;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class AddOwnerCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

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

        String chatToPromote = getUserAsString(messageParts, 1);

        if (!isValidUsername(chatToPromote))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> chatsToPromote = retrieveUserList(client, chatToPromote);

        if (chatsToPromote.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{chatToPromote}' found.");
            return;
        }

        User user = chatsToPromote.getFirst();
        String userID = user.getId();
        String userLogin = user.getLogin();
        String userDisplayName = user.getDisplayName();
        int userIID = Integer.parseInt(userID);

        Map<Integer, String> owners = SQLUtils.getOwners();
        Set<Integer> ownerIDs = owners.keySet();

        if (ownerIDs.contains(userIID))
        {
            sendChatMessage(channelID, STR."CoolStoryBob Already promoted \{chatToPromote}.");
            return;
        }

        Map<Integer, String> admins = SQLUtils.getAdmins();
        Set<Integer> adminIDs = admins.keySet();

        if (adminIDs.contains(userIID))
        {
            SQLite.onUpdate(STR."UPDATE admins SET isOwner = TRUE WHERE userID = \{userID}");

            sendChatMessage(channelID, STR."BloodTrail Successfully promoted \{userDisplayName} as an owner.");
            return;
        }

        SQLite.onUpdate(STR."INSERT INTO admins(userID, userLogin, isOwner) VALUES(\{userID}, '\{userLogin}', TRUE)");

        BlockyJarUserBody body = new BlockyJarUserBody(userIID, userLogin);
        ServiceProvider.postOwner(body);

        sendChatMessage(channelID, STR."BloodTrail Successfully promoted \{userDisplayName} as an owner.");
    }
}
