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
package dev.blocky.twitch.commands;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.yt.NoEmbed;
import dev.blocky.api.entities.yt.YouTubeDislikes;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.getActualChannelID;
import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class YouTubeCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        String eventUserName = event.getChatterUserName();

        if (messageParts.length == 1)
        {
            sendChatMessage(channelID, "FeelsMan Please specify a YouTube url or video id.");
            return false;
        }

        String videoID = messageParts[1];

        if (!videoID.matches("^(https?://(www.|m.|music.)?youtu(be.com|.be)/(watch[?]v=|shorts/))?[\\w-]{11}((&|[?])[\\w=?&]+)?$"))
        {
            sendChatMessage(channelID, "FeelsMan Invalid YouTube url or video id specified.");
            return false;
        }

        if (videoID.length() != 11)
        {
            int lastSlash = videoID.lastIndexOf('/');
            String videoIDRaw = videoID.substring(lastSlash + 1);

            videoID = videoIDRaw.substring(0, 11);

            if (videoIDRaw.startsWith("watch?v="))
            {
                videoID = videoIDRaw.substring(8, 19);
            }
        }

        String url = STR."https://m.youtube.com/watch?v=\{videoID}";

        NoEmbed noEmbed = ServiceProvider.getYouTubeEmbed(url);
        String videoTitle = noEmbed.getTitle();
        String videoAuthor = noEmbed.getAuthorName();

        if (videoTitle == null && videoAuthor == null)
        {
            sendChatMessage(channelID, STR."FeelsGoodMan No YouTube video found for id '\{videoID}'.");
            return false;
        }

        YouTubeDislikes youTubeDislikes = ServiceProvider.getYouTubeVideoVotes(videoID);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date creationDate = youTubeDislikes.getCreationDate();
        String readableCreationDate = formatter.format(creationDate);

        int likes = youTubeDislikes.getLikes();
        int dislikes = youTubeDislikes.getDislikes();
        long viewCount = youTubeDislikes.getViewCount();
        boolean isDeleted = youTubeDislikes.isDeleted();

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        String formattedLikes = decimalFormat.format(likes);
        String formattedDislikes = decimalFormat.format(dislikes);
        String formattedViewCount = decimalFormat.format(viewCount);

        String videoDeletion = "";

        if (isDeleted)
        {
            videoDeletion = "(\u26D4 DELETED VIDEO \u26D4)";
        }

        String messageToSend = STR."\{eventUserName} \ud83c\udfa5 Stats for \{videoDeletion} '\{videoTitle}' by \{videoAuthor} from \{readableCreationDate} - \ud83d\udc40 \{formattedViewCount} \ud83d\udc4d \{formattedLikes} \ud83d\udc4e \{formattedDislikes}";

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, messageToSend);
    }
}
