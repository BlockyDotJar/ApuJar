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
package dev.blocky.twitch.commands.rolelookup;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.helix.domain.User;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.blockyjar.KokbinPaste;
import dev.blocky.api.entities.tools.ToolsFounder;
import dev.blocky.api.entities.tools.ToolsModVIP;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class RoleLookupCommand implements ICommand
{
    @Override
    public void onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToLookup = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToLookup))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return;
        }

        List<User> usersToLookup = retrieveUserList(client, userToLookup);

        if (usersToLookup.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToLookup}' found.");
            return;
        }

        User user = usersToLookup.getFirst();
        String userDisplayName = user.getDisplayName();
        String userLogin = user.getLogin();

        int modCount = 0;
        int vipCount = 0;
        int founderCount = 0;

        List<ToolsModVIP> toolsMods = ServiceProvider.getToolsMods(userLogin);
        List<String> moderators = new ArrayList<>();

        if (toolsMods != null)
        {
            moderators = toolsMods.stream().map(ToolsModVIP::getUserLogin).toList();
            modCount = toolsMods.size();
        }

        List<ToolsModVIP> toolsVIPs = ServiceProvider.getToolsVIPs(userLogin);
        List<String> vips = new ArrayList<>();

        if (toolsVIPs != null)
        {
            vips = toolsVIPs.stream().map(ToolsModVIP::getUserLogin).toList();
            vipCount = toolsVIPs.size();
        }

        List<ToolsFounder> toolsFounders = ServiceProvider.getToolsFounders(userLogin);
        List<String> founders = new ArrayList<>();

        if (toolsFounders != null)
        {
            founders = toolsFounders.stream().map(ToolsFounder::getUserLogin).toList();
            founderCount = toolsFounders.size();
        }

        String moderatorsFormatted = String.join("\n", moderators);
        String vipsFormatted = String.join("\n", vips);
        String foundersFormatted = String.join("\n", founders);

        String realModerators = moderatorsFormatted.isBlank() ? "- / -" : moderatorsFormatted;
        String realVIPs = vipsFormatted.isBlank() ? "- / -" : vipsFormatted;
        String realFounders = foundersFormatted.isBlank() ? "- / -" : foundersFormatted;

        String roles = STR."""
                Moderator:

                \{realModerators}

                VIP:

                \{realVIPs}

                Founder:

                \{realFounders}
                """;

        KokbinPaste kokbinPaste = ServiceProvider.paste(roles);
        String pasteKey = kokbinPaste.getKey();

        String messageToSend = STR."PogChamp \{userDisplayName} has \{modCount} moderators, \{vipCount} vips and \{founderCount} founder in its channel! o_O https://paste.blockyjar.dev/\{pasteKey}";

        channelID = getActualChannelID(channelToSend, channelID);

        sendChatMessage(channelID, messageToSend);
    }
}
