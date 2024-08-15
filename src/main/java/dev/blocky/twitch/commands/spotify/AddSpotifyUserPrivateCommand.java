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
package dev.blocky.twitch.commands.spotify;

import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import dev.blocky.twitch.interfaces.IPrivateCommand;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.utils.SQLUtils;
import dev.blocky.twitch.serialization.SpotifyUser;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.cdimascio.dotenv.Dotenv;
import org.joda.time.LocalDateTime;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.net.URI;

import static dev.blocky.twitch.utils.TwitchUtils.sendWhisper;

public class AddSpotifyUserPrivateCommand implements IPrivateCommand
{
    @Override
    public boolean onPrivateCommand(@NonNull PrivateMessageEvent event, @NonNull String[] messageParts) throws Exception
    {
        EventUser eventUser = event.getUser();
        String eventUserID = eventUser.getId();
        int eventUserIID = Integer.parseInt(eventUserID);

        if (messageParts.length == 1)
        {
            sendWhisper(eventUserID, "FeelsMan Please specify the Spotify authorization code that was generated by https://apujar.blockyjar.dev/oauth2/spotify.html FeelsOkayMan");
            return false;
        }

        String authCode = messageParts[1];

        if (!authCode.startsWith("AQ") && !authCode.matches("^[\\w-]{340}$"))
        {
            sendWhisper(eventUserID, "FeelsDankMan Invalid authorization code specified. (Authorization code must start with 'AQ' and contain 340 case insensitive letters with underscores [_], minuses [-] and numbers)");
            return false;
        }

        Dotenv env = Dotenv.configure()
                .filename(".spotify")
                .load();

        String clientID = env.get("SPOTIFY_CLIENT_ID");
        String clientSecret = env.get("SPOTIFY_CLIENT_SECRET");
        URI redirectUri = SpotifyHttpManager.makeUri("https://apujar.blockyjar.dev/oauth2/spotify/callback.html");

        SpotifyApi spotifyAPI = new SpotifyApi.Builder()
                .setClientId(clientID)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();

        AuthorizationCodeRequest authorizationCodeRequest = spotifyAPI.authorizationCode(authCode).build();
        AuthorizationCodeCredentials authCodeCredentials = authorizationCodeRequest.execute();

        String accessToken = authCodeCredentials.getAccessToken();
        String refreshToken = authCodeCredentials.getRefreshToken();
        int expiresIn = authCodeCredentials.getExpiresIn();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresOn = now.plusSeconds(expiresIn);

        SpotifyUser spotifyUser = SQLUtils.getSpotifyUser(eventUserIID);

        if (spotifyUser == null)
        {
            SQLite.onUpdate(STR."INSERT INTO spotifyCredentials(userID, accessToken, refreshToken, expiresOn) VALUES(\{eventUserIID}, '\{accessToken}', '\{refreshToken}', '\{expiresOn}')");
            sendWhisper(eventUserID, "SeemsGood Successfully added Spotify credentials.");
            return false;
        }

        SQLite.onUpdate(STR."UPDATE spotifyCredentials SET accessToken = '\{accessToken}', refreshToken = '\{refreshToken}', expiresOn = '\{expiresOn}' WHERE userID = \{eventUserIID}");

        sendWhisper(eventUserID, "SeemsGood Successfully updated Spotify credential in the database.");
        return true;
    }
}
