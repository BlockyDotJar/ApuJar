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
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.github.GitHubAsset;
import dev.blocky.api.entities.github.GitHubRelease;
import dev.blocky.twitch.interfaces.ICommand;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatterinoCommand implements ICommand
{
    @Override
    public void onCommand(@NotNull ChannelMessageEvent event, @NotNull TwitchClient client, @NotNull String[] prefixedMessageParts, @NotNull String[] messageParts) throws Exception
    {
        TwitchChat chat = client.getChat();

        EventChannel channel = event.getChannel();
        String channelName = channel.getName();

        String owner = "SevenTV";
        String repository = "chatterino7";

        if (messageParts.length >= 2)
        {
            String chatterinoType = messageParts[1];

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
                case "dankerino", "dank" ->
                {
                    owner = "Mm2PL";
                    repository = "dankerino";
                }
            }
        }

        GitHubRelease gitHubRelease = ServiceProvider.getGitHubLatestRelease(owner, repository);
        String htmlURL = gitHubRelease.getHtmlURL();
        String tagName = gitHubRelease.getTagName();
        Date publishedAt = gitHubRelease.getPublishedAt();
        boolean isPreRealse = gitHubRelease.isPreRelease();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String readablePublishedAt = formatter.format(publishedAt);

        List<GitHubAsset> assets = gitHubRelease.getAssets();
        GitHubAsset asset = null;

        if (assets.isEmpty())
        {
            chat.sendMessage(channelName, STR."SeemsGood The latest version of \{repository} is \{tagName} and was released on \{readablePublishedAt} (Pre-Release: \{isPreRealse}) \uD83D\uDC49 \{htmlURL}");
            return;
        }

        for (GitHubAsset gitHubAsset : assets)
        {
            String gitHubAssetName = gitHubAsset.getAssetName();

            if (!gitHubAssetName.endsWith(".exe"))
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

        chat.sendMessage(channelName, STR."SeemsGood The latest version of \{repository} is \{tagName} and was released on \{readablePublishedAt} (Asset-ID: \{assetID}, Pre-Release: \{isPreRealse}) \uD83D\uDC49 \{browserDownloadURL}");
    }
}
