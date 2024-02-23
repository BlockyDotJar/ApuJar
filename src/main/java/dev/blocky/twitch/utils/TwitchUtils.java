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
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.api.entities.ivr.IVRModVIP;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.RegExUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class TwitchUtils
{
    @NonNull
    public static String getUserAsString(@NonNull String[] msgParts, int index)
    {
        String userNameRaw = msgParts[index].strip();
        return RegExUtils.removeAll(userNameRaw, "@").toLowerCase();
    }

    @NonNull
    public static String getUserAsString(@NonNull String[] msgParts, @NonNull EventUser eventUser)
    {
        return msgParts.length == 1 ? eventUser.getName() : getUserAsString(msgParts, 1);
    }

    @Nullable
    public static String getSecondUserAsString(@NonNull String[] msgParts, @NonNull EventUser eventUser)
    {
        return msgParts.length == 3 ? getUserAsString(msgParts, 2) : eventUser.getName();
    }

    @NonNull
    public static String getParameterUserAsString(@NonNull String[] msgParts, @NonNull EventUser eventUser)
    {
        String message = removeElements(msgParts, 1);
        String parameterUser = RegExUtils.removeAll(message, "-ch(annel)?").strip();

        if (parameterUser.contains(" "))
        {
            String[] parameterUserParts = parameterUser.split(" ");
            parameterUser = parameterUserParts[0];
        }

        if (parameterUser.isBlank())
        {
            return eventUser.getName();
        }
        return parameterUser;
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
    public static String removeElements(@NonNull String[] msgParts, int start)
    {
        msgParts = Arrays.copyOfRange(msgParts, start, msgParts.length);
        return String.join(" ", msgParts);
    }

    @NonNull
    public static String getActualChannel(@Nullable String sendChannel, @NonNull String channelName)
    {
        return sendChannel == null ? channelName : sendChannel;
    }

    public static boolean hasModeratorPerms(@NonNull IVR ivr, @NonNull String userName)
    {
        for (IVRModVIP ivrModVIP : ivr.getMods())
        {
            String login = ivrModVIP.getLogin();

            if (login.equalsIgnoreCase(userName))
            {
                return true;
            }
        }
        return false;
    }
}
