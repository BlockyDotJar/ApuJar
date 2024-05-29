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

import dev.blocky.api.entities.blockyjar.BlockyJarBible;
import dev.blocky.api.entities.blockyjar.BlockyJarBibleEntry;
import dev.blocky.api.request.BlockyJarBibleBody;
import dev.blocky.api.request.BlockyJarUserBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface BlockyJarService
{
    @POST("apujar/admins")
    Call<Void> postAdmin(@Body BlockyJarUserBody body);

    @DELETE("apujar/admins/{adminID}")
    Call<Void> deleteAdmin(@Path("adminID") int adminID);

    @POST("apujar/owners")
    Call<Void> postOwner(@Body BlockyJarUserBody body);

    @DELETE("apujar/owners/{ownerID}")
    Call<Void> deleteOwner(@Path("ownerID") int ownerID);

    @GET("apujar/bible")
    Call<BlockyJarBible> getBible(@Query("random") boolean random, @Query("limit") int limit);

    @GET("apujar/bible/{biblePage}")
    Call<BlockyJarBibleEntry> getBibleEntry(@Path("biblePage") int biblePage);

    @POST("apujar/bible")
    Call<BlockyJarBibleEntry> postBibleEntry(@Body BlockyJarBibleBody body);

    @DELETE("apujar/bible/{biblePage}")
    Call<BlockyJarBibleEntry> deleteBibleEntry(@Path("biblePage") int biblePage);

    @PATCH("apujar/bible/{biblePage}")
    Call<BlockyJarBibleEntry> patchBibleEntry(@Path("biblePage") int biblePage, @Body BlockyJarBibleBody body);

    @PATCH("apujar/internal/user/{userID}")
    Call<Void> patchUser(@Path("userID") int userID, @Body BlockyJarUserBody body);
}
