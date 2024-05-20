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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class JoinCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        String chatToJoin = getUserAsString(messageParts, eventUser);

        if (!isValidUsername(chatToJoin))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> chatsToJoin = retrieveUserList(client, chatToJoin);

        if (chatsToJoin.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{chatToJoin}' found.");
            return;
        }

        if (!chatToJoin.equalsIgnoreCase(eventUserName))
        {
            IVR ivr = ServiceProvider.getIVRModVip(chatToJoin);
            boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);

            Map<Integer, String> admins = SQLUtils.getAdmins();
            Set<Integer> adminIDs = admins.keySet();

            Map<Integer, String> owners = SQLUtils.getOwners();
            Set<Integer> ownerIDs = owners.keySet();

            if (messageParts.length > 1 && (!hasModeratorPerms && !adminIDs.contains(eventUserIID) && !ownerIDs.contains(eventUserIID)))
            {
                sendChatMessage(channelID, "ManFeels Can't join channel, because you aren't broadcaster or mod at this channel.");
                return;
            }
        }

        if (chat.isChannelJoined(chatToJoin) || chatToJoin.equalsIgnoreCase("ApuJar"))
        {
            sendChatMessage(channelID, STR."CoolStoryBob Already joined \{chatToJoin}'s chat.");
            return;
        }

        chat.joinChannel(chatToJoin);

        User user = chatsToJoin.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();

        SQLite.onUpdate(STR."INSERT INTO chats(userID, userLogin, eventsEnabled) VALUES(\{userID}, '\{userLogin}', TRUE)");

        sendChatMessage(userID, "lebronJAM Hi, my name is, what? HUH My name is, who? eeeh My name is, ApuApustaja ApuApustaja ApuJar !");
        sendChatMessage(channelID, STR."MrDestructoid Successfully joined \{userDisplayName}'s chat SeemsGood If you want to disable event notifications use #ren false FeelsOkayMan By adding me to your chat, you agree with our Privacy Policy ( https://apujar.blockyjar.dev/legal/privacy-policy.html ) and our ToS ( https://apujar.blockyjar.dev/legal/terms-of-service.html ) Okayeg If you disagree with them, then use '#part' to remove the bot from your chat FeelsGoodMan");
    }
}
