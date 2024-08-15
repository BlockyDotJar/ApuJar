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
import dev.blocky.api.entities.blockyjar.KokbinPaste;
import dev.blocky.api.entities.ivr.IVRFounder;
import dev.blocky.api.entities.modchecker.ModCheckerUser;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ModCheckerCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToLookup = getParameterUserAsString(messageParts, "-ch(annel)?", eventUserName);

        if (!isValidUsername(userToLookup))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToLookup}' found.");
            return false;
        }

        User user = usersToLookup.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();
        String userID = user.getId();
        int userIID = Integer.parseInt(userID);

        List<ModCheckerUser> modCheckerUsers = ServiceProvider.getModCheckerUsers(userIID);

        if (modCheckerUsers == null || modCheckerUsers.isEmpty())
        {
            sendChatMessage(channelID, STR."ohh User \{userDisplayName} doesn't get logged by modChecker at the moment or the user opted himself/herself out from the tracking. Please try searching the user FeelsOkayMan \uD83D\uDC49 https://mdc.lol/c");
            return false;
        }

        String messageToSend = null;

        boolean hasChannelParameter = hasRegExParameter(messageParts, "-ch(annel)?");

        if (!hasChannelParameter)
        {
            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerUserMods(userIID);
            List<ModCheckerUser> modCheckerVIPs = ServiceProvider.getModCheckerUserVIPs(userIID);

            int modCount = modCheckerMods.size();
            int vipCount = modCheckerVIPs.size();

            messageToSend = STR."PogChamp \{userDisplayName} is moderator in \{modCount} and vip in \{vipCount} channel! o_O https://mdc.lol/u/\{userLogin}";
        }

        if (hasChannelParameter)
        {
            List<ModCheckerUser> modCheckerMods = ServiceProvider.getModCheckerChannelMods(userIID);
            List<ModCheckerUser> modCheckerVIPs = ServiceProvider.getModCheckerChannelVIPs(userIID);
            List<IVRFounder> ivrFounders = ServiceProvider.getIVRFounders(userLogin);

            int modCount = modCheckerMods.size();
            int vipCount = modCheckerVIPs.size();
            int founderCount = 0;

            String pasteKey = null;

            if (ivrFounders != null)
            {
                List<String> founders = ivrFounders.stream().map(IVRFounder::getUserLogin).toList();
                founderCount = ivrFounders.size();

                String foundersFormatted = String.join("\n", founders);

                String founder = STR."""
                Founder:

                \{foundersFormatted}
                """;

                KokbinPaste kokbinPaste = ServiceProvider.paste(founder);
                pasteKey = kokbinPaste.getKey();
            }

            messageToSend = STR."PogChamp \{userDisplayName} has \{modCount} moderators, \{vipCount} vips and \{founderCount} founder in his/her channel! o_O https://mdc.lol/c/\{userLogin}";

            if (pasteKey != null)
            {
                messageToSend += STR." / https://paste.blockyjar.dev/\{pasteKey}";
            }
        }

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
