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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.*;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVUserEmoteCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        if (messageParts.length == 1)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a user.");
            return;
        }

        String userToYoink = getUserAsString(messageParts, 1);

        if (messageParts.length == 2)
        {
            chat.sendMessage(channelName, "FeelsMan Please specify a emote.");
            return;
        }

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
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        String emoteToGetURLFrom = messageParts[2];

        SevenTV sevenTV = SevenTVUtils.getUser(userLogin);
        SevenTVData sevenTVData = sevenTV.getData();
        ArrayList<SevenTVUser> sevenTVUsers = sevenTVData.getUsers();
        List<SevenTVUser> sevenTVUsersFiltered = SevenTVUtils.getFilteredUsers(sevenTVUsers, userLogin);

        if (sevenTVUsersFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."undefined No (7TV) user with name '\{userLogin}' found.");
            return;
        }

        SevenTVUser sevenTVUser = sevenTVUsersFiltered.getFirst();
        String sevenTVUserDisplayName = sevenTVUser.getUserDisplayName();
        String sevenTVUserID = sevenTVUser.getUserID();

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
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToGetURLFrom);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            chat.sendMessage(channelName, STR."FeelsGoodMan No emote with name '\{emoteToGetURLFrom}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getEmoteID();

        chat.sendMessage(channelName, STR."SeemsGood Here is your 7tv emote link for the ' \{emoteToGetURLFrom} ' emote from \{userDisplayName} \uD83D\uDC49 https://7tv.app/emotes/\{sevenTVEmoteID}");
    }
}
