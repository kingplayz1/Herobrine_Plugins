package com.kingplayz.herobrine.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Repository for managing player profiles in the database.
 * Handles player opt-out status, haunting levels, and encounter history.
 */
public class PlayerProfileRepository {

    private final DatabaseManager dbManager;

    public PlayerProfileRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Gets a player's profile, creating it if it doesn't exist.
     * @param playerUuid The player's UUID
     * @return The player profile
     */
    public PlayerProfile getPlayerProfile(UUID playerUuid) {
        String sql = """
                SELECT * FROM player_profiles WHERE player_uuid = ?
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlayerProfile(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getInt("opted_out") == 1,
                            rs.getInt("haunting_level"),
                            rs.getLong("last_encounter_at"),
                            rs.getLong("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            // Log but don't crash - return default profile
            // In a real implementation, we'd use a proper logger
            System.err.println("Failed to get player profile: " + e.getMessage());
        }

        // If not found or error, create a default profile
        PlayerProfile defaultProfile = new PlayerProfile(playerUuid, false, 0, 0, System.currentTimeMillis());
        savePlayerProfile(defaultProfile);
        return defaultProfile;
    }

    /**
     * Saves a player's profile to the database.
     * @param profile The player profile to save
     */
    public void savePlayerProfile(PlayerProfile profile) {
        String sql = """
                INSERT OR REPLACE INTO player_profiles
                (player_uuid, opted_out, haunting_level, last_encounter_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profile.getPlayerUuid().toString());
            stmt.setInt(2, profile.isOptedOut() ? 1 : 0);
            stmt.setInt(3, profile.getHauntingLevel());
            stmt.setLong(4, profile.getLastEncounterAt());
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save player profile: " + e.getMessage());
        }
    }

    /**
     * Sets whether a player has opted out of hauntings.
     * @param playerUuid The player's UUID
     * @param optedOut True if opted out, false otherwise
     */
    public void setPlayerOptedOut(UUID playerUuid, boolean optedOut) {
        PlayerProfile profile = getPlayerProfile(playerUuid);
        profile.setOptedOut(optedOut);
        savePlayerProfile(profile);
    }

    /**
     * Checks if a player has opted out of hauntings.
     * @param playerUuid The player's UUID
     * @return True if opted out, false otherwise
     */
    public boolean isPlayerOptedOut(UUID playerUuid) {
        return getPlayerProfile(playerUuid).isOptedOut();
    }

    /**
     * Records that a player had an encounter at the current time.
     * @param playerUuid The player's UUID
     */
    public void recordPlayerEncounter(UUID playerUuid) {
        PlayerProfile profile = getPlayerProfile(playerUuid);
        profile.setLastEncounterAt(System.currentTimeMillis());
        // In a full implementation, we might increment haunting level here
        savePlayerProfile(profile);
    }

    /**
     * Gets how long ago the player's last encounter was (in milliseconds).
     * Returns Long.MAX_VALUE if never encountered.
     * @param playerUuid The player's UUID
     * @return Time since last encounter
     */
    public long getTimeSinceLastEncounter(UUID playerUuid) {
        PlayerProfile profile = getPlayerProfile(playerUuid);
        long lastEncounter = profile.getLastEncounterAt();
        if (lastEncounter == 0) {
            return Long.MAX_VALUE; // Never encountered
        }
        return System.currentTimeMillis() - lastEncounter;
    }

    /**
     * Increases a player's haunting level.
     * @param playerUuid The player's UUID
     * @param amount The amount to increase by
     */
    public void increaseHauntingLevel(UUID playerUuid, int amount) {
        PlayerProfile profile = getPlayerProfile(playerUuid);
        profile.setHauntingLevel(profile.getHauntingLevel() + amount);
        savePlayerProfile(profile);
    }

    /**
     * Gets a player's current haunting level.
     * @param playerUuid The player's UUID
     * @return The haunting level
     */
    public int getHauntingLevel(UUID playerUuid) {
        return getPlayerProfile(playerUuid).getHauntingLevel();
    }

    /**
     * Data class representing a player's profile.
     */
    public static class PlayerProfile {
        private final UUID playerUuid;
        private boolean optedOut;
        private int hauntingLevel;
        private long lastEncounterAt;
        private long updatedAt;

        public PlayerProfile(UUID playerUuid, boolean optedOut, int hauntingLevel, long lastEncounterAt, long updatedAt) {
            this.playerUuid = playerUuid;
            this.optedOut = optedOut;
            this.hauntingLevel = hauntingLevel;
            this.lastEncounterAt = lastEncounterAt;
            this.updatedAt = updatedAt;
        }

        public UUID getPlayerUuid() {
            return playerUuid;
        }

        public boolean isOptedOut() {
            return optedOut;
        }

        public void setOptedOut(boolean optedOut) {
            this.optedOut = optedOut;
        }

        public int getHauntingLevel() {
            return hauntingLevel;
        }

        public void setHauntingLevel(int hauntingLevel) {
            this.hauntingLevel = hauntingLevel;
        }

        public long getLastEncounterAt() {
            return lastEncounterAt;
        }

        public void setLastEncounterAt(long lastEncounterAt) {
            this.lastEncounterAt = lastEncounterAt;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}