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

import static dev.blocky.twitch.commands.admin.UserSayCommand.channelToSend;
import static dev.blocky.twitch.utils.TwitchUtils.*;

public class ChatterinoCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelChatMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        String channelID = event.getBroadcasterUserId();

        boolean isWindowsParameter = hasRegExParameter(messageParts, "-exe");
        boolean isMacOsParameter = hasRegExParameter(messageParts, "-dmg(=(arm64|x86))?");
        boolean isLinuxParameter = hasRegExParameter(messageParts, "-deb(=(arm64|x86))?");

        String owner = "SevenTV";
        String repository = "chatterino7";

        if (messageParts.length >= 2)
        {
            String chatterinoType = getParameterAsString(messageParts, "-(exe|dmg|deb)(=(arm64|x86))?");

            if (chatterinoType != null)
            {
                switch (chatterinoType)
                {
                    case "chatterinohomies", "homies" ->
                    {
                        owner = "itzAlex";
                        repository = "chatterino7";
                    }
                    case "chatterino2", "normal", "default" ->
                    {
                        owner = "Chatterino";
                        repository = "chatterino2";
                    }
                }
            }
        }

        GitHubRelease gitHubRelease = ServiceProvider.getGitHubLatestRelease(owner, repository);
        String htmlURL = gitHubRelease.getHtmlURL();
        String tagName = gitHubRelease.getTagName();
        Date publishedAt = gitHubRelease.getPublishedAt();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String readablePublishedAt = formatter.format(publishedAt);

        List<GitHubAsset> assets = gitHubRelease.getAssets();
        GitHubAsset asset = null;

        if (assets.isEmpty() || (!isWindowsParameter && !isMacOsParameter && !isLinuxParameter))
        {
            sendChatMessage(channelID, STR."SeemsGood The latest version of \{repository} is \{tagName} and was released on \{readablePublishedAt} \uD83D\uDC49 \{htmlURL}");
            return;
        }

        String fileRaw = getParameterValue(messageParts, "-(exe|dmg|deb)(=(arm64|x86))?");
        String neededFileType = null;
        String neededArchitecture = null;

        if (isWindowsParameter)
        {
            neededFileType = "exe";
        }

        if (isMacOsParameter)
        {
            neededFileType = "dmg";

            if (fileRaw != null && (fileRaw.equals("x86") || fileRaw.equals("arm64")))
            {
                neededArchitecture = fileRaw;
            }
        }

        if (isLinuxParameter)
        {
            neededFileType = "deb";

            if (fileRaw != null && fileRaw.equals("x86"))
            {
                neededArchitecture = fileRaw;
            }
        }

        for (GitHubAsset gitHubAsset : assets)
        {
            String gitHubAssetName = gitHubAsset.getAssetName();

            if (!gitHubAssetName.endsWith(STR.".\{neededFileType}"))
            {
                continue;
            }

            if (neededArchitecture != null && !gitHubAssetName.contains(neededArchitecture))
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

        sendChatMessage(channelID, STR."SeemsGood The latest version of \{repository} is \{tagName} and was released on \{readablePublishedAt} (Asset-ID: \{assetID}) \uD83D\uDC49 \{browserDownloadURL}");
    }
}
