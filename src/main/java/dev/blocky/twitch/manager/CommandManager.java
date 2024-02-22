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

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.twitch.commands.*;
import dev.blocky.twitch.commands.admin.*;
import dev.blocky.twitch.commands.ivr.*;
import dev.blocky.twitch.commands.modscanner.*;
import dev.blocky.twitch.commands.owner.*;
import dev.blocky.twitch.commands.seventv.SevenTVAddCommand;
import dev.blocky.twitch.commands.seventv.SevenTVAllowCommand;
import dev.blocky.twitch.commands.seventv.SevenTVRemoveCommand;
import dev.blocky.twitch.commands.spotify.*;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class CommandManager
{
    private static ConcurrentHashMap<List<String>, ICommand> commands;
    private final TwitchClient client;

    public CommandManager(@NonNull SimpleEventHandler eventHandler, @NonNull TwitchClient client)
    {
        this.client = client;

        eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);

        commands = new ConcurrentHashMap<>();

        commands.put(List.of("commands", "help"), new CommandsCommand());
        commands.put(Collections.singletonList("ping"), new PingCommand());

        commands.put(Collections.singletonList("say"), new SayCommand());
        commands.put(List.of("usersay", "usay"), new UserSayCommand());

        commands.put(Collections.singletonList("spam"), new SpamCommand());
        commands.put(List.of("userspam", "uspam"), new UserSpamCommand());

        commands.put(Collections.singletonList("join"), new JoinCommand());
        commands.put(Collections.singletonList("part"), new PartCommand());

        commands.put(Collections.singletonList("exit"), new ExitCommand());

        commands.put(Collections.singletonList("addadmin"), new AddAdminCommand());
        commands.put(List.of("deleteadmin", "deladmin"), new DeleteAdminCommand());

        commands.put(Collections.singletonList("addowner"), new AddOwnerCommand());
        commands.put(List.of("deleteowner", "delowner"), new DeleteOwnerCommand());

        commands.put(Collections.singletonList("chatter"), new ChatterCommand());
        commands.put(Collections.singletonList("user"), new UserCommand());
        commands.put(Collections.singletonList("follower"), new FollowerCommand());
        commands.put(List.of("globalbadge", "badge"), new GlobalBadgeCommand());
        commands.put(List.of("chatidentity", "ci", "badges", "7tvpaint"), new ChatIdentityCommand());
        commands.put(List.of("chatcolor", "color"), new ChatColorCommand());
        commands.put(List.of("createdat", "created"), new CreatedAtCommand());
        commands.put(List.of("lastbroadcast", "laststream", "last"), new LastStreamCommand());

        commands.put(Collections.singletonList("isbanned"), new IsBannedCommand());
        commands.put(Collections.singletonList("isaffiliate"), new IsAffiliateCommand());
        commands.put(Collections.singletonList("ispartner"), new IsPartnerCommand());
        commands.put(Collections.singletonList("isstaff"), new IsStaffCommand());

        commands.put(List.of("modscanner", "ms"), new ModScannerCommand());
        commands.put(List.of("modlookup", "mods", "ml"), new ModLookupCommand());
        commands.put(List.of("viplookup", "vips", "vl"), new VIPLookupCommand());
        commands.put(List.of("founderlookup", "founder", "fl"), new FounderLookupCommand());

        commands.put(List.of("modage", "modchannel", "ismod", "ma", "mc"), new ModageCommand());
        commands.put(List.of("vipage", "vipchannel", "isvip", "va", "vc"), new VIPageCommand());
        commands.put(List.of("founderage", "founderchannel", "isfounder", "fda", "fc"), new FounderageCommand());

        commands.put(Collections.singletonList("setprefix"), new SetPrefixCommand());
        commands.put(List.of("deleteprefix", "delprefix"), new DeletePrefixCommand());

        commands.put(Collections.singletonList("sql"), new SQLCommand());

        commands.put(List.of("addglobalcommand", "addglobalcmd", "addgcmd"), new AddGlobalCommandCommand());
        commands.put(List.of("editglobalcommand", "editglobalcmd", "editgcmd"), new EditGlobalCommandCommand());
        commands.put(List.of("deleteglobalcommand", "deleteglobalcmd", "delglobalcommand", "delglobalcmd", "delgcmd"), new DeleteGlobalCommandCommand());

        commands.put(Collections.singletonList("google"), new GoogleCommand());

        commands.put(List.of("checkname", "cn"), new CheckNameCommand());

        commands.put(List.of("followage", "fa"), new FollowageCommand());
        commands.put(List.of("subage", "sa"), new SubageCommand());

        commands.put(List.of("crossban", "cb"), new CrossbanCommand());
        commands.put(List.of("crossunban", "cub"), new CrossunbanCommand());

        commands.put(List.of("addspotifyuser", "addspotifyu"), new AddSpotifyUserCommand());

        commands.put(Collections.singletonList("song"), new CurrentSongCommand());
        commands.put(Collections.singletonList("volume"), new VolumeCommand());
        commands.put(Collections.singletonList("setvolume"), new SetVolumeCommand());
        commands.put(Collections.singletonList("start"), new StartResumePlaybackCommand());
        commands.put(Collections.singletonList("resume"), new StartResumePlaybackCommand());
        commands.put(Collections.singletonList("pause"), new PausePlaybackCommand());
        commands.put(Collections.singletonList("next"), new SkipToNextSongCommand());
        commands.put(List.of("previous", "prev"), new SkipToPreviousSongCommand());
        commands.put(Collections.singletonList("setduration"), new SeekSongPositionCommand());
        commands.put(Collections.singletonList("additem"), new AddItemToPlaybackQueueCommand());
        commands.put(Collections.singletonList("repeat"), new RepeatCommand());
        commands.put(Collections.singletonList("shuffle"), new ShuffleCommand());
        commands.put(Collections.singletonList("yoink"), new YoinkSongCommand());

        commands.put(List.of("7tvallow", "siebentverlaubung"), new SevenTVAllowCommand());
        commands.put(List.of("7tvadd", "siebentvhinzufuegung"), new SevenTVAddCommand());
        commands.put(List.of("7tvremove", "siebentventfernung"), new SevenTVRemoveCommand());
    }

    boolean onMessage(String commandOrAlias, ChannelMessageEvent event, String[] prefixedMessageParts, String[] messageParts) throws Exception
    {
        Set<Map.Entry<List<String>, ICommand>> entries = commands.entrySet();

        for (Map.Entry<List<String>, ICommand> entry : entries)
        {
            List<String> commandsAndAliases = entry.getKey();
            ICommand command = entry.getValue();

            if (commandsAndAliases.contains(commandOrAlias))
            {
                command.onCommand(event, client, prefixedMessageParts, messageParts);
                return true;
            }
        }
        return false;
    }

    public void onChannelMessage(@NonNull ChannelMessageEvent event)
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();

        try
        {
            String message = event.getMessage();

            String actualPrefix = SQLUtils.getActualPrefix(channelID);
            int prefixLength = actualPrefix.length();

            Pattern PREFIX_PATTERN = Pattern.compile("^@?apujar,? prefix(.*)?$", CASE_INSENSITIVE);
            Matcher PREFIX_MATCHER = PREFIX_PATTERN.matcher(message);

            if (PREFIX_MATCHER.matches())
            {
                chat.sendMessage(channelName, STR."4Head My current prefix is '\{actualPrefix}'");
                return;
            }

            HashMap<String, String> globalCommands = SQLUtils.getGlobalCommands();

            if (message.startsWith(actualPrefix))
            {
                String globalCommand = message.substring(prefixLength).strip();

                if (globalCommands.containsKey(globalCommand))
                {
                    chat.sendMessage(channelName, globalCommands.get(globalCommand));
                    return;
                }
            }

            if (!message.startsWith(actualPrefix))
            {
                return;
            }

            EventUser eventUser = event.getUser();
            String eventUserID = eventUser.getId();
            int eventUserIID = Integer.parseInt(eventUserID);

            String commandRaw = message.substring(prefixLength).strip();
            String[] messageParts = commandRaw.split(" ");

            if (messageParts.length > 0)
            {
                String command = messageParts[0];

                HashSet<Integer> adminIDs = SQLUtils.getAdminIDs();
                HashSet<String> adminCommands = SQLUtils.getAdminCommands();

                HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();
                HashSet<String> ownerCommands = SQLUtils.getOwnerCommands();

                if ((!adminIDs.contains(eventUserIID) && adminCommands.contains(command)) && (!ownerIDs.contains(eventUserIID) && ownerCommands.contains(command)))
                {
                    chat.sendMessage(channelName, "4Head You don't have any permission to do that :P");
                    return;
                }

                String[] prefixedMessageParts = message.split(" ");

                if (!command.isBlank() && !onMessage(command, event, prefixedMessageParts, messageParts))
                {
                    chat.sendMessage(channelName, STR."Sadeg '\{command}' command was not found.");
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();
            chat.sendMessage(channelName, STR."Weird Error while trying to execute an command FeelsGoodMan \{error}");

            e.printStackTrace();
        }
    }

    @NonNull
    public static ConcurrentHashMap<List<String>, ICommand> getConcurrentCommands()
    {
        return commands;
    }

    @NonNull
    public static HashSet<String> getCommands()
    {
        Set<Map.Entry<List<String>, ICommand>> entries = commands.entrySet();
        HashSet<String> commandSet = new HashSet<>();

        for (Map.Entry<List<String>, ICommand> entry : entries)
        {
            List<String> commandsAndAliases = entry.getKey();
            commandSet.addAll(commandsAndAliases);
        }

        return commandSet;
    }
}
