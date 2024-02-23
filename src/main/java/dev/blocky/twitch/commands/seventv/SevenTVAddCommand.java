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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.*;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SevenTVAddCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
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

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a emote.");
            return;
        }

        String emoteToAdd = messageParts[1];
        String emoteAlias = emoteToAdd;

        if (messageParts.length == 3)
        {
            emoteAlias = messageParts[2];
        }

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

        SevenTV sevenTV = SevenTVUtils.getUser(channelName);
        SevenTVData sevenTVData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVData.getUsers();
        List<SevenTVUser> sevenTVUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, channelName);

        if (sevenTVUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No user with name '\{channelName}' found.");
            return;
        }

        SevenTVUser sevenTVUser = sevenTVUsersFiltered.getFirst();
        String sevenTVUserDisplayName = sevenTVUser.getDisplayName();
        String sevenTVUserID = sevenTVUser.getID();

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(channelIID, eventUserIID, sevenTVUserID, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !ownerIDs.contains(eventUserIID) && !isAllowedEditor)
        {
            chat.sendMessage(channelName, "ManFeels You can't add emotes, because you aren't a broadcaster, 7tv editor or a broadcaster allowed user.");
            return;
        }

        sevenTV = SevenTVUtils.searchEmotes(emoteToAdd);
        sevenTVData = sevenTV.getData();
        SevenTVEmoteSearch seventTVEmoteSearch = sevenTVData.getEmotes();
        ArrayList<SevenTVEmote> sevenTVEmotes = seventTVEmoteSearch.getItems();
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToAdd);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."FeelsGoodMan No emote with name '\{emoteToAdd}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getID();

        sevenTVEmote = ServiceProvider.getSevenTVEmote(sevenTVEmoteID);

        boolean isAnimated = sevenTVEmote.isAnimated();
        boolean isListed = sevenTVEmote.isListed();
        boolean isPrivate = sevenTVEmote.getFlags() == 1;

        sevenTVUser = ServiceProvider.getSevenTVUser(sevenTVUserID);

        SevenTVUserConnection sevenTVConnection = SevenTVUtils.getSevenTVUserConnection(sevenTVUser);

        if (sevenTVConnection == null)
        {
            chat.sendMessage(channelName, STR."undefined No emote set found for \{sevenTVUserDisplayName}.");
            return;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVConnection.getEmoteSet();
        String sevenTVEmoteSetID = sevenTVEmoteSet.getID();

        SevenTV emoteAddition = SevenTVUtils.changeEmote(SevenTVEmoteChangeAction.ADD, sevenTVEmoteSetID, sevenTVEmoteID, emoteAlias);

        ArrayList<SevenTVError> errors = emoteAddition.getErrors();

        if (errors != null)
        {
            SevenTVError error = errors.getFirst();
            SevenTVErrorExtension extension = error.getExtension();
            String message = extension.getMessage();
            int code = extension.getCode();

            chat.sendMessage(channelName, STR."(7TV) error (\{code}) undefined \ud83d\udc4d \{message}");
            return;
        }

        chat.sendMessage(channelName, STR."SeemsGood Successfully added (7TV) emote \{emoteAlias} (Private: \{isPrivate}, Animated: \{isAnimated}, Listed: \{isListed})");
    }
}
