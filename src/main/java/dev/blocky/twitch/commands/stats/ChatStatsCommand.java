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
import dev.blocky.api.entities.stats.StreamElementsChatter;
import dev.blocky.api.entities.stats.StreamElementsEmote;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ChatStatsCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToGetChatStatsFrom = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToGetChatStatsFrom))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToGetChatStatsFrom = retrieveUserList(client, userToGetChatStatsFrom);

        if (usersToGetChatStatsFrom.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToGetChatStatsFrom}' found.");
            return false;
        }

        User user = usersToGetChatStatsFrom.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        StreamElementsChatStats streamElementsChatStats = ServiceProvider.getChatStats(userLogin, 1);

        if (streamElementsChatStats == null)
        {
            sendChatMessage(channelID, "UNLUCKY No streamelements chatstats for user found.");
            return false;
        }

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        int totalMessages = streamElementsChatStats.getTotalMessageCount();
        int uniqueChatter = streamElementsChatStats.getUniqueChatterCount();

        List<StreamElementsChatter> chatters = streamElementsChatStats.getChatters();
        StreamElementsChatter topChatter = chatters.getFirst();
        String topChatterLogin = topChatter.getUserLogin();
        int topChatterMessageCount = topChatter.getMessageCount();

        String formattedTotalMessages = decimalFormat.format(totalMessages);
        String formattedUniqueChatter = decimalFormat.format(uniqueChatter);
        String formattedTopChatterMessageCount = decimalFormat.format(topChatterMessageCount);

        String messageToSend = STR."peepoChat Here are the chatstats for \{userDisplayName} \uD83D\uDC49 Total messages: \{formattedTotalMessages} Unique chatter: \{formattedUniqueChatter} Chatter #1: \{topChatterLogin} - \{formattedTopChatterMessageCount}";

        List<StreamElementsEmote> twitchEmotes = streamElementsChatStats.getTwitchEmotes();
        List<StreamElementsEmote> bttvEmotes = streamElementsChatStats.getBTTVEmotes();
        List<StreamElementsEmote> ffzEmotes = streamElementsChatStats.getFFZEmotes();
        List<StreamElementsEmote> sevenTVEmotes = streamElementsChatStats.getSevenTVEmotes();

        String topTwitchEmotes = getEmotesFormatted(twitchEmotes, 1);
        String topBTTVEmotes = getEmotesFormatted(bttvEmotes, 1);
        String topFFZEmotes = getEmotesFormatted(ffzEmotes, 1);
        String topSevenTVEmotes = getEmotesFormatted(sevenTVEmotes, 1);

        String topEmotes = "";

        if (topTwitchEmotes != null)
        {
            topEmotes = STR."Twitch: \{topTwitchEmotes}";
        }

        if (topBTTVEmotes != null)
        {
            topEmotes = STR."\{topEmotes} BTTV: \{topTwitchEmotes}";
        }

        if (topFFZEmotes != null)
        {
            topEmotes = STR."\{topEmotes} FFZ: \{topTwitchEmotes}";
        }

        if (topSevenTVEmotes != null)
        {
            topEmotes = STR."\{topEmotes} (7TV) : \{topTwitchEmotes}";
        }

        messageToSend += STR." \{topEmotes}";

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
