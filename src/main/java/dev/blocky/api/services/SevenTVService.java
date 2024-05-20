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

import dev.blocky.api.entities.seventv.SevenTV;
import dev.blocky.api.entities.seventv.SevenTVEmote;
import dev.blocky.api.entities.seventv.SevenTVEmoteSet;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.api.request.SevenTVGQLBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SevenTVService
{
    @GET("emote-sets/{emoteSetID}")
    @Headers("Cache-Control: no-cache")
    Call<SevenTVEmoteSet> getEmoteSet(@Path("emoteSetID") String emoteSetID);

    @GET("emotes/{emoteID}")
    @Headers("Cache-Control: no-cache")
    Call<SevenTVEmote> getEmote(@Path("emoteID") String emoteID);

    @GET("users/twitch/{userID}")
    @Headers("Cache-Control: no-cache")
    Call<SevenTVTwitchUser> getTwitchUser(@Path("userID") int userID);

    @POST("gql")
    @Headers({"Content-Type: application/json", "Cache-Control: no-cache"})
    Call<SevenTV> postGQL(@Body SevenTVGQLBody body);
}
