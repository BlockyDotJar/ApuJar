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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TwitchUtils;
import dev.blocky.twitch.utils.serialization.Command;
import dev.blocky.twitch.utils.serialization.Prefix;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserSayCommand implements ICommand
{
    public static String channelToSend;

    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a chat.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsGoodMan Please specify a message.");
            return;
        }

        String chatToSay = getUserAsString(messageParts, 1);

        if (!isValidUsername(chatToSay))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToSay = retrieveUserList(client, chatToSay);

        if (usersToSay.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{chatToSay}' found.");
            return;
        }

        User user = usersToSay.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        String messageToSend = removeElements(messageParts, 2);

        Map<Integer, String> owners = SQLUtils.getOwners();
        Set<Integer> ownerIDs = owners.keySet();

        if (messageToSend.startsWith("/"))
        {
            if (!ownerIDs.contains(eventUserIID))
            {
                sendChatMessage(channelID, "DatSheffy You don't have permission to use any kind of / (slash) commands through my account.");
                return;
            }

            IVR ivr = ServiceProvider.getIVRModVip(channelName);
            boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, eventUserName);
            boolean selfModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, "ApuJar");

            if (!channelName.equalsIgnoreCase(eventUserName) && !hasModeratorPerms)
            {
                sendChatMessage(channelID, "ManFeels You can't use / (slash) commands, because you aren't the broadcaster or a moderator.");
                return;
            }

            if (!selfModeratorPerms && !channelName.equalsIgnoreCase("ApuJar"))
            {
                sendChatMessage(channelID, "ManFeels You can't use / (slash) commands, because i'm not the broadcaster or a moderator.");
                return;
            }
        }

        Set<Command> commands = SQLUtils.getCommands();

        Prefix prefix = SQLUtils.getPrefix(userIID);
        String actualPrefix = prefix.getPrefix();
        int prefixLength = actualPrefix.length();

        boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

        String command = messageParts[2];

        boolean isSendable = checkChatSettings(messageParts, userLogin, userID, channelID);

        if (!isSendable)
        {
            return;
        }

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
                        return;
                    }

                    channelToSend = userID;

                    String message = messageToSend.substring(prefixLength);

                    String[] messagePartsRaw = message.split(" ");
                    messageParts = getFilteredParts(messagePartsRaw);

                    String[] prefixedMessagePartsRaw = messageToSend.split(" ");
                    prefixedMessageParts = getFilteredParts(prefixedMessagePartsRaw);

                    boolean isSayable = cmd.isSayable();

                    if (!isSayable)
                    {
                        sendChatMessage(channelID, "4Head Specified command isn't sayable :P");
                        return;
                    }

                    commandOrAlias.onCommand(event, client, prefixedMessageParts, messageParts);

                    channelToSend = null;
                    return;
                }
            }
        }

        TreeMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

        if (globalCommands.containsKey(command))
        {
            String message = globalCommands.get(command);

            sendChatMessage(userID, message);
            sendChatMessage(channelID, STR."SeemsGood Successfully sent message in \{userDisplayName}'s chat.");
            return;
        }

        sendChatMessage(userID, messageToSend);
        sendChatMessage(channelID, STR."SeemsGood Successfully sent message in \{userDisplayName}'s chat.");
    }
}
