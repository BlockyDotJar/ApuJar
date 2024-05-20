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
package dev.blocky.twitch.manager;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static dev.blocky.twitch.utils.OSUtils.getFilePath;

public class SQLite
{
    private static final Logger logger = LoggerFactory.getLogger(SQLite.class);

    private static Connection conn;
    private static Statement stmt;
    private static File file;

    @NonNull
    public static SQLite connect() throws SQLException
    {
        String osFilePath = getFilePath("database.db");

        file = new File(osFilePath);

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

    public void initDatabase() throws SQLException, IOException
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            File file = new File("src/main/resources/table-creation.sql");
            Path path = file.toPath();

            String sql = Files.readString(path);

            stmt.executeLargeUpdate(sql);
        }
    }
}
