package com.kingplayz.herobrine.persistence;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages the SQLite database connection for the Herobrine plugin.
 * Handles connection pooling and thread safety.
 */
public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;
    private final String databasePath;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.databasePath = "jdbc:sqlite:" + plugin.getDataFolder() + "/herobrine.db";
        initializeDatabase();
    }

    /**
     * Initializes the database connection and creates tables if needed.
     */
    private void initializeDatabase() {
        try {
            // Ensure the data folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection
            connection = DriverManager.getConnection(databasePath);
            connection.setAutoCommit(true);

            // Create tables if they don't exist
            createTables();

            plugin.getLogger().info("Database initialized successfully at " + databasePath);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found. Please add it to dependencies.", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    /**
     * Creates the necessary tables if they don't exist.
     */
    private void createTables() {
        String createPlayerProfilesTable = """
                CREATE TABLE IF NOT EXISTS player_profiles (
                    player_uuid TEXT PRIMARY KEY,
                    opted_out INTEGER NOT NULL DEFAULT 0,
                    haunting_level INTEGER NOT NULL DEFAULT 0,
                    last_encounter_at INTEGER,
                    updated_at INTEGER NOT NULL
                );
                """;

        String createEncountersTable = """
                CREATE TABLE IF NOT EXISTS encounters (
                    encounter_id TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL,
                    world_uuid TEXT NOT NULL,
                    state TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    ended_at INTEGER,
                    outcome TEXT,
                    version TEXT NOT NULL
                );
                """;

        String createWorldChangesTable = """
                CREATE TABLE IF NOT EXISTS world_changes (
                    change_id TEXT PRIMARY KEY,
                    encounter_id TEXT NOT NULL,
                    world_uuid TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    original_data TEXT NOT NULL,
                    expected_modified_data TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    restored_at INTEGER
                );
                """;

        String createEncounterPlayerIndex = """
                CREATE INDEX IF NOT EXISTS idx_encounters_player
                ON encounters(player_uuid, started_at);
                """;

        String createWorldChangesStatusIndex = """
                CREATE INDEX IF NOT EXISTS idx_world_changes_status
                ON world_changes(status, encounter_id);
                """;

        try {
            var statement = connection.createStatement();
            statement.execute(createPlayerProfilesTable);
            statement.execute(createEncountersTable);
            statement.execute(createWorldChangesTable);
            statement.execute(createEncounterPlayerIndex);
            statement.execute(createWorldChangesStatusIndex);
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables", e);
        }
    }

    /**
     * Gets the database connection.
     * @return The database connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeDatabase();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check connection status", e);
        }
        return connection;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }
}