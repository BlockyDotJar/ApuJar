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

import dev.blocky.api.entities.lilb.LiLBChatter;
import dev.blocky.api.entities.seventv.SevenTVSubage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LiLBService
{
    @GET("blocky/{login}")
    Call<LiLBChatter> getChatter(@Path("login") String login);

    @GET("7tvsa/{login}")
    Call<SevenTVSubage> getSevenTVSubage(@Path("login") String login);
}
