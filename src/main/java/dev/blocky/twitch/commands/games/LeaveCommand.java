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
import dev.blocky.twitch.utils.TwitchUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class LeaveCommand implements ICommand
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
            chat.sendMessage(channelName, "FeelsDankMan No tictactoe game found.");
            return;
        }

        List<Integer> playerIDs = SQLUtils.getTicTacToePlayerIDs(channelIID);

        int player1ID = playerIDs.getFirst();
        int player2ID = playerIDs.getLast();

        if (eventUserIID != player1ID && eventUserIID != player2ID)
        {
            chat.sendMessage(channelName, "FeelsDankMan You don't even play along.");
            return;
        }

        int round = SQLUtils.getTicTacToeRound(channelIID);

        String startedAt = SQLUtils.getTicTacToeStartedAt(channelIID);

        LocalDateTime ttcStartedAt = LocalDateTime.parse(startedAt);
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(ttcStartedAt, now);

        long SS = duration.toSecondsPart();
        long MM = duration.toMinutes();

        int nextUserID = player1ID;

        if (player2ID != eventUserIID)
        {
            nextUserID = player2ID;
        }

        List<User> users = TwitchUtils.retrieveUserListByID(client, nextUserID);

        User user = users.getFirst();
        String userDisplayName = user.getDisplayName();

        SQLite.onUpdate(STR."DELETE FROM tictactoe WHERE userID = \{channelID}");

        chat.sendMessage(channelName, STR."YIPPEE \{eventUserName} left the game. \{userDisplayName} won the game! happie (Game lasted \{MM}:\{SS} | \{round} rounds)");
    }
}
