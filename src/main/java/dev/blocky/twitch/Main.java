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
package dev.blocky.twitch;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.util.ThreadUtils;
import com.github.twitch4j.helix.TwitchHelix;
import dev.blocky.api.exceptions.Unauthorized;
import dev.blocky.twitch.manager.CommandManager;
import dev.blocky.twitch.manager.PrivateCommandManager;
import dev.blocky.twitch.manager.TwitchConfigurator;
import dev.blocky.twitch.scheduler.InformationMessageScheduler;
import dev.blocky.twitch.sql.SQLite;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.cdimascio.dotenv.Dotenv;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static TwitchClient client;
    private static long startedAt;

    public static String sevenTVAccessToken;

    public static void main(String[] args) throws SQLException, SchedulerException
    {
        startedAt = System.currentTimeMillis();
        new Main();
    }

    public Main() throws SQLException, SchedulerException
    {
        SQLite.connect().initDatabase();

        Dotenv env = Dotenv.configure()
                .filename(".twitch")
                .load();

        String clientID = env.get("CLIENT_ID");
        String accessToken = env.get("ACCESS_TOKEN");
        String refreshToken = env.get("REFRESH_TOKEN");

        sevenTVAccessToken = env.get("SEVENTV_ACCESS_TOKEN");

        TwitchIdentityProvider tip = new TwitchIdentityProvider(clientID, null, null);

        Supplier<OAuth2Credential> readCredentialFromFile = () ->
        {
            OAuth2Credential cred = new OAuth2Credential("twitch", accessToken);
            cred.setRefreshToken(refreshToken);
            return cred;
        };

        Consumer<OAuth2Credential> saveCredentialToFile = token ->
        {
            try
            {
                File envFile = new File("src/main/resources/.twitch");
                Path envPath = envFile.toPath();

                String refrehedAccessToken = token.getAccessToken();
                String refreshedRefreshToken = token.getRefreshToken();

                String newEnvContent = STR.
                        """
                                ACCESS_TOKEN=\{refrehedAccessToken}
                                REFRESH_TOKEN=\{refreshedRefreshToken}
                                CLIENT_ID=\{clientID}
                                SEVENTV_ACCESS_TOKEN=\{sevenTVAccessToken}
                                """;

                Files.writeString(envPath, newEnvContent);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        };

        OAuth2Credential initialToken = readCredentialFromFile.get();
        OAuth2Credential credential = tip.getAdditionalCredentialInformation(initialToken).orElseGet
                        (
                                () -> tip.refreshCredential(initialToken)
                                        .flatMap(tip::getAdditionalCredentialInformation)
                                        .orElse(null)
                        );

        int expiresIn = credential.getExpiresIn();

        if (credential == null)
        {
            logger.error("Invalid Twitch access-token specified.", new Unauthorized("Invalid Twitch access-token specified."));
            return;
        }

        if (credential != initialToken)
        {
            saveCredentialToFile.accept(credential);
        }

        Runtime runtime = Runtime.getRuntime();
        int processors = runtime.availableProcessors();

        ScheduledThreadPoolExecutor exec = ThreadUtils.getDefaultScheduledThreadPoolExecutor("twitch4j", processors);

        if (expiresIn > 0)
        {
            exec.scheduleAtFixedRate(
                    () -> tip.refreshCredential(credential)
                            .ifPresent(cr ->
                                    {
                                        String refreshedAccessToken = cr.getAccessToken();
                                        String refreshedRefreshToken = cr.getRefreshToken();
                                        int refreshedExpiresIn = cr.getExpiresIn();

                                        credential.setAccessToken(refreshedAccessToken);
                                        credential.setRefreshToken(refreshedRefreshToken);
                                        credential.setExpiresIn(refreshedExpiresIn);

                                        saveCredentialToFile.accept(credential);
                                    }
                            ), expiresIn / 2, 3600, SECONDS);
        }

        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(tip);

        TwitchClientBuilder clientBuilder = TwitchClientBuilder.builder()
                .withDefaultAuthToken(credential)
                .withChatAccount(credential)
                .withEnableHelix(true)
                .withEnableChat(true);

        client = clientBuilder.build();

        TwitchConfigurator configurator = new TwitchConfigurator(client);
        configurator.configure();

        EventManager eventManager = client.getEventManager();
        SimpleEventHandler eventHandler = eventManager.getEventHandler(SimpleEventHandler.class);

        TwitchHelix helix = client.getHelix();

        new CommandManager(eventHandler, client);
        new PrivateCommandManager(eventHandler, helix);

        new InformationMessageScheduler();

        cli();
    }

    private static void cli()
    {
        new Thread(() ->
        {
            try
            {
                InputStreamReader streamReader = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(streamReader);

                TwitchChat chat = client.getChat();

                while (true)
                {
                    String line = reader.readLine();

                    if (line.equalsIgnoreCase("exit"))
                    {
                        chat.sendMessage("ApuJar", "ManFeels Preparing to shutdown...");

                        for (int i = 5; i > 0; i--)
                        {
                            if (i != 1)
                            {
                                logger.info(STR."Bot stops in \{i} seconds.");
                            }

                            if (i == 1)
                            {
                                chat.sendMessage("ApuJar", "GigaSignal Disconnecting from Twitch websocket...");

                                logger.info("Bot stops in 1 second.");
                            }

                            TimeUnit.SECONDS.sleep(1);
                        }

                        client.close();
                        SQLite.disconnect();

                        System.exit(0);
                    }

                    if (line.equalsIgnoreCase("reconnect"))
                    {
                        logger.info("Trying to reconnect to Twitch websocket.");

                        chat.reconnect();

                        chat.sendMessage("ApuJar", "GigaSignal Successfully reconncted to Twitch websocket...");
                    }
                }
            }
            catch (IOException | InterruptedException | SQLException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    @Nullable
    public static TwitchClient getTwitchClient()
    {
        return client;
    }

    public static long getStartedAt()
    {
        return startedAt;
    }
}
