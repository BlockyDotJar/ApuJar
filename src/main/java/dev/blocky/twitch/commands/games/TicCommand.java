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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.sql.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.TTTMinimaxAI;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.blocky.twitch.utils.TwitchUtils.retrieveUserListByID;

public class TicCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
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

        HashSet<Integer> ticTacToeGames = SQLUtils.getTicTacToeGames();

        if (!ticTacToeGames.contains(channelIID))
        {
            chat.sendMessage(channelName, "FeelsDankMan Use the 'tictactoe' command to start a game.");
            return;
        }

        int currentUserID = SQLUtils.getTicTacToeNexUserID(channelIID);

        if (eventUserIID != currentUserID)
        {
            chat.sendMessage(channelName, STR."WTF It's not your turn \{eventUserName}.");
            return;
        }

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a field.");
            return;
        }

        String position = messageParts[1];

        if (position.length() > 1)
        {
            chat.sendMessage(channelName, "FeelsMan Invalid value specified. (Choose between 1 and 9)");
            return;
        }

        char pos = position.charAt(0);

        if (!Character.isDigit(pos))
        {
            chat.sendMessage(channelName, "FeelsMan Invalid value specified. (Choose between 1 and 9)");
            return;
        }

        int index = Integer.parseInt(position);

        int[] board = SQLUtils.getTicTacToeBoard(channelIID);

        if (board[index - 1] != 0)
        {
            chat.sendMessage(channelName, STR."FeelsDankMan Position \{position} is already specified.");
            return;
        }

        board[index - 1] = 1;

        List<Integer> playerIDs = SQLUtils.getTicTacToePlayerIDs(channelIID);

        int player1ID = playerIDs.getFirst();
        int player2ID = playerIDs.getLast();

        if (player1ID != currentUserID)
        {
            board[index - 1] = 2;
        }

        int nextUserID = player1ID;

        if (player2ID != currentUserID)
        {
            nextUserID = player2ID;
        }

        int round = SQLUtils.getTicTacToeRound(channelIID);

        String startedAt = SQLUtils.getTicTacToeStartedAt(channelIID);

        LocalDateTime ttcStartedAt = LocalDateTime.parse(startedAt);
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(ttcStartedAt, now);

        long SS = duration.toSecondsPart();
        long MM = duration.toMinutes();

        boolean aiTurn = nextUserID == 896181679;

        if (aiTurn && round != 9)
        {
            nextUserID = eventUserIID;

            TTTMinimaxAI ai = new TTTMinimaxAI(2);
            int move = ai.doMove(board, 2);

            board[move] = 2;

            round++;
        }

        String[] boardValues = Arrays.stream(board)
                .mapToObj(i ->
                {
                    if (i == 0)
                    {
                        return "\u2B1C";
                    }

                    if (i == 1)
                    {
                        return "\u274C";
                    }

                    return "\u2B55";
                })
                .toArray(String[]::new);

        for (int i = 0; i < 9; i += 3)
        {
            String left = boardValues[i];
            String middle = boardValues[i + 1];
            String right = boardValues[i + 2];

            String messageToSend = STR."\{left}\{middle}\{right}";

            chat.sendMessage(channelName, messageToSend);

            TimeUnit.MILLISECONDS.sleep(500);
        }

        List<User> users = retrieveUserListByID(client, nextUserID);

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();

        int nextRound = round + 1;

        if (round == 9)
        {
            SQLite.onUpdate(STR."DELETE FROM tictactoe WHERE userID = \{channelID}");

            chat.sendMessage(channelName, STR."UNLUCKY The tic tac toe session ended with a tie. (Game lasted \{MM}:\{SS} | 9 rounds)");
            return;
        }

        if (TTTMinimaxAI.checkForWinner(board) == 1)
        {
            SQLite.onUpdate(STR."DELETE FROM tictactoe WHERE userID = \{channelID}");

            chat.sendMessage(channelName, STR."Pag \{eventUserName} (\u274C) won the game. (Game lasted \{MM}:\{SS} | \{round} rounds)");
            return;
        }

        if (TTTMinimaxAI.checkForWinner(board) == 2)
        {
            SQLite.onUpdate(STR."DELETE FROM tictactoe WHERE userID = \{channelID}");

            if (aiTurn)
            {
                chat.sendMessage(channelName, STR."EZ I (\u2B55) won the game. (Game lasted \{MM}:\{SS} | \{round} rounds)");
                return;
            }

            chat.sendMessage(channelName, STR."Pag \{eventUserName} (\u2B55) won the game. (Game lasted \{MM}:\{SS} | \{round} rounds)");
            return;
        }

        String newBoard = Arrays.toString(board);

        String messageToSend = STR."PogU Its your turn \{userDisplayName} NOWAYING Use the 'tic' command with a value between 1 and 9. (Round \{nextRound})";

        SQLite.onUpdate(STR."UPDATE tictactoe SET board = '\{newBoard}', nextUserID = \{nextUserID}, round = \{nextRound} WHERE userID = \{channelID}");

        chat.sendMessage(channelName, messageToSend);
    }
}
