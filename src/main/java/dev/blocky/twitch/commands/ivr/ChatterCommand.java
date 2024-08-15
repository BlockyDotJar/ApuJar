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
package dev.blocky.twitch.commands.ivr;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.blockyjar.KokbinPaste;
import dev.blocky.api.entities.ivr.IVRUser;
import dev.blocky.api.entities.lilb.LiLBChatter;
import dev.blocky.twitch.interfaces.ICommand;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.List;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ChatterCommand implements ICommand
{
    @Override
    public boolean onCommand(@NonNull ChannelChatMessageEvent event, @NonNull TwitchClient client, @NonNull String[] prefixedMessageParts, @NonNull String[] messageParts) throws Exception
    {
        String eventUserName = event.getChatterUserName();
        String channelID = event.getBroadcasterUserId();

        String userToCheck = getUserAsString(messageParts, eventUserName);

        if (!isValidUsername(userToCheck))
        {
            sendChatMessage(channelID, "o_O Username doesn't match with RegEx R-)");
            return false;
        }

        List<IVRUser> ivrUsers = ServiceProvider.getIVRUser(userToCheck);

        if (ivrUsers == null || ivrUsers.isEmpty())
        {
            sendChatMessage(channelID, STR.":| No user called '\{userToCheck}' found.");
            return false;
        }

        IVRUser ivrUser = ivrUsers.getFirst();
        String userDisplayName = ivrUser.getUserDisplayName();
        String userLogin = ivrUser.getUserLogin();
        int userChatterCount = ivrUser.getChatterCount();

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        LiLBChatter lilbChatter = ServiceProvider.getChatter(userLogin);

        if (lilbChatter == null)
        {
            sendChatMessage(channelID, "FeelsOkayMan lilb API server error. dink lilb_lxryer");
            return false;
        }

        int moderatorCount = lilbChatter.getModeratorCount();
        int vipCount = lilbChatter.getVIPCount();
        int viewerCount = lilbChatter.getViewerCount();

        int chatters = moderatorCount + vipCount + viewerCount;

        if (chatters != userChatterCount)
        {
            viewerCount += (userChatterCount - chatters);
        }

        String chatterCount = decimalFormat.format(userChatterCount);
        String viewerCountFormatted = decimalFormat.format(viewerCount);

        List<String> moderators = lilbChatter.getModerators();
        List<String> vips = lilbChatter.getVIPs();
        List<String> viewers = lilbChatter.getViewers();

        String moderatorsFormatted = String.join("\n", moderators);
        String vipsFormatted = String.join("\n", vips);
        String viewersFormatted = String.join("\n", viewers);

        String realModerators = moderatorsFormatted.isBlank() ? "- / -" : moderatorsFormatted;
        String realVIPs = vipsFormatted.isBlank() ? "- / -" : vipsFormatted;
        String realViewers = viewersFormatted.isBlank() ? "- / -" : viewersFormatted;

        String chatter = STR."""
                Moderator:

                \{realModerators}
                
                VIP:

                \{realVIPs}
                
                Viewer:

                \{realViewers}
                """;

        KokbinPaste kokbinPaste = ServiceProvider.paste(chatter);
        String pasteKey = kokbinPaste.getKey();

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, STR."Susge There are \{chatterCount} chatter in \{userDisplayName}'s chat. (Moderator: \{moderatorCount}, VIP: \{vipCount}, Viewer: \{viewerCountFormatted}) https://paste.blockyjar.dev/\{pasteKey}");
    }
}
