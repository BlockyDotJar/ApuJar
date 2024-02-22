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

public class SevenTVRemoveCommand implements ICommand
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

        String emoteToRemove = messageParts[1];

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

        SevenTV sevenTV = SevenTVUtils.getUser(channelName);
        SevenTVData sevenTVUserData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVUserData.getUsers();
        List<SevenTVUser> sevenTVUsersSorted = sevenTVUsers.stream()
                .filter(sevenTVUser ->
                {
                    String sevenTVUsername = sevenTVUser.getUsername();
                    return sevenTVUsername.equalsIgnoreCase(channelName);
                })
                .toList();

        if (sevenTVUsersSorted.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No user with name '\{channelName}' found.");
            return;
        }

        SevenTVUser sevenTVUser = sevenTVUsersSorted.getFirst();
        String sevenTVUserDisplayName = sevenTVUser.getDisplayName();
        String sevenTVUserID = sevenTVUser.getID();

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(chat, channelIID, eventUserIID, sevenTVUserID, channelName, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !ownerIDs.contains(eventUserIID) && !isAllowedEditor)
        {
            chat.sendMessage(channelName, "ManFeels You can't remove emotes, because you aren't a broadcaster, 7tv editor or a broadcaster allowed user.");
            return;
        }

        sevenTVUser = ServiceProvider.getSevenTVUser(sevenTVUserID);
        ArrayList<SevenTVConnection> sevenTVConnections = sevenTVUser.getConnections();

        SevenTVConnection sevenTVConnection = null;

        for (SevenTVConnection connection : sevenTVConnections)
        {
            String platform = connection.getPlatform();

            if (platform.equals("TWITCH"))
            {
                sevenTVConnection = connection;
                break;
            }
        }

        if (sevenTVConnection == null)
        {
            chat.sendMessage(channelName, STR."undefined No emote set found for \{sevenTVUserDisplayName}.");
            return;
        }

        SevenTVEmoteSet sevenTVChannelEmoteSet = sevenTVConnection.getEmoteSet();
        String sevenTVChannelEmoteSetID = sevenTVChannelEmoteSet.getID();

        SevenTV sevenTVEmoteSet = ServiceProvider.getSevenTVEmoteSet(sevenTVChannelEmoteSetID);
        ArrayList<SevenTVEmote> sevenTVEmotes = sevenTVEmoteSet.getEmotes();
        List<SevenTVEmote> sevenTVEmotesSorted = sevenTVEmotes.stream()
                .filter(sevenTVEmote ->
                {
                    String sevenTVEmoteName = sevenTVEmote.getName();
                    return sevenTVEmoteName.equalsIgnoreCase(emoteToRemove);
                })
                .toList();

        if (sevenTVEmotesSorted.isEmpty())
        {
            chat.sendMessage(channelName, STR."FeelsGoodMan No emote with name '\{emoteToRemove}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesSorted.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getID();

        SevenTV emoteAddition = SevenTVUtils.changeEmote(SevenTVEmoteChangeAction.REMOVE, sevenTVChannelEmoteSetID, sevenTVEmoteID, emoteToRemove);

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

        chat.sendMessage(channelName, STR."SeemsGood Successfully removed (7TV) emote \{emoteToRemove}.");
    }
}
