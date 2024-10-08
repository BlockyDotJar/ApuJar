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
package dev.blocky.twitch.commands.seventv;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.SevenTVEmote;
import dev.blocky.api.entities.seventv.SevenTVEmoteSet;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SevenTVEmoteCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a emote.");
            return false;
        }

        String emoteToGetURLFrom = messageParts[1];

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, channelIID);

        if (sevenTVTwitchUser == null)
        {
            return false;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVTwitchUser.getCurrentEmoteSet();

        if (sevenTVEmoteSet == null)
        {
            sendChatMessage(channelID, "FeelsGoodMan No emote active emote-set found.");
            return false;
        }

        String sevenTVEmoteSetID = sevenTVEmoteSet.getEmoteSetID();

        sevenTVEmoteSet = ServiceProvider.getSevenTVEmoteSet(channelIID, sevenTVEmoteSetID);

        List<SevenTVEmote> sevenTVEmotes = sevenTVEmoteSet.getEmotes();
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToGetURLFrom, false);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            sendChatMessage(channelID, STR."FeelsGoodMan No emote with name '\{emoteToGetURLFrom}' found.");
            return false;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        SevenTVEmote realEmote = sevenTVEmote.getData();

        String sevenTVEmoteID = realEmote.getEmoteID();

        boolean isAnimated = realEmote.isAnimated();
        boolean isListed = realEmote.isListed();
        boolean isPrivate = realEmote.getEmoteFlags() == 1;

        return sendChatMessage(channelID, STR."SeemsGood Here is your 7tv emote link for the ' \{emoteToGetURLFrom} ' emote (Private: \{isPrivate}, Animated: \{isAnimated}, Listed: \{isListed}) \uD83D\uDC49 https://7tv.app/emotes/\{sevenTVEmoteID}");
    }
}
