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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.*;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVYoinkCommand implements ICommand
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
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToYoink = getUserAsString(messageParts, 1);

        if (!isValidUsername(userToYoink))
        {
            chat.sendMessage(channelName, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToYoink = retrieveUserList(client, userToYoink);

        if (usersToYoink.isEmpty())
        {
            chat.sendMessage(channelName, STR.":| No user called '\{userToYoink}' found.");
            return;
        }

        User user = usersToYoink.getFirst();
        String userLogin = user.getLogin();

        SevenTV sevenTV = SevenTVUtils.getUser(userLogin);
        SevenTVData sevenTVData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVData.getUsers();
        List<SevenTVUser> sevenTVUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, userLogin);

        SevenTVUser sevenTVUser = sevenTVUsersFiltered.getFirst();
        String sevenTVUserDisplayName = sevenTVUser.getUserDisplayName();
        String sevenTVUserID = sevenTVUser.getUserID();

        if (sevenTVUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) user with name '\{userLogin}' found.");
            return;
        }

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a emote.");
            return;
        }

        String emoteToYoink = messageParts[2];
        String emoteAlias = emoteToYoink;

        if (messageParts.length >= 4)
        {
            emoteAlias = messageParts[3];
        }

        HashSet<Integer> ownerIDs = SQLUtils.getOwnerIDs();

        sevenTV = SevenTVUtils.getUser(channelName);
        sevenTVData = sevenTV.getData();
        sevenTVUsers = sevenTVData.getUsers();
        sevenTVUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, channelName);

        if (sevenTVUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) user with name '\{channelName}' found.");
            return;
        }

        sevenTVUser = sevenTVUsersFiltered.getFirst();
        String sevenTVChannelDisplayName = sevenTVUser.getUserDisplayName();
        String sevenTVChannelID = sevenTVUser.getUserID();

        boolean isAllowedEditor = SevenTVUtils.isAllowedEditor(channelIID, eventUserIID, sevenTVChannelID, eventUserName);

        if (!channelName.equalsIgnoreCase(eventUserName) && !ownerIDs.contains(eventUserIID) && !isAllowedEditor)
        {
            chat.sendMessage(channelName, "ManFeels You can't add emotes, because you aren't the broadcaster, 7tv editor or the broadcaster allowed user.");
            return;
        }

        sevenTVUser = ServiceProvider.getSevenTVUser(sevenTVUserID);

        SevenTVUserConnection sevenTVConnection = SevenTVUtils.getSevenTVUserConnection(sevenTVUser);

        if (sevenTVConnection == null)
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) emote set found for \{sevenTVUserDisplayName}.");
            return;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVConnection.getEmoteSet();
        String sevenTVEmoteSetID = sevenTVEmoteSet.getEmoteSetID();

        sevenTV = ServiceProvider.getSevenTVEmoteSet(sevenTVEmoteSetID);
        ArrayList<SevenTVEmote> sevenTVEmotes = sevenTV.getEmotes();
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToYoink);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."FeelsGoodMan No emote with name '\{emoteToYoink}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getEmoteID();

        sevenTVEmote = ServiceProvider.getSevenTVEmote(sevenTVEmoteID);

        boolean isAnimated = sevenTVEmote.isAnimated();
        boolean isListed = sevenTVEmote.isListed();
        boolean isPrivate = sevenTVEmote.getEmoteFlags() == 1;

        sevenTVUser = ServiceProvider.getSevenTVUser(sevenTVChannelID);

        sevenTVConnection = SevenTVUtils.getSevenTVUserConnection(sevenTVUser);

        if (sevenTVConnection == null)
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) emote set found for \{sevenTVChannelDisplayName}.");
            return;
        }

        sevenTVEmoteSet = sevenTVConnection.getEmoteSet();
        sevenTVEmoteSetID = sevenTVEmoteSet.getEmoteSetID();

        SevenTV emoteAddition = SevenTVUtils.changeEmote(SevenTVEmoteChangeAction.ADD, sevenTVEmoteSetID, sevenTVEmoteID, emoteAlias);

        ArrayList<SevenTVError> errors = emoteAddition.getErrors();

        if (errors != null)
        {
            SevenTVError error = errors.getFirst();
            SevenTVErrorExtension errorExtension = error.getErrorExtension();
            String errorMessage = errorExtension.getErrorMessage();
            int errorCode = errorExtension.getErrorCode();

            chat.sendMessage(channelName, STR."(7TV) error (\{errorCode}) undefined \ud83d\udc4d \{errorMessage}");
            return;
        }

        chat.sendMessage(channelName, STR."SeemsGood Successfully added (7TV) emote \{emoteAlias} (Private: \{isPrivate}, Animated: \{isAnimated}, Listed: \{isListed})");
    }
}
