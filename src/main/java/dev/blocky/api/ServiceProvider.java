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
import dev.blocky.api.entities.lilb.LiLBChatter;
import dev.blocky.api.entities.maps.GeoCountryCode;
import dev.blocky.api.entities.maps.MapSearch;
import dev.blocky.api.entities.modscanner.ModScanner;
import dev.blocky.api.entities.openmeteo.OpenMeteo;
import dev.blocky.api.entities.seventv.SevenTV;
import dev.blocky.api.entities.seventv.SevenTVEmote;
import dev.blocky.api.entities.seventv.SevenTVEmoteSet;
import dev.blocky.api.entities.seventv.SevenTVTwitchUser;
import dev.blocky.api.entities.wordle.Wordle;
import dev.blocky.api.entities.yt.NoEmbed;
import dev.blocky.api.entities.yt.YouTubeDislikes;
import dev.blocky.api.interceptor.*;
import dev.blocky.api.request.BlockyJarBibleBody;
import dev.blocky.api.request.BlockyJarUserBody;
import dev.blocky.api.request.SevenTVGQLBody;
import dev.blocky.api.request.TwitchGQLBody;
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

import static dev.blocky.twitch.Main.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceProvider
{
    private static final AuthInterceptor sevenTVAuthInterceptor = new AuthInterceptor(STR."Bearer \{sevenTVAccessToken}", null, null, null);
    private static final AuthInterceptor blockyjarAuthInterceptor = new AuthInterceptor(STR."Bearer \{accessToken}", clientID, null, null);
    private static final AuthInterceptor twitchGQLAuthInterceptor = new AuthInterceptor(STR."OAuth \{oAuthToken}", "kimne78kx3ncx6brgo4mv6wki5h1ko", clientIntegrity, deviceID);

    private static final SevenTVGQLErrorInterceptor sevenTVGQLErrorInterceptor = new SevenTVGQLErrorInterceptor();

    private static final ModScannerErrorInterceptor modScannerErrorInterceptor = new ModScannerErrorInterceptor();
    private static final GeonameErrorInterceptor geonameErrorInterceptor = new GeonameErrorInterceptor();
    private static final GitHubErrorInterceptor gitHubErrorInterceptor = new GitHubErrorInterceptor();

    private static final BlockyJarErrorInterceptor blockyJarErrorInterceptor = new BlockyJarErrorInterceptor();

    private static final int IVR_API_VERSION = 2;
    private static final int SEVENTV_API_VERSION = 3;
    private static final int GEOAPIFY_API_VERSION = 1;
    private static final int OPEN_METEO_API_VERSION = 1;
    private static final int WORDLE_API_VERSION = 2;
    private static final int BLOCKYJAR_API_VERSION = 1;

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

        OkHttpClient client = builder.cache(null).build();

        String baseURL = switch (clazz.getSimpleName())
        {
            case "ModScannerService" -> "https://api.modscanner.com/twitch/";
            case "IVRService" -> STR."https://api.ivr.fi/v\{IVR_API_VERSION}/";
            case "SevenTVService" -> STR."https://7tv.io/v\{SEVENTV_API_VERSION}/";
            case "GitHubService" -> "https://api.github.com/";
            case "GeocodeService" -> STR."https://api.geoapify.com/v\{GEOAPIFY_API_VERSION}/geocode/";
            case "GeonameService" -> "http://api.geonames.org/";
            case "OpenMeteoService" -> STR."https://api.open-meteo.com/v\{OPEN_METEO_API_VERSION}/";
            case "WordleService" -> STR."https://www.nytimes.com/svc/wordle/v\{WORDLE_API_VERSION}/";
            case "LiLBService" -> "https://api.blxryer.de/";
            case "BlockyJarService" -> STR."https://api.blockyjar.dev/v\{BLOCKYJAR_API_VERSION}/";
            case "NoEmbedService" -> "https://noembed.com/";
            case "YouTubeDislikesService" -> "https://returnyoutubedislikeapi.com/";
            case "TwitchGQLService" -> "https://gql.twitch.tv/";
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
    public static SevenTVEmoteSet getSevenTVEmoteSet(@NonNull int channelID, @NonNull String emoteSetID) throws IOException
    {
        SevenTVErrorInterceptor sevenTVErrorInterceptor = new SevenTVErrorInterceptor(channelID);

        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class, sevenTVErrorInterceptor);
        Call<SevenTVEmoteSet> sevenTVCall = sevenTVService.getEmoteSet(emoteSetID);
        Response<SevenTVEmoteSet> response = sevenTVCall.execute();
        return response.body();
    }

    @Nullable
    public static SevenTVEmote getSevenTVEmote(@NonNull int channelID, @NonNull String emoteID) throws IOException
    {
        SevenTVErrorInterceptor sevenTVErrorInterceptor = new SevenTVErrorInterceptor(channelID);

        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class, sevenTVErrorInterceptor);
        Call<SevenTVEmote> sevenTVCall = sevenTVService.getEmote(emoteID);
        Response<SevenTVEmote> response = sevenTVCall.execute();
        return response.body();
    }

    @Nullable
    public static SevenTVTwitchUser getSevenTVUser(@NonNull int channelID, int userID) throws IOException
    {
        SevenTVErrorInterceptor sevenTVErrorInterceptor = new SevenTVErrorInterceptor(channelID);

        SevenTVService sevenTVService = ServiceProvider.createService(SevenTVService.class, sevenTVErrorInterceptor);
        Call<SevenTVTwitchUser> sevenTVCall = sevenTVService.getTwitchUser(userID);
        Response<SevenTVTwitchUser> response = sevenTVCall.execute();
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
    public static MapSearch getSearchedMaps(@NonNull String text) throws IOException
    {
        Dotenv env = Dotenv.configure()
                .filename(".maps")
                .load();

        String apiKey = env.get("GEOAPIFY_API_KEY");

        GeocodeService geocodeService = ServiceProvider.createService(GeocodeService.class);
        Call<MapSearch> mapSearchCall = geocodeService.search(text, apiKey);
        Response<MapSearch> response = mapSearchCall.execute();
        return response.body();
    }

    @NonNull
    public static GeoCountryCode getCountryCode(double lat, double lng) throws IOException
    {
        GeonamesService geocodeService = ServiceProvider.createService(GeonamesService.class, geonameErrorInterceptor);
        Call<GeoCountryCode> mapSearchCall = geocodeService.getCountryCode(lat, lng, "BlockyJar");
        Response<GeoCountryCode> response = mapSearchCall.execute();
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

    @NonNull
    public static Wordle getWordle(@NonNull String year, @NonNull String month, @NonNull String day) throws IOException
    {
        WordleService wordleService = ServiceProvider.createService(WordleService.class);
        Call<Wordle> wordleCall = wordleService.getWordle(year, month, day);
        Response<Wordle> response = wordleCall.execute();
        return response.body();
    }

    @NonNull
    public static LiLBChatter getChatter(@NonNull String login) throws IOException
    {
        LiLBService lilbService = ServiceProvider.createService(LiLBService.class);
        Call<LiLBChatter> lilbCall = lilbService.getChatter(login);
        Response<LiLBChatter> response = lilbCall.execute();
        return response.body();
    }

    public static void postTwitchGQL(@NonNull TwitchGQLBody body) throws IOException
    {
        TwitchGQLService twitchGQLService = ServiceProvider.createService(TwitchGQLService.class, twitchGQLAuthInterceptor);
        Call<Void> twitchGQLCall = twitchGQLService.postGQL(body);
        twitchGQLCall.execute();
    }

    public static void postAdmin(@NonNull BlockyJarUserBody body) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.postAdmin(body);
        blockyJarCall.execute();
    }

    public static void deleteAdmin(int adminID) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.deleteAdmin(adminID);
        blockyJarCall.execute();
    }

    public static void postOwner(@NonNull BlockyJarUserBody body) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.postOwner(body);
        blockyJarCall.execute();
    }

    public static void deleteOwner(int adminID) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.deleteOwner(adminID);
        blockyJarCall.execute();
    }

    public static void postBibleEntry(@NonNull BlockyJarBibleBody body) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.postBibleEntry(body);
        blockyJarCall.execute();
    }

    public static void deleteBibleEntry(int biblePage) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.deleteBibleEntry(biblePage);
        blockyJarCall.execute();
    }

    public static void patchBibleEntry(int biblePage, @NonNull BlockyJarBibleBody body) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.patchBibleEntry(biblePage, body);
        blockyJarCall.execute();
    }

    public static void patchUser(int userID, @NonNull BlockyJarUserBody body) throws IOException
    {
        BlockyJarService blockyJarService = ServiceProvider.createService(BlockyJarService.class, blockyJarErrorInterceptor, blockyjarAuthInterceptor);
        Call<Void> blockyJarCall = blockyJarService.patchUser(userID, body);
        blockyJarCall.execute();
    }

    @Nullable
    public static NoEmbed getYouTubeEmbed(@NonNull String url) throws IOException
    {
        NoEmbedService noEmbedService = ServiceProvider.createService(NoEmbedService.class);
        Call<NoEmbed> noEmbedCall = noEmbedService.getEmbed(url);
        Response<NoEmbed> response = noEmbedCall.execute();
        return response.body();
    }

    @Nullable
    public static YouTubeDislikes getYouTubeVideoVotes(@NonNull String videoID) throws IOException
    {
        YouTubeDislikesService youTubeDislikesService = ServiceProvider.createService(YouTubeDislikesService.class);
        Call<YouTubeDislikes> youTubeDislikesCall = youTubeDislikesService.getVotes(videoID);
        Response<YouTubeDislikes> response = youTubeDislikesCall.execute();
        return response.body();
    }
}
