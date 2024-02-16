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
package dev.blocky.api;

import dev.blocky.api.entities.IVRFI;
import dev.blocky.api.entities.ModScanner;
import dev.blocky.api.interceptor.ErrorResponseInterceptor;
import dev.blocky.api.services.IVRFIService;
import dev.blocky.api.services.ModScannerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceProvider
{
    @NonNull
    public static <T> T createService(@NonNull Class<T> clazz, @NonNull String api, Interceptor interceptor)
    {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);

        ConnectionPool connectionPool = new ConnectionPool(5, 5, SECONDS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .dispatcher(dispatcher);

        if (interceptor != null)
        {
            builder.addInterceptor(interceptor);
        }

        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .baseUrl(api)
                .build();

        return retrofit.create(clazz);
    }

    @NonNull
    public static ModScanner createModScannerUser(@NonNull String userName) throws IOException
    {
        ErrorResponseInterceptor errorInterceptor = new ErrorResponseInterceptor();

        ModScannerService msService = ServiceProvider.createService(ModScannerService.class, "https://api.modscanner.com/twitch/", errorInterceptor);
        Call<ModScanner> msCall = msService.getUser(userName);
        Response<ModScanner> response = msCall.execute();
        return response.body();
    }

    @NonNull
    public static ModScanner createModScannerChannel(@NonNull String channelName) throws IOException
    {
        ErrorResponseInterceptor errorInterceptor = new ErrorResponseInterceptor();

        ModScannerService msService = ServiceProvider.createService(ModScannerService.class, "https://api.modscanner.com/twitch/", errorInterceptor);
        Call<ModScanner> msCall = msService.getChannel(channelName);
        Response<ModScanner> response = msCall.execute();
        return response.body();
    }

    @NonNull
    public static List<IVRFI> createIVRFIUser(@NonNull String userName) throws IOException
    {
        IVRFIService ivrfiService = ServiceProvider.createService(IVRFIService.class, "https://api.ivr.fi/v2/", null);
        Call<List<IVRFI>> ivrfiCall = ivrfiService.getUser(userName);
        Response<List<IVRFI>> response = ivrfiCall.execute();
        return response.body();
    }

    @NonNull
    public static IVRFI createIVRFISubAge(@NonNull String userName, @NonNull String channelName) throws IOException
    {
        IVRFIService ivrfiService = ServiceProvider.createService(IVRFIService.class, "https://api.ivr.fi/v2/", null);
        Call<IVRFI> ivrfiCall = ivrfiService.getSubage(userName, channelName);
        Response<IVRFI> response = ivrfiCall.execute();
        return response.body();
    }

    @NonNull
    public static IVRFI createIVRFIModVip(@NonNull String channelName) throws IOException
    {
        IVRFIService ivrfiService = ServiceProvider.createService(IVRFIService.class, "https://api.ivr.fi/v2/", null);
        Call<IVRFI> ivrfiCall = ivrfiService.getModVip(channelName);
        Response<IVRFI> response = ivrfiCall.execute();
        return response.body();
    }

    @NonNull
    public static IVRFI createIVRFIFounders(@NonNull String login) throws IOException
    {
        IVRFIService ivrfiService = ServiceProvider.createService(IVRFIService.class, "https://api.ivr.fi/v2/", null);
        Call<IVRFI> ivrfiCall = ivrfiService.getFounders(login);
        Response<IVRFI> response = ivrfiCall.execute();
        return response.body();
    }
}
