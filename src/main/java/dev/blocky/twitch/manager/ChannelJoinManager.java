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
package dev.blocky.twitch.manager;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelNoticeEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import dev.blocky.twitch.serialization.Chat;
import dev.blocky.twitch.utils.SQLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;

public class ChannelJoinManager
{
    private final IEventSubSocket eventSocket;
    private final TwitchChat twitchChat;

    public ChannelJoinManager(@NonNull IEventSubSocket eventSocket, @NonNull EventManager eventManager, @NonNull TwitchChat chat)
    {
        this.eventSocket = eventSocket;
        this.twitchChat = chat;

        SimpleEventHandler eventHandler = eventManager.getEventHandler(SimpleEventHandler.class);
        eventHandler.onEvent(ChannelNoticeEvent.class, this::onChannelNotice);
        eventHandler.onEvent(IRCMessageEvent.class, this::onIRCMessage);
    }

    public void onChannelNotice(@NonNull ChannelNoticeEvent event)
    {
        EventChannel channel = event.getChannel();
        String channelName = channel.getName();
        String channelID = channel.getId();
        int channelIID = Integer.parseInt(channelID);

        try
        {
            String message = event.getMessage();

            Chat chat = SQLUtils.getChat(channelID);

            if (message.contains("banned") && chat != null)
            {
                Set<Chat> chats = SQLUtils.getChats();
                int chatCount = chats.size() - 1;

                twitchChat.leaveChannel(channelName);
                partChannel(channelIID);

                SQLite.onUpdate(STR."DELETE FROM chats WHERE userLogin = '\{channelName}'");
                sendChatMessage("896181679", STR."peepoLeave Left channel '\{channelName}' because i am now BANNED in it FicktEuchAlle Now active in \{chatCount} chats");
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            sendChatMessage("896181679", STR."Channel: \{channelName} Weird Error while trying to listen to channel notices FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }

    public void onIRCMessage(@NonNull IRCMessageEvent event)
    {
        try
        {
            String commandType = event.getCommandType();

            if (!commandType.equals("CLEARCHAT"))
            {
                return;
            }

            String targetUserID = event.getTargetUserId();

            if (targetUserID == null)
            {
                return;
            }

            int targetUserIID = Integer.parseInt(targetUserID);

            EventChannel channel = event.getChannel();
            String channelName = channel.getName();
            String channelID = channel.getId();
            int channelIID = Integer.parseInt(channelID);

            Chat chat = SQLUtils.getChat(channelID);

            String banDuration = event.getTagValue("ban-duration").orElse(null);

            if (banDuration == null && targetUserIID == 896181679 && chat != null)
            {
                Set<Chat> chats = SQLUtils.getChats();
                int chatCount = chats.size() - 1;

                twitchChat.leaveChannel(channelName);
                partChannel(channelIID);

                SQLite.onUpdate(STR."DELETE FROM chats WHERE userID = \{channelID}");
                sendChatMessage("896181679", STR."peepoLeave Left channel '\{channelName}' because i am now BANNED in it FicktEuchAlle Now active in \{chatCount} chats");
            }
        }
        catch (Exception e)
        {
            String error = e.getMessage();

            Class<?> clazz = e.getClass();
            String clazzName = clazz.getName();

            EventChannel channel = event.getChannel();
            String channelName = channel.getName();

            sendChatMessage("896181679", STR."Channel: \{channelName} Weird Error while trying to listen to channel notices FeelsGoodMan \{error} (\{clazzName})");

            e.printStackTrace();
        }
    }

    public void joinChannel(int userID)
    {
        String chatID = String.valueOf(userID);

        eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription
                (
                        builder -> builder.broadcasterUserId(chatID).userId("896181679").build(),
                        null
                ));
    }

    public void partChannel(int userID)
    {
        String chatID = String.valueOf(userID);

        eventSocket.unregister(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription
                (
                        builder -> builder.broadcasterUserId(chatID).userId("896181679").build(),
                        null
                ));
    }
}
