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
package dev.blocky.twitch.commands.games;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.serialization.TicTacToe;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class TicTacToeCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        TicTacToe ticTacToe = SQLUtils.getTicTacToeGame(channelIID);

        if (ticTacToe != null)
        {
            sendChatMessage(channelID, "NOIDONTTHINKSO There can only be one game at a time in a channel.");
            return;
        }

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        String userToPlayWith = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToPlayWith))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToPlayWith = retrieveUserList(client, userToPlayWith);

        if (usersToPlayWith.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToPlayWith}' found.");
            return;
        }

        User user = usersToPlayWith.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        if (userIID == eventUserIID)
        {
            sendChatMessage(channelID, "UHM You can't play with yourself.");
            return;
        }

        String playerIDs = STR."\{eventUserID},\{userID}";

        LocalDateTime localDateTime = LocalDateTime.now();

        String nextUserName = userDisplayName;
        int nextUserID = userIID;

        if (userIID == 896181679)
        {
            nextUserName = eventUserName;
            nextUserID = eventUserIID;

            sendChatMessage(channelID, STR."OH \{eventUserName} You wanna fight me huh? Get ready, cause this shit's about to get heavy AlienDance");
        }

        SQLite.onUpdate(STR."INSERT INTO tictactoe(userID, playerIDs, board, nextUserID, round, startedAt) VALUES(\{channelID}, '\{playerIDs}', '[0, 0, 0, 0, 0, 0, 0, 0, 0]', \{nextUserID}, 1, '\{localDateTime}')");

        for (int i = 0; i < 3; i++)
        {
            sendChatMessage(channelID, "\u2B1C\u2B1C\u2B1C");
            TimeUnit.MILLISECONDS.sleep(500);
        }

        sendChatMessage(channelID, STR."PogU It's your turn \{nextUserName} NOWAYING Use the 'tic' command with a value between 1 and 9.");
    }
}
