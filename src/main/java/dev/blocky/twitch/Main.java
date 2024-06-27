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
import com.github.philippheuer.events4j.api.IEventManager;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.util.ThreadUtils;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.helix.TwitchHelix;
import dev.blocky.api.exceptions.Unauthorized;
import dev.blocky.twitch.manager.CommandManager;
import dev.blocky.twitch.manager.PrivateCommandManager;
import dev.blocky.twitch.manager.SQLite;
import dev.blocky.twitch.manager.TwitchConfigurator;
import dev.blocky.twitch.scheduler.HolidayScheduler;
import dev.blocky.twitch.scheduler.InformationMessageScheduler;
import dev.blocky.twitch.scheduler.TicTacToeScheduler;
import dev.blocky.twitch.utils.OSUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.blocky.twitch.utils.TwitchUtils.sendChatMessage;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static TwitchClient client;
    public static TwitchHelix helix;

    public static long startedAt;

    public static String accessToken, clientID;

    public static String sevenTVAccessToken;

    public static String streamElementsJWTToken;

    public static void main(String[] args) throws Exception
    {
        startedAt = System.currentTimeMillis();

        new Main();
    }

    public Main() throws Exception
    {
        SQLite.connect().initDatabase();

        String directoryPath = OSUtils.getDirectoryPath();

        Dotenv env = Dotenv.configure()
                .directory(directoryPath)
                .filename(".twitch")
                .load();

        accessToken = env.get("ACCESS_TOKEN");
        String refreshToken = env.get("REFRESH_TOKEN");
        clientID = env.get("CLIENT_ID");

        sevenTVAccessToken = env.get("SEVENTV_ACCESS_TOKEN");

        streamElementsJWTToken = env.get("STREAMELEMENTS_JWT_TOKEN");

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
                String filePath = OSUtils.getFilePath(".twitch");

                File envFile = new File(filePath);
                Path envPath = envFile.toPath();

                String refrehedAccessToken = token.getAccessToken();
                String refreshedRefreshToken = token.getRefreshToken();

                String newEnvContent = STR.
                        """
                                ACCESS_TOKEN=\{refrehedAccessToken}
                                REFRESH_TOKEN=\{refreshedRefreshToken}
                                CLIENT_ID=\{clientID}
                                SEVENTV_ACCESS_TOKEN=\{sevenTVAccessToken}
                                STREAMELEMENTS_JWT_TOKEN=\{streamElementsJWTToken}
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

        if (credential == null)
        {
            logger.error("Invalid Twitch access-token specified.", new Unauthorized("Invalid Twitch access-token specified."));
            return;
        }

        int expiresIn = credential.getExpiresIn();

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
                .withEnableEventSocket(true)
                .withEnableChat(true);

        client = clientBuilder.build();
        helix = client.getHelix();

        IEventSubSocket eventSocket = client.getEventSocket();
        EventManager clientEventManger = client.getEventManager();
        TwitchChat chat = client.getChat();

        TwitchConfigurator configurator = new TwitchConfigurator(eventSocket, clientEventManger, chat);
        configurator.configure();

        IEventManager eventManager = eventSocket.getEventManager();

        new CommandManager(eventManager);
        new PrivateCommandManager(eventManager);

        new InformationMessageScheduler();
        new TicTacToeScheduler();

        new HolidayScheduler();

        /*
         *  new StreamAwardsScheduler();
         *  new AprilFoolsScheduler();
         */

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
                        sendChatMessage("896181679", "ManFeels Preparing to shutdown...");

                        for (int i = 5; i > 0; i--)
                        {
                            if (i != 1)
                            {
                                logger.info(STR."Bot stops in \{i} seconds.");
                            }

                            if (i == 1)
                            {
                                sendChatMessage("896181679", "GigaSignal Disconnecting from Twitch websocket...");

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

                        sendChatMessage("896181679", "GigaSignal Successfully reconnected to Twitch websocket...");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }).start();
    }
}
