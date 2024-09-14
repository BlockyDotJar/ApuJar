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
package dev.blocky.twitch.commands.github;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.github.GitHubAsset;
import dev.blocky.api.entities.github.GitHubRelease;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.blocky.twitch.commands.admin.SayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ChattyCommand implements ICommand
{
    @Override
    public boolean onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        boolean hasEXEParameter = hasParameter(messageParts, "-exe");
        boolean hasZIPParameter = hasParameter(messageParts, "-zip");

        GitHubRelease gitHubRelease = ServiceProvider.getGitHubLatestRelease("chatty", "chatty");
        String htmlURL = gitHubRelease.getHtmlURL();
        String tagName = gitHubRelease.getTagName();
        Date publishedAt = gitHubRelease.getPublishedAt();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String readablePublishedAt = formatter.format(publishedAt);

        List<GitHubAsset> assets = gitHubRelease.getAssets();
        GitHubAsset asset = null;

        if (assets.isEmpty() || (!hasEXEParameter && !hasZIPParameter))
        {
            return sendChatMessage(channelID, STR."SeemsGood The latest version of chatty is \{tagName} and was released on \{readablePublishedAt} \uD83D\uDC49 \{htmlURL}");
        }

        String neededFileType = null;

        if (hasEXEParameter)
        {
            neededFileType = "exe";
        }

        if (hasZIPParameter)
        {
            neededFileType = "zip";
        }

        for (GitHubAsset gitHubAsset : assets)
        {
            String gitHubAssetName = gitHubAsset.getAssetName();

            if (!gitHubAssetName.endsWith(STR.".\{neededFileType}"))
            {
                continue;
            }

            asset = gitHubAsset;
            break;
        }

        if (asset == null)
        {
            asset = assets.getFirst();
        }

        int assetID = asset.getAssetID();

        String browserDownloadURL = asset.getBrowserDownloadURL();

        channelID = getActualChannelID(channelToSend, channelID);

        return sendChatMessage(channelID, STR."SeemsGood The latest version of chatty is \{tagName} and was released on \{readablePublishedAt} (Asset-ID: \{assetID}) \uD83D\uDC49 \{browserDownloadURL}");
    }
}
