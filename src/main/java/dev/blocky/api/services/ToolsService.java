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
package dev.blocky.api.services;

import dev.blocky.api.entities.tools.ToolsFounder;
import dev.blocky.api.entities.tools.ToolsModVIP;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ToolsService
{
    @GET("getmods/{channel}")
    Call<List<ToolsModVIP>> getMods(@Path("channel") String channel);

    @GET("getvips/{channel}")
    Call<List<ToolsModVIP>> getVIPs(@Path("channel") String channel);

    @GET("getfounders/{user}")
    Call<List<ToolsFounder>> getFounders(@Path("user") String user);
}
