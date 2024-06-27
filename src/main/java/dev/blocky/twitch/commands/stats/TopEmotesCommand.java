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
package dev.blocky.twitch.commands.stats;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.stats.StreamElementsChatStats;
import dev.blocky.api.entities.stats.StreamElementsEmote;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class TopEmotesCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String emoteService = "*";

        if (messageParts.length > 1)
        {
            emoteService = messageParts[1];

            if (!emoteService.matches("^(twitch|ttv|7tv|stv|seventv|betterttv|bttv|frankerfacez|ffz)$"))
            {
                sendChatMessage(channelID, "FeelsMan Please specify a valid emote service. (7tv, bttv or ffz)");
                return;
            }
        }

        String userToGetTopEmotesFrom = getSecondUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToGetTopEmotesFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToGetTopEmotesFrom = retrieveUserList(client, userToGetTopEmotesFrom);

        if (usersToGetTopEmotesFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetTopEmotesFrom}' found.");
            return;
        }

        User user = usersToGetTopEmotesFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        StreamElementsChatStats streamElementsChatStats = ServiceProvider.getChatStats(userLogin, 5);

        if (streamElementsChatStats == null)
        {
            sendChatMessage(channelID, "UNLUCKY No streamelements chatstats for user found.");
            return;
        }

        String messageToSend = STR."peepoChat Here are the top emotes for \{userDisplayName} \uD83D\uDC49";

        List<StreamElementsEmote> twitchEmotes = streamElementsChatStats.getTwitchEmotes();
        List<StreamElementsEmote> bttvEmotes = streamElementsChatStats.getBTTVEmotes();
        List<StreamElementsEmote> ffzEmotes = streamElementsChatStats.getFFZEmotes();
        List<StreamElementsEmote> sevenTVEmotes = streamElementsChatStats.getSevenTVEmotes();

        String topEmotes = switch (emoteService)
        {
            case "twitch", "ttv" -> getEmotesFormatted(twitchEmotes, 5);
            case "betterttv", "bttv" -> getEmotesFormatted(bttvEmotes, 5);
            case "frankerfacez", "ffz" -> getEmotesFormatted(ffzEmotes, 5);
            case "7tv", "stv", "seventv" -> getEmotesFormatted(sevenTVEmotes, 5);
            default ->
            {
                String topTwitchEmotes = getEmotesFormatted(twitchEmotes, 1);
                String topBTTVEmotes = getEmotesFormatted(bttvEmotes, 1);
                String topFFZEmotes = getEmotesFormatted(ffzEmotes, 1);
                String topSevenTVEmotes = getEmotesFormatted(sevenTVEmotes, 1);

                String topEmotesRaw = "";

                if (topTwitchEmotes != null)
                {
                    topEmotesRaw = STR."Twitch: \{topTwitchEmotes}";
                }

                if (topBTTVEmotes != null)
                {
                    topEmotesRaw = STR."\{topEmotesRaw} BTTV: \{topTwitchEmotes}";
                }

                if (topFFZEmotes != null)
                {
                    topEmotesRaw = STR."\{topEmotesRaw} FFZ: \{topTwitchEmotes}";
                }

                if (topSevenTVEmotes != null)
                {
                    topEmotesRaw = STR."\{topEmotesRaw} (7TV) : \{topTwitchEmotes}";
                }

                yield topEmotesRaw;
            }
        };

        if (topEmotes.isBlank())
        {
            sendChatMessage(channelID, "Sadeg No top emotes found.");
            return;
        }

        messageToSend += STR." \{topEmotes}";

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
