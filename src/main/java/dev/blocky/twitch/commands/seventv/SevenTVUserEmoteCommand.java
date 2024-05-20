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
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.seventv.SevenTVEmote;
import dev.blocky.api.entities.seventv.SevenTVEmoteSet;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.twitch.interfaces.ICommand;
import dev.blocky.twitch.utils.SevenTVUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.utils.TwitchUtils.*;

public class SevenTVUserEmoteCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        EventChannel channel = event.getChannel();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a user.");
            return;
        }

        String userToYoink = getUserAsString(messageParts, 1);

        if (messageParts.length == 2)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a emote.");
            return;
        }

        if (!isValidUsername(userToYoink))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToYoink = retrieveUserList(client, userToYoink);

        if (usersToYoink.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToYoink}' found.");
            return;
        }

        User user = usersToYoink.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        String emoteToGetURLFrom = messageParts[2];

        SevenTVTwitchUser sevenTVTwitchUser = ServiceProvider.getSevenTVUser(channelIID, userIID);

        if (sevenTVTwitchUser == null)
        {
            return;
        }

        SevenTVEmoteSet sevenTVEmoteSet = sevenTVTwitchUser.getCurrentEmoteSet();
        List<SevenTVEmote> sevenTVEmotes = sevenTVEmoteSet.getEmotes();
        List<SevenTVEmote> sevenTVEmotesFiltered = SevenTVUtils.getFilteredEmotes(sevenTVEmotes, emoteToGetURLFrom);

        if (sevenTVEmotesFiltered.isEmpty())
        {
            sendChatMessage(channelID, STR."FeelsGoodMan No emote with name '\{emoteToGetURLFrom}' found.");
            return;
        }

        SevenTVEmote sevenTVEmote = sevenTVEmotesFiltered.getFirst();
        String sevenTVEmoteID = sevenTVEmote.getEmoteID();

        boolean isAnimated = sevenTVEmote.isAnimated();
        boolean isListed = sevenTVEmote.isListed();
        boolean isPrivate = sevenTVEmote.getEmoteFlags() == 1;

        sendChatMessage(channelID, STR."SeemsGood Here is your 7tv emote link for the ' \{emoteToGetURLFrom} ' emote from \{userDisplayName} (Private: \{isPrivate}, Animated: \{isAnimated}, Listed: \{isListed}) \uD83D\uDC49 https://7tv.app/emotes/\{sevenTVEmoteID}");
    }
}
