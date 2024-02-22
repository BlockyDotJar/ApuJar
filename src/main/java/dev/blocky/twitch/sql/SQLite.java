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
package dev.blocky.twitch.sql;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;

public class SQLite
{
    private static final Logger logger = LoggerFactory.getLogger(SQLite.class);

    private static Connection conn;
    private static Statement stmt;
    private static File file;

    @NonNull
    public static SQLite connect() throws SQLException
    {
        file = new File("src/main/resources/database.db");

        String filePath = file.getPath();
        String fileName = file.getName();

        conn = DriverManager.getConnection(STR."jdbc:sqlite:\{filePath}");
        stmt = conn.createStatement();

        logger.info(STR."Successfully connected to \{fileName}");
        return new SQLite();
    }

    public static void disconnect() throws SQLException
    {
        if (conn != null)
        {
            conn.close();

            String fileName = file.getName();

            logger.info(STR."Successfully disconnected from \{fileName}");
        }
    }

    public static void onUpdate(@NonNull String sql) throws SQLException
    {
        stmt.execute(sql);
    }

    @NonNull
    public static ResultSet onQuery(@NonNull String sql) throws SQLException
    {
        return stmt.executeQuery(sql);
    }

    public void initDatabase() throws SQLException
    {
        onUpdate("CREATE TABLE IF NOT EXISTS chats(userID INTEGER)");
        onUpdate("CREATE TABLE IF NOT EXISTS admins(userID INTEGER, isOwner BOOLEAN)");
        onUpdate("CREATE TABLE IF NOT EXISTS customPrefixes(userID INTEGER, prefix VARCHAR)");
        onUpdate("CREATE TABLE IF NOT EXISTS adminCommands(command VARCHAR, requiresOwner BOOLEAN)");
        onUpdate("CREATE TABLE IF NOT EXISTS bible(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userID INTEGER, entry VARCHAR)");
        onUpdate("CREATE TABLE IF NOT EXISTS customCommands(userID INTEGER, name VARCHAR, message VARCHAR)");
        onUpdate("CREATE TABLE IF NOT EXISTS customKeywords(userID INTEGER, name VARCHAR, message VARCHAR, equals BOOLEAN)");
        onUpdate("CREATE TABLE IF NOT EXISTS globalCommands(name VARCHAR, message VARCHAR)");
        onUpdate("CREATE TABLE IF NOT EXISTS spotifyCredentials(userID INTEGER, accessToken VARCHAR, refreshToken VARCHAR, expiresOn VARCHAR)");
        onUpdate("CREATE TABLE IF NOT EXISTS sevenTVUsers(userID INTEGER, allowedUserIDs VARCHAR)");
    }
}
