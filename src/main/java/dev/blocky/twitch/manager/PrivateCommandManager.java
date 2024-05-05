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
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.helix.TwitchHelix;
import dev.blocky.twitch.commands.spotify.AddSpotifyUserPrivateCommand;
import dev.blocky.twitch.commands.spotify.DeleteSpotifyUserPrivateCommand;
import dev.blocky.twitch.commands.weather.DeleteLocationPrivateCommand;
import dev.blocky.twitch.commands.weather.HideLocationPrivateCommand;
import dev.blocky.twitch.commands.weather.LocationPrivateCommand;
import dev.blocky.twitch.commands.weather.SetLocationPrivateCommand;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blocky.twitch.utils.TwitchUtils.sendPrivateMessage;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class PrivateCommandManager
{
    private static ConcurrentHashMap<List<String>, IPrivateCommand> privateCommands;
    private final TwitchHelix helix;

    public PrivateCommandManager(@NonNull SimpleEventHandler eventHandler, @NonNull TwitchHelix helix)
    {
        this.helix = helix;

        eventHandler.onEvent(PrivateMessageEvent.class, this::onPrivateMessage);

        privateCommands = new ConcurrentHashMap<>();

        privateCommands.put(List.of("addspotifyuser", "addspotifyu"), new AddSpotifyUserPrivateCommand());
        privateCommands.put(List.of("deletespotifyuser", "delspotifyuser", "delspotifyu"), new DeleteSpotifyUserPrivateCommand());

        privateCommands.put(List.of("setlocation"), new SetLocationPrivateCommand());
        privateCommands.put(List.of("hidelocation"), new HideLocationPrivateCommand());
        privateCommands.put(List.of("location"), new LocationPrivateCommand());
        privateCommands.put(List.of("deletelocation", "dellocation"), new DeleteLocationPrivateCommand());
    }

    boolean onPrivateMessage(@NonNull String commandOrAlias, @NonNull PrivateMessageEvent event, @NonNull String[] messageParts) throws Exception
    {
        for (List<String> privateCommandKeys : privateCommands.keySet())
        {
            boolean commandExists = privateCommandKeys.stream().anyMatch(commandOrAlias::equalsIgnoreCase);

            if (commandExists)
            {
                IPrivateCommand command = privateCommands.get(privateCommandKeys);
                command.onPrivateCommand(event, helix, messageParts);
                return true;
            }
        }
        return false;
    }

    public void onPrivateMessage(@NonNull PrivateMessageEvent event)
    {
        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        try
        {
            String message = event.getMessage();

            String actualPrefix = SQLUtils.getPrefix(eventUserIID);

            Pattern PREFIX_PATTERN = Pattern.compile("(.*)?prefix(.*)?", CASE_INSENSITIVE);
            Matcher PREFIX_MATCHER = PREFIX_PATTERN.matcher(message);

            SQLUtils.correctUserLogin(eventUserIID, eventUserName);

            if (PREFIX_MATCHER.matches())
            {
                sendPrivateMessage(helix, eventUserID, STR."4Head The prefix for your chat is is '\{actualPrefix}'");
                return;
            }

            String commandRaw = message.strip();
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
                    sendPrivateMessage(helix, eventUserID, "4Head You don't have any permission to do that :P");
                    return;
                }

                if (!command.isBlank() && !onPrivateMessage(command, event, messageParts))
                {
                    sendPrivateMessage(helix, eventUserID, STR.":/ '\{command}' command wasn't found.");
                }
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();
            sendPrivateMessage(helix, eventUserID, STR."SirMad Error while trying to execute an command PogChamp \{error}");

            e.printStackTrace();
        }
    }
}
