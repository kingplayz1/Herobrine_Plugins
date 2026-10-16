package com.kingplayz.herobrine.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Repository for managing encounters in the database.
 * Handles storing and retrieving encounter data for persistence across restarts.
 */
public class EncounterRepository {

    private final DatabaseManager dbManager;

    public EncounterRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves an encounter to the database.
     * @param encounter The encounter to save
     */
    public void saveEncounter(Encounter encounter) {
        String sql = """
                INSERT OR REPLACE INTO encounters
                (encounter_id, player_uuid, world_uuid, state, started_at, ended_at, outcome, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, encounter.getEncounterId());
            stmt.setString(2, encounter.getPlayerUuid().toString());
            stmt.setString(3, encounter.getWorldUuid().toString());
            stmt.setString(4, encounter.getState());
            stmt.setLong(5, encounter.getStartedAt());
            stmt.setLong(6, encounter.getEndedAt());
            stmt.setString(7, encounter.getOutcome());
            stmt.setString(8, encounter.getVersion());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save encounter: " + e.getMessage());
        }
    }

    /**
     * Gets an encounter by its ID.
     * @param encounterId The encounter ID
     * @return The encounter or null if not found
     */
    public Encounter getEncounter(String encounterId) {
        String sql = """
                SELECT * FROM encounters WHERE encounter_id = ?
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, encounterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Encounter(
                            rs.getString("encounter_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            UUID.fromString(rs.getString("world_uuid")),
                            rs.getString("state"),
                            rs.getLong("started_at"),
                            rs.getLong("ended_at"),
                            rs.getString("outcome"),
                            rs.getString("version")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get encounter: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all active encounters for a player.
     * @param playerUuid The player's UUID
     * @return List of active encounters
     */
    public List<Encounter> getActiveEncountersForPlayer(UUID playerUuid) {
        List<Encounter> encounters = new ArrayList<>();
        String sql = """
                SELECT * FROM encounters
                WHERE player_uuid = ? AND ended_at IS NULL
                ORDER BY started_at DESC
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    encounters.add(new Encounter(
                            rs.getString("encounter_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            UUID.fromString(rs.getString("world_uuid")),
                            rs.getString("state"),
                            rs.getLong("started_at"),
                            rs.getLong("ended_at"),
                            rs.getString("outcome"),
                            rs.getString("version")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get active encounters for player: " + e.getMessage());
        }
        return encounters;
    }

    /**
     * Updates an encounter in the database.
     * @param encounter The encounter to update
     */
    public void updateEncounter(Encounter encounter) {
        String sql = """
                UPDATE encounters SET
                state = ?,
                ended_at = ?,
                outcome = ?
                WHERE encounter_id = ?
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, encounter.getState());
            stmt.setLong(2, encounter.getEndedAt());
            stmt.setString(3, encounter.getOutcome());
            stmt.setString(4, encounter.getEncounterId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update encounter: " + e.getMessage());
        }
    }

    /**
     * Gets encounter history for a player (limited to recent encounters).
     * @param playerUuid The player's UUID
     * @param limit Maximum number of encounters to return
     * @return List of encounters
     */
    public List<Encounter> getEncounterHistory(UUID playerUuid, int limit) {
        List<Encounter> encounters = new ArrayList<>();
        String sql = """
                SELECT * FROM encounters
                WHERE player_uuid = ?
                ORDER BY started_at DESC
                LIMIT ?
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    encounters.add(new Encounter(
                            rs.getString("encounter_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            UUID.fromString(rs.getString("world_uuid")),
                            rs.getString("state"),
                            rs.getLong("started_at"),
                            rs.getLong("ended_at"),
                            rs.getString("outcome"),
                            rs.getString("version")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get encounter history: " + e.getMessage());
        }
        return encounters;
    }

    /**
     * Data class representing an encounter.
     */
    public static class Encounter {
        private final String encounterId;
        private final UUID playerUuid;
        private final UUID worldUuid;
        private final String state;
        private final long startedAt;
        private final Long endedAt; // Nullable
        private final String outcome; // Nullable
        private final String version;

        public Encounter(String encounterId, UUID playerUuid, UUID worldUuid,
                         String state, long startedAt, Long endedAt,
                         String outcome, String version) {
            this.encounterId = encounterId;
            this.playerUuid = playerUuid;
            this.worldUuid = worldUuid;
            this.state = state;
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.outcome = outcome;
            this.version = version;
        }

        public String getEncounterId() {
            return encounterId;
        }

        public UUID getPlayerUuid() {
            return playerUuid;
        }

        public UUID getWorldUuid() {
            return worldUuid;
        }

        public String getState() {
            return state;
        }

        public long getStartedAt() {
            return startedAt;
        }

        public Long getEndedAt() {
            return endedAt;
        }

        public String getOutcome() {
            return outcome;
        }

        public String getVersion() {
            return version;
        }

        public boolean isActive() {
            return endedAt == null;
        }
    }
}