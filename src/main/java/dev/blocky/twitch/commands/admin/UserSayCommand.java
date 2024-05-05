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
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.CommandManager;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static dev.blocky.twitch.utils.TwitchUtils.*;
import static java.util.Map.Entry;

public class UserSayCommand implements ICommand
{
    public static String channelToSend;

    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a chat.");
            return;
        }

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsGoodMan Please specify a message.");
            return;
        }

        String chatToSay = getUserAsString(messageParts, 1);

        if (!isValidUsername(chatToSay))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToSay = retrieveUserList(client, chatToSay);

        if (usersToSay.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{chatToSay}' found.");
            return;
        }

        User user = usersToSay.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        String messageToSend = removeElements(messageParts, 2);

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

        if (messageToSend.startsWith("/"))
        {
            if (!ownerIDs.contains(eventUserIID))
            {
                chat.sendMessage(channelName, "DatSheffy You don't have permission to use any kind of / (slash) commands through my account.");
                return;
            }

            IVR ivr = ServiceProvider.getIVRModVip(channelName);
            boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, "ApuJar");

            if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
            {
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because you aren't the broadcaster or a moderator.");
                return;
            }

            if (!selfModeratorPerms && !channelName.equalsIgnoreCase("ApuJar"))
            {
                chat.sendMessage(channelName, "ManFeels You can't use / (slash) commands, because i'm not the broadcaster or a moderator.");
                return;
            }
        }

        ConcurrentHashMap<List<String>, ICommand> commands = CommandManager.getCommandsAsMap();
        Set<Entry<List<String>, ICommand>> entries = commands.entrySet();

        String actualPrefix = SQLUtils.getPrefix(userIID);
        int prefixLength = actualPrefix.length();

        String command = messageParts[2];

        if (command.length() > prefixLength)
        {
            command = command.substring(prefixLength);

            for (Entry<List<String>, ICommand> entry : entries)
            {
                List<String> commandsAndAliases = entry.getKey();
                ICommand commandOrAlias = entry.getValue();

                if (commandsAndAliases.contains(command))
                {
                    HashSet<String> adminCommands = SQLUtils.getAdminCommands();
                    HashSet<String> ownerCommands = SQLUtils.getOwnerCommands();

                    if (adminCommands.contains(command) || ownerCommands.contains(command))
                    {
                        chat.sendMessage(channelName, "4Head Admin or owner commands aren't allowed to use here :P");
                        return;
                    }

                    channelToSend = userLogin;

                    String message = messageToSend.substring(prefixLength);

                    prefixedMessageParts = messageToSend.split(" ");
                    messageParts = message.split(" ");

                    commandOrAlias.onCommand(event, client, prefixedMessageParts, messageParts);

                    channelToSend = null;
                    return;
                }
            }
        }

        HashMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (globalCommands.containsKey(command))
        {
            String message = globalCommands.get(command);
            chat.sendMessage(channelToSend, message);
            return;
        }

        chat.sendMessage(userLogin, messageToSend);
        chat.sendMessage(channelName, STR."SeemsGood Successfully sent message in \{userDisplayName}'s chat.");
    }
}
