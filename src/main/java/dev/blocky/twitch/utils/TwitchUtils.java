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
package dev.blocky.twitch.utils;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.*;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import dev.blocky.api.ServiceProvider;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.api.entities.ivr.IVRModVIP;
import dev.blocky.api.entities.ivr.IVRSubage;
import dev.blocky.api.entities.ivr.IVRSubageMeta;
import dev.blocky.api.entities.stats.StreamElementsChatter;
import dev.blocky.api.entities.stats.StreamElementsEmote;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static dev.blocky.twitch.Main.helix;
import static dev.blocky.twitch.manager.CommandManager.ratelimitedChats;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class TwitchUtils
{
    @NonNull
    public static String getUserAsString(@NonNull String[] messageParts, int index)
    {
        String userNameRaw = messageParts[index];
        return RegExUtils.removeAll(userNameRaw, "@").toLowerCase();
    }

    @NonNull
    public static String getUserAsString(@NonNull String[] messageParts, @NonNull EventUser eventUser)
    {
        return messageParts.length == 1 ? eventUser.getName() : getUserAsString(messageParts, 1);
    }

    @Nullable
    public static String getSecondUserAsString(@NonNull String[] messageParts, @NonNull EventUser eventUser)
    {
        return messageParts.length == 3 ? getUserAsString(messageParts, 2) : eventUser.getName();
    }

    @NonNull
    public static String getChannelAsString(@NonNull String[] messageParts, @NonNull EventChannel channel)
    {
        return messageParts.length == 1 ? channel.getName() : getUserAsString(messageParts, 1);
    }

    @Nullable
    public static String getParameterAsString(@NonNull String[] messageParts, @NonNull String regex, int index)
    {
        String message = removeElements(messageParts, index);
        String value = RegExUtils.removeAll(message, regex);

        if (value.isBlank())
        {
            return null;
        }

        return value.strip();
    }

    @Nullable
    public static String getParameterAsString(@NonNull String[] messageParts, @NonNull String regex)
    {
        return getParameterAsString(messageParts, regex, 1);
    }

    @NonNull
    public static String getParameterUserAsString(@NonNull String[] messageParts, @NonNull String regex, @NonNull EventUser eventUser)
    {
        String message = removeElements(messageParts, 1);
        String parameterUser = RegExUtils.removeAll(message, regex);

        if (parameterUser.contains(" "))
        {
            String[] parameterUserParts = parameterUser.split(" ");
            parameterUser = parameterUserParts[0];
        }

        if (parameterUser.startsWith("@"))
        {
            parameterUser = parameterUser.substring(1);
        }

        if (parameterUser.isBlank())
        {
            return eventUser.getName();
        }
        return parameterUser;
    }

    @Nullable
    public static String getParameterValue(@NonNull String[] messageParts, @NonNull String regex)
    {
        List<String> parameterValues = Arrays.stream(messageParts).filter(part ->
        {
            Pattern PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher MATCHER = PATTERN.matcher(part);
            return MATCHER.find();
        }).toList();

        if (parameterValues.isEmpty())
        {
            return null;
        }

        String parameterValueRaw = parameterValues.getFirst();
        int equalSign = parameterValueRaw.indexOf('=');

        return parameterValueRaw.substring(equalSign + 1);
    }

    public static boolean hasParameter(@NonNull String[] messageParts, @NonNull String parameter)
    {
        return Arrays.stream(messageParts).anyMatch(parameter::equalsIgnoreCase);
    }

    public static boolean hasRegExParameter(@NonNull String[] messageParts, @NonNull String parameters)
    {
        return Arrays.stream(messageParts).anyMatch(messagePart -> messagePart.matches(parameters));
    }

    @NonNull
    public static List<User> retrieveUserList(@NonNull TwitchClient client, @NonNull String userName)
    {
        UserList userList = client.getHelix().getUsers
                        (
                                null,
                                null,
                                Collections.singletonList(userName)
                        )
                .execute();
        return userList.getUsers();
    }

    @NonNull
    public static List<User> retrieveUserListByID(@NonNull TwitchClient client, int userIID)
    {
        String userID = String.valueOf(userIID);

        UserList userList = client.getHelix().getUsers
                        (
                                null,
                                Collections.singletonList(userID),
                                null
                        )
                .execute();
        return userList.getUsers();
    }

    @NonNull
    public static boolean isValidUsername(@NonNull String userName)
    {
        Pattern pattern = Pattern.compile("(?!_)\\w+", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userName);
        return matcher.matches();
    }

    @NonNull
    public static String[] getFilteredParts(@NonNull String[] messageParts)
    {
        return Arrays.stream(messageParts)
                .filter(messagePart -> !messagePart.isBlank())
                .map(String::strip)
                .toArray(String[]::new);
    }

    @NonNull
    public static String[] removeElementsAsArray(@NonNull String[] messageParts, int start)
    {
        return Arrays.copyOfRange(messageParts, start, messageParts.length);
    }

    @NonNull
    public static String removeElements(@NonNull String[] messageParts, int start)
    {
        messageParts = Arrays.copyOfRange(messageParts, start, messageParts.length);
        return String.join(" ", messageParts);
    }

    @NonNull
    public static String getActualChannelID(@Nullable String fallbackChannelID, @NonNull String channelID)
    {
        return fallbackChannelID == null ? channelID : fallbackChannelID;
    }

    public static boolean hasModeratorPerms(@NonNull IVR ivr, @NonNull String userName)
    {
        for (IVRModVIP ivrModVIP : ivr.getMods())
        {
            String userLogin = ivrModVIP.getUserLogin();

            if (userLogin.equalsIgnoreCase(userName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean hasVIPPerms(@NonNull IVR ivr, @NonNull String userName)
    {
        for (IVRModVIP ivrModVIP : ivr.getVIPs())
        {
            String userLogin = ivrModVIP.getUserLogin();

            if (userLogin.equalsIgnoreCase(userName))
            {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static String handleIllegalCharacters(@NonNull String prefix)
    {
        return RegExUtils.replaceAll(prefix, "'", "''");
    }

    public static void sendChatMessage(@NonNull String channelID, @NonNull String message)
    {
        int channelIID = Integer.parseInt(channelID);
        sendChatMessage(channelIID, message);
    }

    public static void sendChatMessage(int channelIID, @NonNull String message)
    {
        String channelID = String.valueOf(channelIID);

        try
        {
            ChatMessage chatMessage = new ChatMessage(channelID, "896181679", message, null);

            SentChatMessageWrapper wrapper = helix.sendChatMessage(null, chatMessage).execute();
            SentChatMessage sentChatMessage = wrapper.get();

            ChatDropReason chatDropReason = sentChatMessage.getDropReason();

            if (!sentChatMessage.isSent())
            {
                return;
            }

            if (chatDropReason != null)
            {
                String dropReason = chatDropReason.getMessage();
                String dropCode = chatDropReason.getCode();

                if (dropCode.equals("msg_duplicate"))
                {
                    long currentTimeMillis = System.currentTimeMillis();
                    ratelimitedChats.put(channelIID, currentTimeMillis);
                    return;
                }

                String messageToSend = STR."Channel message dropped unexpectedly FeelsGoodMan '\{dropCode}' - \{dropReason}";
                ChatMessage failedChatMessage = new ChatMessage("896181679", "896181679", messageToSend, null);

                helix.sendChatMessage(null, failedChatMessage).execute();
            }
        }
        catch (HystrixRuntimeException | ContextedRuntimeException _)
        {
            long currentTimeMillis = System.currentTimeMillis();

            ratelimitedChats.put(channelIID, currentTimeMillis);
        }
    }

    public static void sendWhisper(@NonNull String userID, @NonNull String message)
    {
        helix.sendWhisper(null, "896181679", userID, message).execute();
    }

    public static boolean checkChatSettings(@NonNull String[] messageParts, @NonNull String userLogin, @NonNull String userID, @NonNull String channelID) throws IOException
    {
        ChatSettingsWrapper chatSettingsWrapper = helix.getChatSettings(null, userID, null).execute();
        ChatSettings chatSettings = chatSettingsWrapper.getChatSettings();

        IVR ivr = ServiceProvider.getIVRModVip(userLogin);
        boolean hasModeratorPerms = TwitchUtils.hasModeratorPerms(ivr, "ApuJar");
        boolean hasVIPPerms = TwitchUtils.hasVIPPerms(ivr, "ApuJar");

        if (hasModeratorPerms || hasVIPPerms || channelID.equals("896181679"))
        {
            return true;
        }

        if (chatSettings.isEmoteOnlyMode())
        {
            EmoteList userEmotes = helix.getUserEmotes(null, "896181679", userID, null).execute();

            List<Emote> emotes = userEmotes.getEmotes();
            List<String> emoteNames = emotes.stream()
                    .map(Emote::getName)
                    .toList();

            messageParts = removeElementsAsArray(messageParts, 2);

            boolean validMessage = Arrays.stream(messageParts).allMatch(emoteNames::contains);

            if (!validMessage)
            {
                sendChatMessage(channelID, STR."mhm Emote only mode detected in channel \{userLogin}, your message doesn't only contain global twitch emotes.");
                return false;
            }
        }

        if (chatSettings.isFollowersOnlyMode())
        {
            OutboundFollowing outboundFollowing = helix.getFollowedChannels(null, "896181679", userID, 100, null).execute();
            List<OutboundFollow> follows = outboundFollowing.getFollows();

            if (follows.isEmpty())
            {
                sendChatMessage(channelID, STR."mhm Followers only mode detected in channel \{userLogin}, no follow detected. Please message @BlockyDotJar to follow the user.");
                return false;
            }
        }

        if (chatSettings.isSubscribersOnlyMode())
        {
            IVRSubage ivrSubage = ServiceProvider.getIVRSubage("ApuJar", userLogin);
            IVRSubageMeta ivrSubageMeta = ivrSubage.getSubageMeta();

            if (ivrSubageMeta == null)
            {
                sendChatMessage(channelID, STR."mhm Subscriber only mode detected in channel \{userLogin}, no sub detected, not able to deliver the message.");
                return false;
            }
        }

        if (chatSettings.isSlowMode())
        {
            sendChatMessage(channelID, STR."mhm Slow mode detected in channel \{userLogin}, message maybe not sendable.");
        }

        return true;
    }

    @Nullable
    public static NamedUserChatColor getChatColor(@NonNull String chatColorName)
    {
        NamedUserChatColor[] namedUserChatColorsRaw = NamedUserChatColor.values();

        Optional<NamedUserChatColor> namedUserChatColors = Arrays.stream(namedUserChatColorsRaw)
                .filter(nucc ->
                {
                    String nuccNameRaw = nucc.name();
                    String nuccName = nuccNameRaw.replace("_", " ");

                    return chatColorName.equalsIgnoreCase(nuccName);
                })
                .findFirst();

        return namedUserChatColors.orElse(null);
    }

    @Nullable
    public static String getEmotesFormatted(@NonNull List<StreamElementsEmote> emotes, int maxRange)
    {
        if (emotes.isEmpty())
        {
            return null;
        }

        int emoteCount = emotes.size();
        int range = Math.min(emoteCount, maxRange);

        List<String> topTwitchEmotes = IntStream.range(0, range).mapToObj(i ->
        {
            int emoteNumber = i + 1;

            StreamElementsEmote emote = emotes.get(i);
            String emoteName = emote.getEmoteName();
            int amountSent  = emote.getAmountSent();

            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            String formattedAmountSent = decimalFormat.format(amountSent);

            return STR."\{emoteNumber}. \{emoteName} - \{formattedAmountSent}";
        }).toList();

        return String.join(" | ", topTwitchEmotes);
    }

    @Nullable
    public static String getChatterFormatted(@NonNull List<StreamElementsChatter> chatters)
    {
        if (chatters.isEmpty())
        {
            return null;
        }

        int chatterCount = chatters.size();
        int range = Math.min(chatterCount, 5);

        List<String> topTwitchChatter = IntStream.range(0, range).mapToObj(i ->
        {
            int chatterNumber = i + 1;

            StreamElementsChatter chatter = chatters.get(i);
            String userLogin = chatter.getUserLogin();
            int messageCount  = chatter.getMessageCount();

            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            String formattedMessageCount = decimalFormat.format(messageCount);

            return STR."\{chatterNumber}. \{userLogin} - \{formattedMessageCount}";
        }).toList();

        return String.join(" | ", topTwitchChatter);
    }
}
