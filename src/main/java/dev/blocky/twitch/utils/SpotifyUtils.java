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
package dev.blocky.twitch.utils;

import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.serialization.SpotifyUser;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.cdimascio.dotenv.Dotenv;
import org.joda.time.LocalDateTime;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

import java.net.URI;

public class SpotifyUtils
{
    @NonNull
    public static SpotifyApi getSpotifyAPI(int userID) throws Exception
    {
        SpotifyUser spotifyUser = SQLUtils.getSpotifyUser(userID);

        String accessToken = spotifyUser.getAccessToken();
        String refreshToken = spotifyUser.getRefreshToken();
        LocalDateTime expiresOnLocalDate = spotifyUser.getExpiresOn();

        SpotifyApi spotifyAPI = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
        
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(expiresOnLocalDate))
        {
            Dotenv env = Dotenv.configure()
                    .filename(".spotify")
                    .load();

            String clientID = env.get("SPOTIFY_CLIENT_ID");
            String clientSecret = env.get("SPOTIFY_CLIENT_SECRET");
            URI redirectUri = SpotifyHttpManager.makeUri("https://apujar.blockyjar.dev/oauth2/spotify/callback.html");

            spotifyAPI = new SpotifyApi.Builder()
                    .setClientId(clientID)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(redirectUri)
                    .build();

            AuthorizationCodeRefreshRequest refreshRequest = spotifyAPI.authorizationCodeRefresh()
                    .grant_type("refresh_token")
                    .refresh_token(refreshToken)
                    .build();

            AuthorizationCodeCredentials authCodeCredentials = refreshRequest.execute();

            accessToken = authCodeCredentials.getAccessToken();
            int expiresIn = authCodeCredentials.getExpiresIn();

            expiresOnLocalDate = now.plusSeconds(expiresIn);

            SQLite.onUpdate(STR."UPDATE spotifyCredentials SET accessToken = '\{accessToken}', expiresOn = '\{expiresOnLocalDate}' WHERE userID = \{userID}");

            spotifyAPI.setAccessToken(accessToken);
        }
        return spotifyAPI;
    }
}
