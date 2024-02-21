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

import edu.umd.cs.findbugs.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor
{
    private final String accessToken;

    public AuthInterceptor(@NonNull String accessToken)
    {
        this.accessToken = accessToken;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException
    {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", STR."Bearer \{accessToken}")
                .build();

        return chain.proceed(request);
    }
}
