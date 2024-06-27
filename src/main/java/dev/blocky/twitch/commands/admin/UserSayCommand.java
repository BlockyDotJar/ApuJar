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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Command;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class UserSayCommand implements ICommand
{
    public static String channelToSend;

    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserID = event.getChatterUserId();
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

        if (messageToSend.startsWith("/") && !messageToSend.equals("/"))
        {
            handleSlashCommands(channelIID, eventUserIID, userIID, messageParts, 2);
            return;
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

            boolean wasSent = sendChatMessage(userID, message);

            if (!wasSent)
            {
                sendChatMessage(channelID, STR."WAIT Something went wrong by sending a message to the chat of \{userDisplayName} (Am i banned/timeouted or are there any special chat settings activated?)");
                return;
            }

            sendChatMessage(channelID, STR."SeemsGood Successfully sent message in \{userDisplayName}'s chat.");
            return;
        }

        boolean wasSent = sendChatMessage(userID, messageToSend);

        if (!wasSent)
        {
            sendChatMessage(channelID, STR."WAIT Something went wrong by sending a message to the chat of \{userDisplayName} (Am i banned/timeouted or are there any special chat settings activated?)");
            return;
        }

        sendChatMessage(channelID, STR."SeemsGood Successfully sent message in \{userDisplayName}'s chat.");
    }
}
