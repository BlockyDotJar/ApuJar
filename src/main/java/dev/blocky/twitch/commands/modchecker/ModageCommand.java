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
package dev.blocky.twitch.commands.modchecker;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ModageCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsDankMan Please specify a user.");
            return false;
        }

        String userToCheck = getUserAsString(messageParts, 1);
        String secondUserToCheck = getSecondUserAsString(messageParts, eventUserName);

        if (userToCheck.equalsIgnoreCase(eventUserName) && secondUserToCheck.equalsIgnoreCase(eventUserName))
        {
            sendChatMessage(channelID, "DIESOFCRINGE You can't be mod in your own chat.");
            return false;
        }

        if (userToCheck.equalsIgnoreCase(secondUserToCheck))
        {
            sendChatMessage(channelID, STR."FeelsDankMan \{userToCheck} can't be mod in his/her own chat.");
            return false;
        }

        if (!isValidUsername(userToCheck) || !isValidUsername(secondUserToCheck))
        {
            sendChatMessage(channelID, "o_O One or both usernames don't match with RegEx R-)");
            return false;
        }

        List<User> usersToCheck = retrieveUserList(client, userToCheck);
        List<User> secondUsersToCheck = retrieveUserList(client, secondUserToCheck);

        if (usersToCheck.isEmpty() || secondUsersToCheck.isEmpty())
        {
            sendChatMessage(channelID, ":| One or both users not found.");
            return false;
        }

        User user = usersToCheck.getFirst();
        String userDisplayName = user.getDisplayName();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        User secondUser = secondUsersToCheck.getFirst();
        String secondUserDisplayName = secondUser.getDisplayName();
        String secondUserID = secondUser.getId();
        int secondUserIID = Integer.parseInt(secondUserID);

        List<ModCheckerUser> modCheckerUsers = ServiceProvider.getModCheckerUsers(secondUserIID);

        if (modCheckerUsers == null || modCheckerUsers.isEmpty())
        {
            sendChatMessage(channelID, STR."ohh User \{secondUserDisplayName} doesn't get logged by modChecker at the moment or the user opted himself/herself out from the tracking. Please try searching the user FeelsOkayMan \uD83D\uDC49 https://mdc.lol/c");
            return false;
        }

        List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(secondUserIID);

        if (modCheckerMods == null || modCheckerMods.isEmpty())
        {
            sendChatMessage(channelID, STR."Sadeg There are no mods in \{secondUserDisplayName}'s chat at the moment.");
            return false;
        }

        Optional<ModCheckerUser> optionalModCheckerMod = modCheckerMods.stream().filter(tm ->
        {
            int modID = tm.getUserID();
            return modID ==  userIID;
        }).findFirst();

        ModCheckerUser modCheckerMod = optionalModCheckerMod.orElse(null);

        if (modCheckerMod == null)
        {
            return sendChatMessage(channelID, STR."forsenLaughingAtYou \{userDisplayName} isn't mod in \{secondUserDisplayName}'s chat at the moment.");
        }

        Date grantedAt = modCheckerMod.getGrantedAt();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedGrantDate = formatter.format(grantedAt);

        String messageToSend = STR."NOWAYING \{userDisplayName} mods \{secondUserDisplayName}'s chat since \{formattedGrantDate} PogU";
        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
