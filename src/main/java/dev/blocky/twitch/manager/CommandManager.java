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
import dev.blocky.twitch.commands.games.LeaveCommand;
import dev.blocky.twitch.commands.games.TicCommand;
import dev.blocky.twitch.commands.games.TicTacToeCommand;
import dev.blocky.twitch.commands.github.ChatterinoCommand;
import dev.blocky.twitch.commands.github.ChattyCommand;
import dev.blocky.twitch.commands.ivr.*;
import dev.blocky.twitch.commands.modscanner.*;
import dev.blocky.twitch.commands.owner.*;
import dev.blocky.twitch.commands.seventv.*;
import dev.blocky.twitch.commands.spotify.*;
import dev.blocky.twitch.commands.weather.UserWeatherCommand;
import dev.blocky.twitch.commands.weather.WeatherCommand;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        commands.put(List.of("ping"), new PingCommand());

        commands.put(List.of("say"), new SayCommand());
        commands.put(List.of("usersay", "usay"), new UserSayCommand());

        commands.put(List.of("spam"), new SpamCommand());
        commands.put(List.of("userspam", "uspam"), new UserSpamCommand());

        commands.put(List.of("globalsay", "gsay"), new GlobalSayCommand());

        commands.put(List.of("join"), new JoinCommand());
        commands.put(List.of("part"), new PartCommand());

        commands.put(List.of("exit"), new ExitCommand());

        commands.put(List.of("addadmin"), new AddAdminCommand());
        commands.put(List.of("deleteadmin", "deladmin"), new DeleteAdminCommand());

        commands.put(List.of("addowner"), new AddOwnerCommand());
        commands.put(List.of("deleteowner", "delowner"), new DeleteOwnerCommand());

        commands.put(List.of("chatter"), new ChatterCommand());
        commands.put(List.of("isinchat", "inchat"), new IsInChatCommand());
        commands.put(List.of("user"), new UserCommand());
        commands.put(List.of("follower"), new FollowerCommand());
        commands.put(List.of("globalbadge", "badge"), new GlobalBadgeCommand());
        commands.put(List.of("id"), new IDCommand());
        commands.put(List.of("chatidentity", "ci", "badges", "7tvpaint"), new ChatIdentityCommand());
        commands.put(List.of("chatcolor", "color"), new ChatColorCommand());
        commands.put(List.of("createdat", "created"), new CreatedAtCommand());
        commands.put(List.of("lastbroadcast", "laststream", "last"), new LastStreamCommand());

        commands.put(List.of("isbanned"), new IsBannedCommand());
        commands.put(List.of("isaffiliate"), new IsAffiliateCommand());
        commands.put(List.of("ispartner"), new IsPartnerCommand());
        commands.put(List.of("isstaff"), new IsStaffCommand());

        commands.put(List.of("modscanner", "ms"), new ModScannerCommand());
        commands.put(List.of("modlookup", "mods", "ml"), new ModLookupCommand());
        commands.put(List.of("viplookup", "vips", "vl"), new VIPLookupCommand());
        commands.put(List.of("founderlookup", "founder", "fl"), new FounderLookupCommand());

        commands.put(List.of("modage", "modchannel", "ismod", "ma", "mc"), new ModageCommand());
        commands.put(List.of("vipage", "vipchannel", "isvip", "va", "vc"), new VIPageCommand());
        commands.put(List.of("founderage", "founderchannel", "isfounder", "fda", "fc"), new FounderageCommand());

        commands.put(List.of("setprefix", "prefix"), new SetPrefixCommand());
        commands.put(List.of("deleteprefix", "delprefix"), new DeletePrefixCommand());

        commands.put(List.of("sql"), new SQLCommand());

        commands.put(List.of("addglobalcommand", "addglobalcmd", "addgcmd"), new AddGlobalCommandCommand());
        commands.put(List.of("editglobalcommand", "editglobalcmd", "editgcmd"), new EditGlobalCommandCommand());
        commands.put(List.of("deleteglobalcommand", "deleteglobalcmd", "delglobalcommand", "delglobalcmd", "delgcmd"), new DeleteGlobalCommandCommand());

        commands.put(List.of("addkeyword", "addkw"), new AddKeywordCommand());
        commands.put(List.of("editkeyword", "editkw"), new EditKeywordCommand());
        commands.put(List.of("editkeywordmatching", "editkwm"), new EditKeywordMatchingCommand());
        commands.put(List.of("deletekeyword", "delkw"), new DeleteKeywordCommand());

        commands.put(List.of("google"), new GoogleCommand());

        commands.put(List.of("checkname", "cn"), new CheckNameCommand());

        commands.put(List.of("followage", "fa"), new FollowageCommand());
        commands.put(List.of("subage", "sa"), new SubageCommand());

        commands.put(List.of("crossban", "cb"), new CrossbanCommand());
        commands.put(List.of("crossunban", "cub"), new CrossunbanCommand());

        commands.put(List.of("play"), new PlayCommand());
        commands.put(List.of("playlink"), new PlayLinkCommand());
        commands.put(List.of("song"), new SongCommand());
        commands.put(List.of("volume"), new VolumeCommand());
        commands.put(List.of("setvolume"), new SetVolumeCommand());
        commands.put(List.of("resume"), new ResumeCommand());
        commands.put(List.of("pause"), new PauseCommand());
        commands.put(List.of("next"), new NextCommand());
        commands.put(List.of("previous", "prev"), new PreviousCommand());
        commands.put(List.of("setprogress", "seekposition"), new SetProgressCommand());
        commands.put(List.of("queue"), new QueueCommand());
        commands.put(List.of("repeat"), new RepeatCommand());
        commands.put(List.of("shuffle"), new ShuffleCommand());
        commands.put(List.of("yoink"), new YoinkCommand());
        commands.put(List.of("songs"), new SongsCommand());
        commands.put(List.of("artists"), new ArtistsCommand());

        commands.put(List.of("7tvallow"), new SevenTVAllowCommand());
        commands.put(List.of("7tvdeny"), new SevenTVDenyCommand());
        commands.put(List.of("7tvemote"), new SevenTVEmoteCommand());
        commands.put(List.of("7tvuseremote", "7tvuemote"), new SevenTVUserEmoteCommand());
        commands.put(List.of("7tvuser"), new SevenTVUserCommand());
        commands.put(List.of("7tvadd"), new SevenTVAddCommand());
        commands.put(List.of("7tvaddlink"), new SevenTVAddLinkCommand());
        commands.put(List.of("7tvyoink"), new SevenTVYoinkCommand());
        commands.put(List.of("7tvrename", "7tvrn"), new SevenTVRenameCommand());
        commands.put(List.of("7tvremove", "7tvrm"), new SevenTVRemoveCommand());

        commands.put(List.of("receiveeventnotifications", "ren"), new ReceiveEventNotificationsCommand());

        commands.put(List.of("chatterino"), new ChatterinoCommand());
        commands.put(List.of("chatty"), new ChattyCommand());

        commands.put(List.of("weather"), new WeatherCommand());
        commands.put(List.of("userweather", "uweather"), new UserWeatherCommand());

        commands.put(List.of("tictactoe", "ttt"), new TicTacToeCommand());
        commands.put(List.of("leave"), new LeaveCommand());
        commands.put(List.of("tic"), new TicCommand());

        commands.put(List.of("wordle"), new WordleCommand());

        commands.put(List.of("kok", "cock", "penis", "pp"), new KokCommand());
        commands.put(List.of("sus", "susge", "suspicious"), new SusCommand());
        commands.put(List.of("cool"), new CoolCommand());
        commands.put(List.of("love"), new LoveCommand());

        commands.put(List.of("filesay", "fs"), new FileSayCommand());
    }

    boolean onMessage(@NonNull String commandOrAlias, @NonNull ChannelMessageEvent event, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        for (List<String> commandKeys : commands.keySet())
        {
            boolean commandExists = commandKeys.stream().anyMatch(commandOrAlias::equalsIgnoreCase);

            if (commandExists)
            {
                ICommand command = commands.get(commandKeys);
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
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        try
        {
            String message = event.getMessage();

            String actualPrefix = SQLUtils.getPrefix(channelIID);
            int prefixLength = actualPrefix.length();

            Pattern PREFIX_PATTERN = Pattern.compile("(.*)?(prefix\\s+(of|von|from))?\\s+?@?apujar,?(prefix)?(.*)?", CASE_INSENSITIVE);
            Matcher PREFIX_MATCHER = PREFIX_PATTERN.matcher(message);

            SQLUtils.correctUserLogin(eventUserIID, eventUserName);

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
                    String commandMessage = globalCommands.get(globalCommand);
                    chat.sendMessage(channelName, commandMessage);
                    return;
                }
            }

            if (message.startsWith(actualPrefix))
            {
                String commandRaw = message.substring(prefixLength).strip();

                String[] messagePartsRaw = commandRaw.split(" ");
                String[] messageParts = Arrays.stream(messagePartsRaw)
                        .filter(messagePart -> !messagePart.isBlank())
                        .map(String::strip)
                        .toArray(String[]::new);

                if (messageParts.length > 0)
                {
                    String command = messageParts[0];

                    HashSet<Integer> adminIDs = SQLUtils.getAdminIDs();
                    HashSet<String> adminCommands = SQLUtils.getAdminCommands();

                    HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();
                    HashSet<String> ownerCommands = SQLUtils.getOwnerCommands();

                    if (!adminIDs.contains(eventUserIID) && adminCommands.contains(command))
                    {
                        chat.sendMessage(channelName, "4Head You don't have any permission to use admin commands :P");
                        return;
                    }

                    if (!ownerIDs.contains(eventUserIID) && ownerCommands.contains(command))
                    {
                        chat.sendMessage(channelName, "4Head You don't have any permission to use owner commands :P");
                        return;
                    }

                    String[] prefixedMessagePartsRaw = message.split(" ");
                    String[] prefixedMessageParts = Arrays.stream(prefixedMessagePartsRaw)
                            .filter(messagePart -> !messagePart.isBlank())
                            .map(String::strip)
                            .toArray(String[]::new);

                    if (!command.isBlank() && !onMessage(command, event, prefixedMessageParts, messageParts))
                    {
                        chat.sendMessage(channelName, STR."Sadeg '\{command}' command wasn't found.");
                    }
                }
            }

            List<Triple<String, String, Boolean>> keywords = SQLUtils.getKeywords(channelIID);

            for (Triple<String, String, Boolean> keyword : keywords)
            {
                String kw = keyword.getLeft();
                String kwMessage = keyword.getMiddle();
                boolean exactMatch = keyword.getRight();

                if ((message.equals(kw) && exactMatch) || (message.contains(kw) && !exactMatch))
                {
                    chat.sendMessage(channelName, kwMessage);
                    return;
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            if (!channelName.equalsIgnoreCase("ApuJar"))
            {
                chat.sendMessage("ApuJar", STR."\{channelName} Weird Error while trying to execute an command FeelsGoodMan \{error}");
            }

            if (channelName.equalsIgnoreCase("ApuJar"))
            {
                chat.sendMessage("ApuJar", STR."Weird Error while trying to execute an command FeelsGoodMan \{error}");
            }

            e.printStackTrace();
        }
    }

    @NonNull
    public static ConcurrentHashMap<List<String>, ICommand> getCommandsAsMap()
    {
        return commands;
    }

    @NonNull
    public static HashSet<String> getCommands()
    {
        HashSet<String> commandSet = new HashSet<>();

        for (List<String> commandKeys : commands.keySet())
        {
            commandSet.addAll(commandKeys);
        }

        return commandSet;
    }
}
