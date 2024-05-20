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
package dev.blocky.twitch.utils.serialization;

import com.google.gson.annotations.SerializedName;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivateCommand
{
    @SerializedName("command")
    String command;

    @SerializedName("aliases")
    String aliases;

    @SerializedName("class")
    String clazz;

    @NonNull
    public String getCommand()
    {
        return command;
    }

    @Nullable
    public Set<String> getAliases()
    {
        if (aliases == null)
        {
            return null;
        }

        String[] aliasParts = aliases.split(",");

        return Arrays.stream(aliasParts).collect(Collectors.toSet());
    }

    @NonNull
    public Set<String> getCommandAndAliases()
    {
        Set<String> aliases = getAliases();

        if (aliases == null || aliases.isEmpty())
        {
            return Collections.singleton(command);
        }

        Set<String> aliasesWithRoot = new HashSet<>(aliases);
        aliasesWithRoot.add(command);

        return aliasesWithRoot;
    }

    @NonNull
    public IPrivateCommand getCommandAsClass() throws Exception
    {
        Class<?> commandClass = Class.forName(STR."dev.blocky.twitch.commands.\{clazz}PrivateCommand");
        Constructor<?> commandConstructor = commandClass.getDeclaredConstructor();
        return (IPrivateCommand) commandConstructor.newInstance();
    }

    PrivateCommand()
    {
    }
}
