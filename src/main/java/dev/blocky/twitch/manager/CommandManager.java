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
package dev.blocky.twitch.manager;

import com.github.philippheuer.events4j.api.IEventManager;
import com.github.twitch4j.eventsub.domain.chat.Message;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.serialization.Command;
import dev.blocky.twitch.serialization.Keyword;
import dev.blocky.twitch.serialization.Prefix;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blocky.twitch.Main.client;
import static dev.blocky.twitch.utils.TwitchUtils.getFilteredParts;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class CommandManager
{
    private static final HashMap<Integer, Long> keywordRatelimits = new HashMap<>();

    public static final HashMap<Integer, Long> ratelimitedChats = new HashMap<>();

    public CommandManager(@NonNull IEventManager eventManager)
    {
        eventManager.onEvent(ChannelChatMessageEvent.class, this::onChannelChatMessage);
    }

    boolean onChatMessage(@NonNull String commandOrAlias, @NonNull ChannelChatMessageEvent event, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        Set<Command> commands = SQLUtils.getCommands();

        for (Command cmd : commands)
        {
            Set<String> commandsAndAliases = cmd.getCommandAndAliases();
            boolean commandExists = commandsAndAliases.stream().anyMatch(commandOrAlias::equalsIgnoreCase);

            if (commandExists)
            {
                ICommand command = cmd.getCommandAsClass();
                command.onCommand(event, client, prefixedMessageParts, messageParts);
                return true;
            }
        }
        return false;
    }

    public void onChannelChatMessage(@NonNull ChannelChatMessageEvent event)
    {
        String channelName = event.getBroadcasterUserName();
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        String eventUserName = event.getChatterUserName();
        String eventUserID = event.getChatterUserId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (eventUserIID == 896181679)
        {
            return;
        }

        try
        {
            if (ratelimitedChats.containsKey(channelIID))
            {
                long currentTimeMillis = System.currentTimeMillis();
                long timeMillis = ratelimitedChats.get(channelIID);

                long difference = currentTimeMillis - timeMillis;

                if (difference < 3000)
                {
                    return;
                }

                ratelimitedChats.remove(channelIID);
            }

            Message message = event.getMessage();
            String cleanedMessage = message.getCleanedText();

            Prefix prefix = SQLUtils.getPrefix(channelIID);
            String actualPrefix = prefix.getPrefix();
            int prefixLength = actualPrefix.length();

            boolean caseInsensitivePrefix = prefix.isCaseInsensitive();

            Pattern PREFIX_PATTERN = Pattern.compile("(.*)?@?apujar,?\\s+?prefix(.*)?", CASE_INSENSITIVE);
            Matcher PREFIX_MATCHER = PREFIX_PATTERN.matcher(cleanedMessage);

            SQLUtils.correctUserLogin(eventUserIID, eventUserName);

            if (PREFIX_MATCHER.matches())
            {
                sendChatMessage(channelID, STR."4Head My current prefix is '\{actualPrefix}'. (Case-Insensitive: \{caseInsensitivePrefix})");
                return;
            }

            TreeMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

            if ((cleanedMessage.startsWith(actualPrefix) && !caseInsensitivePrefix) ||
                    (StringUtils.startsWithIgnoreCase(cleanedMessage, actualPrefix) && caseInsensitivePrefix))
            {
                String globalCommand = cleanedMessage.substring(prefixLength).strip();

                if (globalCommands.containsKey(globalCommand))
                {
                    String commandMessage = globalCommands.get(globalCommand);
                    sendChatMessage(channelID, commandMessage);
                    return;
                }

                String commandRaw = cleanedMessage.substring(prefixLength).strip();

                String[] messagePartsRaw = commandRaw.split(" ");
                String[] messageParts = getFilteredParts(messagePartsRaw);

                if (messageParts.length > 0)
                {
                    String command = messageParts[0].toLowerCase();

                    Map<Integer, String> admins = SQLUtils.getAdmins();
                    Set<Integer> adminIDs = admins.keySet();

                    Set<String> adminCommands = SQLUtils.getAdminCommands();

                    Map<Integer, String> owners = SQLUtils.getOwners();
                    Set<Integer> ownerIDs = owners.keySet();

                    Set<String> ownerCommands = SQLUtils.getOwnerCommands();

                    if (!adminIDs.contains(eventUserIID) && adminCommands.contains(command))
                    {
                        sendChatMessage(channelID, "4Head You don't have any permission to use admin commands :P");
                        return;
                    }

                    if (!ownerIDs.contains(eventUserIID) && ownerCommands.contains(command))
                    {
                        sendChatMessage(channelID, "4Head You don't have any permission to use owner commands :P");
                        return;
                    }

                    String[] prefixedMessagePartsRaw = cleanedMessage.split(" ");
                    String[] prefixedMessageParts = getFilteredParts(prefixedMessagePartsRaw);

                    if (!command.isBlank() && !onChatMessage(command, event, prefixedMessageParts, messageParts))
                    {
                        sendChatMessage(channelID, STR."Sadeg '\{command}' command wasn't found.");
                    }

                    return;
                }
                return;
            }

            Set<Keyword> keywords = SQLUtils.getKeywords(channelIID);

            for (Keyword keyword : keywords)
            {
                String kw = keyword.getName();
                String kwMessage = keyword.getMessage();
                boolean exactMatch = keyword.isExactMatch();
                boolean caseInsensitiveKeyword = keyword.isCaseInsensitive();

                if ((((cleanedMessage.equals(kw) && !caseInsensitiveKeyword) || (cleanedMessage.equalsIgnoreCase(kw) && caseInsensitiveKeyword)) && exactMatch) ||
                        (((cleanedMessage.contains(kw) && !caseInsensitiveKeyword) || (StringUtils.containsIgnoreCase(cleanedMessage, kw) && caseInsensitiveKeyword)) && !exactMatch))
                {
                    long currentTimeMillis = System.currentTimeMillis();

                    if (keywordRatelimits.containsKey(channelIID))
                    {
                        long timeMillis = keywordRatelimits.get(channelIID);
                        long difference = currentTimeMillis - timeMillis;

                        if (difference < 3000)
                        {
                            return;
                        }
                    }

                    keywordRatelimits.put(channelIID, currentTimeMillis);

                    sendChatMessage(channelID, kwMessage);
                    return;
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            if (error != null && error.equals("Forbidden"))
            {
                sendChatMessage(channelID, "FeelsOkayMan Spotify account E-Mail needed. Send the E-Mail to @BlockyDotJar, because i currently don't have quota-extension perms on Spotify. https://developer.spotify.com/documentation/web-api/concepts/quota-modes");
                return;
            }

            if (error != null && error.equals("Player command failed: Restriction violated"))
            {
                sendChatMessage(channelID, "FeelsDankMan Can't skip to previous song because of Spotify being weird.");
                return;
            }

            sendChatMessage("896181679", STR."Channel: \{channelName} Weird Error while trying to execute an command FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }
}
