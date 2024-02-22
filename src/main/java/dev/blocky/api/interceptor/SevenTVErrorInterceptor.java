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
package dev.blocky.api.interceptor;

import dev.blocky.api.exceptions.BadRequest;
import dev.blocky.api.exceptions.HTTPException;
import dev.blocky.api.exceptions.TwitchServerException;
import dev.blocky.api.exceptions.Unauthorized;
import edu.umd.cs.findbugs.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class SevenTVErrorInterceptor implements Interceptor
{
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException
    {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String body = response.peekBody(Long.MAX_VALUE).string();
        JSONObject json = new JSONObject(body);

        if (!response.isSuccessful())
        {
            JSONArray errors = json.getJSONArray("errors");
            JSONObject error = errors.getJSONObject(0);
            String message = error.getString("message");

            switch (response.code())
            {
                case 400 -> throw new BadRequest(message);
                case 401 -> throw new Unauthorized(message);
                case 500 -> throw new TwitchServerException(STR."Internal Server Error: \{message}");
                default -> throw new HTTPException(message);
            }
        }
        return response;
    }
}
