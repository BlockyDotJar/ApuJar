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
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.Command;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static dev.blocky.twitch.utils.TwitchUtils.removeElementsAsArray;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class AddCommandAliasesCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a command.");
            return;
        }

        String command = messageParts[1].toLowerCase();

        if (!command.matches("^[a-zA-Z\\d]+$"))
        {
            sendChatMessage(channelID, "FeelsMan Specified command is invalid.");
            return;
        }

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsGoodMan Please specify at least one new alias.");
            return;
        }

        String[] aliasesRaw = removeElementsAsArray(messageParts, 2);
        List<String> aliases = Arrays.stream(aliasesRaw).collect(Collectors.toCollection(ArrayList::new));
        List<String> invalidAliases = aliases.stream()
                .filter(alias -> !alias.matches("^[a-zA-Z\\d]+$") || alias.length() < 2)
                .collect(Collectors.toCollection(ArrayList::new));

        aliases.removeAll(invalidAliases);

        if (aliases.isEmpty())
        {
            sendChatMessage(channelID, "FeelsDankMan Invalid aliases specified. (Either special characters are used or the length is below 2 characters)");
            return;
        }

        Set<Command> commands = SQLUtils.getCommands();

        Optional<Command> validCommand = commands.stream()
                .filter(cmd ->
                {
                    String commandName = cmd.getCommand();
                    boolean requiresOwner = cmd.requiresOwner();

                    Map<Integer, String> owners = SQLUtils.getOwners();
                    Set<Integer> ownerIDs = owners.keySet();

                    if (requiresOwner && !ownerIDs.contains(eventUserIID))
                    {
                        return false;
                    }

                    return commandName.equals(command);
                })
                .findFirst();

        Command cmd = validCommand.orElse(null);

        if (cmd == null)
        {
            sendChatMessage(channelID, STR."FeelsDankMan Command '\{command}' is either a owner command or doesn't even exist.");
            return;
        }

        Set<String> commandAndAliases = cmd.getCommandAndAliases();
        List<String> matchedAliases = commandAndAliases.stream()
                .filter(aliases::contains)
                .toList();

        invalidAliases.addAll(matchedAliases);
        aliases.removeAll(matchedAliases);

        int aliasCount = aliases.size();

        if (aliases.isEmpty())
        {
            sendChatMessage(channelID, "FeelsDankMan All aliases already exist.");
            return;
        }

        String messageToSend = STR."WOW Successfully added \{aliasCount} aliases to the '\{command}' command.";

        if (!invalidAliases.isEmpty())
        {
            String readableInvalidAliases = String.join(", ", invalidAliases);
            messageToSend += STR." (Invalid aliases: \{readableInvalidAliases})";
        }

        Set<String> cmdAliases = cmd.getAliases();

        if (cmdAliases == null)
        {
            cmdAliases = Collections.emptySet();
        }

        List<String> newAliases = CollectionUtils.collate(cmdAliases, aliases);
        String readableNewAliases = String.join(",", newAliases);

        SQLite.onUpdate(STR."UPDATE commands SET aliases = '\{readableNewAliases}' WHERE command = '\{command}'");

        sendChatMessage(channelID, messageToSend);
    }
}
