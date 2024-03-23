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

import dev.blocky.api.entities.github.GitHubRelease;
import dev.blocky.api.entities.ivr.IVR;
import dev.blocky.api.entities.ivr.IVRSubage;
import dev.blocky.api.entities.ivr.IVRUser;
import dev.blocky.api.entities.maps.MapSearch;
import dev.blocky.api.entities.maps.ReversedMap;
import dev.blocky.api.entities.modscanner.ModScanner;
import dev.blocky.api.entities.openmeteo.OpenMeteo;
import dev.blocky.api.entities.seventv.SevenTV;
import dev.blocky.api.entities.seventv.SevenTVEmote;
import dev.blocky.api.entities.seventv.SevenTVUser;
import dev.blocky.api.gql.SevenTVGQLBody;
import dev.blocky.api.interceptor.*;
import dev.blocky.api.services.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;

import static dev.blocky.twitch.Main.sevenTVAccessToken;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceProvider
{
    private static final AuthInterceptor sevenTVAuthInterceptor = new AuthInterceptor(sevenTVAccessToken);

    private static final SevenTVGQLErrorInterceptor sevenTVGQLErrorInterceptor = new SevenTVGQLErrorInterceptor();

    private static final ModScannerErrorInterceptor modScannerErrorInterceptor = new ModScannerErrorInterceptor();
    private static final GeocodeErrorInterceptor geocodeErrorInterceptor = new GeocodeErrorInterceptor();
    private static final GitHubErrorInterceptor gitHubErrorInterceptor = new GitHubErrorInterceptor();

    private static final int IVR_API_VERSION = 2;
    private static final int SEVENTV_API_VERSION = 3;
    private static final int GEOAPIFY_VERSION = 1;
    private static final int OPEN_METEO_VERSION = 1;

    @NonNull
    public static <T> T createService(@NonNull Class<T> clazz, @Nullable Interceptor... interceptors)
    {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);

        ConnectionPool connectionPool = new ConnectionPool(5, 5, SECONDS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .dispatcher(dispatcher);

        for (Interceptor interceptor : interceptors)
        {
            builder.addInterceptor(interceptor);
        }

        OkHttpClient client = builder.build();

        String baseURL = switch (clazz.getSimpleName())
        {
            case "ModScannerService" -> "https://api.modscanner.com/twitch/";
            case "IVRService" -> STR."https://api.ivr.fi/v\{IVR_API_VERSION}/";
            case "SevenTVService" -> STR."https://7tv.io/v\{SEVENTV_API_VERSION}/";
            case "GitHubService" -> "https://api.github.com/";
            case "GeocodeService" -> "https://geocode.maps.co/";
            case "ReversedGeocodeService" -> STR."https://api.geoapify.com/v\{GEOAPIFY_VERSION}/geocode/";
            case "OpenMeteoService" -> STR."https://api.open-meteo.com/v\{OPEN_METEO_VERSION}/";
            default -> null;
        };

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .client(client)
                .build();

        return retrofit.create(clazz);
    }

    @NonNull
    public static ModScanner getModScannerUser(@NonNull String userName) throws IOException
    {
        ModScannerService msService = ServiceProvider.createService(ModScannerService.class, modScannerErrorInterceptor);
        Call<ModScanner> msCall = msService.getUser(userName);
        Response<ModScanner> response = msCall.execute();
        return response.body();
    }

    @NonNull
    public static ModScanner getModScannerChannel(@NonNull String channelName) throws IOException
    {
        ModScannerService msService = ServiceProvider.createService(ModScannerService.class, modScannerErrorInterceptor);
        Call<ModScanner> msCall = msService.getChannel(channelName);
        Response<ModScanner> response = msCall.execute();
        return response.body();
    }

    @NonNull
    public static List<IVRUser> getIVRUser(@NonNull String userName) throws IOException
    {
        IVRService ivrService = ServiceProvider.createService(IVRService.class);
        Call<List<IVRUser>> ivrCall = ivrService.getUser(userName);
        Response<List<IVRUser>> response = ivrCall.execute();
        return response.body();
    }

    @NonNull
    public static IVRSubage getIVRSubage(@NonNull String userName, @NonNull String channelName) throws IOException
    {
        IVRService ivrService = ServiceProvider.createService(IVRService.class);
        Call<IVRSubage> ivrCall = ivrService.getSubage(userName, channelName);
        Response<IVRSubage> response = ivrCall.execute();
        return response.body();
    }

    @NonNull
    public static IVR getIVRModVip(@NonNull String channelName) throws IOException
    {
        IVRService ivrService = ServiceProvider.createService(IVRService.class);
        Call<IVR> ivrCall = ivrService.getModVip(channelName);
        Response<IVR> response = ivrCall.execute();
        return response.body();
    }

    @Nullable
    public static IVR getIVRFounders(@NonNull String login) throws IOException
    {
        IVRService ivrService = ServiceProvider.createService(IVRService.class);
        Call<IVR> ivrCall = ivrService.getFounders(login);
        Response<IVR> response = ivrCall.execute();
        return response.body();
    }

    @NonNull
    public static SevenTV getSevenTVEmoteSet(@NonNull String emoteSetID) throws IOException
    {
        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class);
        Call<SevenTV> sevenTVCall = sevenTVService.getEmoteSet(emoteSetID);
        Response<SevenTV> response = sevenTVCall.execute();
        return response.body();
    }

    @NonNull
    public static SevenTVEmote getSevenTVEmote(@NonNull String emoteID) throws IOException
    {
        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class);
        Call<SevenTVEmote> sevenTVCall = sevenTVService.getEmote(emoteID);
        Response<SevenTVEmote> response = sevenTVCall.execute();
        return response.body();
    }

    @NonNull
    public static SevenTVUser getSevenTVUser(@NonNull String userID) throws IOException
    {
        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class);
        Call<SevenTVUser> sevenTVCall = sevenTVService.getUser(userID);
        Response<SevenTVUser> response = sevenTVCall.execute();
        return response.body();
    }

    @NonNull
    public static SevenTV postSevenTVGQL(@NonNull SevenTVGQLBody body) throws IOException
    {
        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class, sevenTVGQLErrorInterceptor, sevenTVAuthInterceptor);
        Call<SevenTV> sevenTVCall = sevenTVService.postGQL(body);
        Response<SevenTV> response = sevenTVCall.execute();
        return response.body();
    }

    @NonNull
    public static GitHubRelease getGitHubLatestRelease(@NonNull String owner, @NonNull String repository) throws IOException
    {
        GitHubService gitHubService = ServiceProvider.createService(GitHubService.class, gitHubErrorInterceptor);
        Call<GitHubRelease> gitHubCall = gitHubService.getLatestRelease(owner, repository);
        Response<GitHubRelease> response = gitHubCall.execute();
        return response.body();
    }

    @NonNull
    public static List<MapSearch> getSearchedMaps(@NonNull String query) throws IOException
    {
        Dotenv env = Dotenv.configure()
                .filename(".maps")
                .load();

        String apiKey = env.get("MAPS_API_KEY");

        GeocodeService geocodeService = ServiceProvider.createService(GeocodeService.class);
        Call<List<MapSearch>> mapSearchCall = geocodeService.search(query, apiKey);
        Response<List<MapSearch>> response = mapSearchCall.execute();
        return response.body();
    }

    @NonNull
    public static ReversedMap getReversedMap(double latitude, double longitude) throws IOException
    {
        Dotenv env = Dotenv.configure()
                .filename(".maps")
                .load();

        String apiKey = env.get("GEOAPIFY_API_KEY");

        ReversedGeocodeService geocodeService = ServiceProvider.createService(ReversedGeocodeService.class, geocodeErrorInterceptor);
        Call<ReversedMap> reversedMapCall = geocodeService.reverse(latitude, longitude, apiKey);
        Response<ReversedMap> response = reversedMapCall.execute();
        return response.body();
    }

    @NonNull
    public static OpenMeteo getOpenMeteoCurrentWeather(double latitude, double longitude) throws IOException
    {
        List<String> currentVariables = List.of
                (
                        "temperature_2m", "relative_humidity_2m", "apparent_temperature", "is_day",
                        "rain", "snowfall", "cloud_cover", "wind_speed_10m", "wind_direction_10m"
                );

        String current = String.join(",", currentVariables);

        OpenMeteoService openMeteoService = ServiceProvider.createService(OpenMeteoService.class);
        Call<OpenMeteo> openMeteoCall = openMeteoService.getCurrentWeather(latitude, longitude, current);
        Response<OpenMeteo> response = openMeteoCall.execute();
        return response.body();
    }
}
