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
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.*;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SevenTVEmoteChangeAction;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class SevenTVAddCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        EventUser eventUser = event.getUser();
        String eventUserName = eventUser.getName();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a emote.");
            return;
        }

        String emoteToAdd = messageParts[1];

        if (emoteToAdd.matches("https?://7tv.app/emotes/[a-z\\d]{24}"))
        {
            sendChatMessage(channelID, "FeelsOkayMan Please use the '7tvaddlink' command to add emotes from a link.");
            return;
        }

        String emoteAlias = emoteToAdd;

        if (messageParts.length >= 3)
        {
            emoteAlias = messageParts[2];
        }

        Map<Integer, String> owners = SQLUtils.getOwners();
        Set<Integer> ownerIDs = owners.keySet();

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(channelIID, eventUserIID);

        if (!channelName.equalsIgnoreCase(eventUserName) && !ownerIDs.contains(eventUserIID) && !isAllowedEditor)
        {
            sendChatMessage(channelID, "ManFeels You can't add emotes, because you aren't the broadcaster, 7tv editor or the broadcaster allowed user.");
            return;
        }

        SevenTV sevenTV = SevenTVUtils.searchEmotes(emoteToAdd);
        SevenTVData sevenTVData = sevenTV.getData();
        SevenTVEmoteSearch seventTVEmoteSearch = sevenTVData.getEmotes();

        if (seventTVEmoteSearch == null)
        {
            sendChatMessage(channelID, STR."FeelsGoodMan No emote with name '\{emoteToAdd}' found.");
            return;
        }

        List<SevenTVEmote> sevenTVEmotes = seventTVEmoteSearch.getItems();
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToAdd);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            sendChatMessage(channelID, STR."FeelsGoodMan No emote with name '\{emoteToAdd}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getEmoteID();

        boolean isAnimated = sevenTVEmote.isAnimated();
        boolean isListed = sevenTVEmote.isListed();
        boolean isPrivate = sevenTVEmote.getEmoteFlags() == 1;

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, channelIID);

        if (sevenTVTwitchUser == null)
        {
            return;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVTwitchUser.getCurrentEmoteSet();

        if (sevenTVEmoteSet == null)
        {
            sendChatMessage(channelID, "FeelsGoodMan No emote active emote-set found.");
            return;
        }

        String sevenTVEmoteSetID = sevenTVEmoteSet.getEmoteSetID();

        SevenTV emoteAddition = SevenTVUtils.changeEmote(SevenTVEmoteChangeAction.ADD, sevenTVEmoteSetID, sevenTVEmoteID, emoteAlias);

        List<SevenTVError> errors = emoteAddition.getErrors();

        if (SevenTVUtils.checkErrors(channelID, errors))
        {
            return;
        }

        sendChatMessage(channelID, STR."SeemsGood Successfully added (7TV) emote \{emoteAlias} (Private: \{isPrivate}, Animated: \{isAnimated}, Listed: \{isListed})");
    }
}
