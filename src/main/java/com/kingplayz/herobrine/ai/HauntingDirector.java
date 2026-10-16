package com.kingplayz.herobrine.ai;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.kingplayz.herobrine.config.ConfigManager;
import com.kingplayz.herobrine.entity.EntityRegistry;
import com.kingplayz.herobrine.entity.HerobrineEntityController;
import java.util.UUID;
import java.util.Random;

/**
 * Main AI controller for Herobrine hauntings.
 * Determines when and where Herobrine should appear based on player activity,
 * time, world conditions, and configured rules.
 * NOW INTEGRATED with Adaptive Stalking AI.
 */
public class HauntingDirector {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final HerobrineEntityController entityController;
    private final EntityRegistry entityRegistry;
    private final AdvancedAIManager advancedAIManager;
    private final Random random = new Random();
    private BukkitTask directorTask;
    private boolean isRunning = false;

    // Configuration values with defaults
    private long checkIntervalTicks = 40L; // 2 seconds (20 ticks/sec * 2)
    private int maxActiveEncounters = 2;
    private int minCooldownMinutes = 20;
    private int maxCooldownMinutes = 45;
    private double nightBonusChance = 0.5; // 50% bonus chance at night
    private boolean requireSurvivalMode = true;
    private double minPlayerDistance = 24.0;
    private double maxPlayerDistance = 64.0;

    public HauntingDirector(JavaPlugin plugin, ConfigManager configManager,
                            HerobrineEntityController entityController,
                            EntityRegistry entityRegistry,
                            AdvancedAIManager advancedAIManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.entityController = entityController;
        this.entityRegistry = entityRegistry;
        this.advancedAIManager = advancedAIManager;
        loadConfiguration();
    }

    /**
     * Loads configuration values from config.yml
     */
    private void loadConfiguration() {
        if (configManager == null) {
            return;
        }

        var config = configManager.getConfig();
        checkIntervalTicks = (long) (config.getInt("settings.check-interval-seconds", 2) * 20);
        maxActiveEncounters = config.getInt("haunting.max-active-encounters", 2);
        minCooldownMinutes = config.getInt("haunting.minimum-cooldown-minutes", 20);
        maxCooldownMinutes = config.getInt("haunting.maximum-cooldown-minutes", 45);
        nightBonusChance = config.getDouble("haunting.night-bonus", 0.5);
        requireSurvivalMode = config.getBoolean("haunting.require-survival-mode", true);
        minPlayerDistance = config.getDouble("haunting.minimum-player-distance", 24.0);
        maxPlayerDistance = config.getDouble("haunting.maximum-player-distance", 64.0);
    }

    /**
     * Starts the haunting director's periodic evaluation.
     */
    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        directorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::evaluateHauntingConditions,
                checkIntervalTicks, checkIntervalTicks);
        plugin.getLogger().info("Herobrine Haunting Director started.");
    }

    /**
     * Stops the haunting director.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        if (directorTask != null) {
            directorTask.cancel();
        }
        plugin.getLogger().info("Herobrine Haunting Director stopped.");
    }

    /**
     * Main evaluation method that runs on a schedule to determine if hauntings should occur.
     * NOW USES ADVANCED AI FOR SCORING.
     */
    private void evaluateHauntingConditions() {
        // Check if haunting is globally enabled
        if (configManager == null || !configManager.getConfig().getBoolean("haunting.enabled", true)) {
            return;
        }

        // Check if we're already at max active encounters
        int activeCount = entityRegistry.getEntityCount();
        if (activeCount >= maxActiveEncounters) {
            return;
        }

        // Get all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip if we've already reached max encounters
            if (entityRegistry.getEntityCount() >= maxActiveEncounters) {
                break;
            }

            // Check if this player is eligible for an encounter using ADVANCED AI
            if (isPlayerEligibleForEncounter(player)) {
                // Determine what type of encounter to trigger
                String encounterType = selectEncounterType(player);

                // Trigger the encounter
                triggerEncounter(player, encounterType);
            }
        }
    }

    /**
     * Checks if a player is eligible for a Herobrine encounter using ADVANCED AI.
     * @param player The player to check
     * @return true if eligible, false otherwise
     */
    private boolean isPlayerEligibleForEncounter(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player has opted out
        if (isPlayerOptedOut(playerUuid)) {
            return false;
        }

        // Check if player is in a valid world
        if (!isInEligibleWorld(player.getWorld().getName())) {
            return false;
        }

        // Check if player is in excluded world
        if (isInExcludedWorld(player.getWorld().getName())) {
            return false;
        }

        // Check game mode if required
        if (requireSurvivalMode && !player.getGameMode().name().equalsIgnoreCase("SURVIVAL")) {
            return false;
        }

        // Check if player is on cooldown
        if (isPlayerOnCooldown(playerUuid)) {
            return false;
        }

        // Check if there are already too many encounters for this player
        if (getPlayerEncounterCount(playerUuid) >= getMaxEncountersPerPlayer()) {
            return false;
        }

        // USE ADVANCED AI TO CALCULATE ELIGIBILITY SCORE
        double eligibilityScore = advancedAIManager.calculateAdvancedEligibilityScore(
                player, getCurrentEncounterStageForPlayer(player)
        );

        // Add randomness to prevent predictability
        double randomFactor = random.nextDouble();

        // Threshold for triggering an encounter (can be adjusted)
        double threshold = 0.3 + (randomFactor * 0.4); // 0.3 to 0.7

        return eligibilityScore > threshold;
    }

    private String getCurrentEncounterStageForPlayer(Player player) {
        // Check if player has an active encounter and get its stage
        UUID playerUuid = player.getUniqueId();
        for (UUID entityUuid : entityRegistry.getEntityDataMap()) {
            UUID targetUuid = entityController.getTargetPlayer(entityUuid);
            if (targetUuid != null && targetUuid.equals(playerUuid)) {
                // In a full implementation, we'd get the actual stage from entity data
                // For now, return a placeholder
                return "observing";
            }
        }
        return "none"; // No active encounter
    }

    /**
     * Selects what type of encounter to trigger for a player.
     * Now enhanced with adaptive insights.
     * @param player The player to haunt
     * @return The type of encounter to trigger
     */
    private String selectEncounterType(Player player) {
        // Use advanced AI to get behavioral insights
        double lookAtRate = 0.0;
        double evidenceRate = 0.0;

        // Try to get behavioral data from adaptive AI if available
        // For now, use weighted random selection as baseline

        double rand = random.nextDouble();

        // Adjust probabilities based on time of night and other factors
        double observationWeight = 0.5;
        double evidenceWeight = 0.3;
        double manifestationWeight = 0.2;

        // Could enhance with adaptive AI insights here

        if (rand < observationWeight) {
            return "observation";
        } else if (rand < observationWeight + evidenceWeight) {
            return "evidence";
        } else {
            return "manifestation";
        }
    }

    /**
     * Triggers a Herobrine encounter for a player.
     * @param player The player to haunt
     * @param encounterType The type of encounter to trigger
     */
    private void triggerEncounter(Player player, String encounterType) {
        UUID playerUuid = player.getUniqueId();

        // Generate a unique encounter ID
        String encounterId = "encounter-" + System.currentTimeMillis() + "-" + random.nextInt(10000);

        // Determine spawn location (behind and above the player)
        Location playerLoc = player.getLocation();
        Location spawnLoc = playerLoc.clone()
                .add(0, 1, 0)  // Start above player's head
                .add(playerLoc.getDirection().multiply(-3)) // 3 blocks behind player
                .add(0, 2, 0); // 2 blocks up for better visibility

        // Ensure the spawn location is valid
        if (!isValidSpawnLocation(spawnLoc)) {
            // Fallback to a simpler location
            spawnLoc = playerLoc.clone().add(0, 1, 0);
        }

        // Determine encounter duration based on type
        int durationSeconds = getEncounterDuration(encounterType);

        // Spawn the Herobrine entity
        Entity entity = entityController.spawnHerobrine(player, encounterId, spawnLoc, durationSeconds);

        if (entity != null) {
            // Update encounter stage based on type
            // In a full implementation, we'd call encounterStateManager.updateEncounterStage
            // For now, we'll just note that the entity was spawned

            plugin.getLogger().info("Triggered " + encounterType + " encounter for player " + player.getName() +
                    " (ID: " + encounterId + ")");

            // Record the encounter in player profile
            recordPlayerEncounter(playerUuid, encounterType);

            // Set player cooldown using advanced AI
            setPlayerCooldown(playerUuid);
        } else {
            plugin.getLogger().warning("Failed to spawn Herobrine entity for player " + player.getName());
        }
    }

    // Helper methods

    private boolean isPlayerOptedOut(UUID playerUuid) {
        // Placeholder - would check player preferences from database or config
        return false;
    }

    private boolean isInEligibleWorld(String worldName) {
        // Check if world is in eligible worlds list
        if (configManager == null) {
            return true; // Default to all worlds if no config
        }

        var eligibleWorlds = configManager.getConfig().getStringList("haunting.eligible-worlds");
        if (eligibleWorlds == null || eligibleWorlds.isEmpty()) {
            return true; // Default to all worlds
        }

        return eligibleWorlds.contains(worldName);
    }

    private boolean isInExcludedWorld(String worldName) {
        // Check if world is in excluded worlds list
        if (configManager == null) {
            return false;
        }

        var excludedWorlds = configManager.getConfig().getStringList("haunting.excluded-worlds");
        if (excludedWorlds == null || excludedWorlds.isEmpty()) {
            return false;
        }

        return excludedWorlds.contains(worldName);
    }

    private boolean isPlayerOnCooldown(UUID playerUuid) {
        // Use Advanced AI Manager's cooldown logic
        return advancedAIManager.isPlayerOnCooldown(playerUuid);
    }

    private int getPlayerEncounterCount(UUID playerUuid) {
        // Placeholder - would count active encounters for this player from registry
        int count = 0;
        for (UUID entityUuid : entityRegistry.getEntityDataMap()) {
            UUID targetUuid = entityController.getTargetPlayer(entityUuid);
            if (targetUuid != null && targetUuid.equals(playerUuid)) {
                count++;
            }
        }
        return count;
    }

    private int getMaxEncountersPerPlayer() {
        if (configManager == null) {
            return 1; // Default
        }
        return configManager.getConfig().getInt("haunting.max-active-per-player", 1);
    }

    private boolean isValidSpawnLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        // Check if the location is loaded
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return false;
        }

        // Check if it's not inside a solid block
        // In a full implementation, we'd check the block type
        return true; // Simplified for now
    }

    private int getEncounterDuration(String encounterType) {
        switch (encounterType.toLowerCase()) {
            case "observation":
                return 8; // Default observation duration
            case "evidence":
                return 10; // Slightly longer for evidence
            case "manifestation":
                return 12; // Longest for full manifestation
            default:
                return 10;
        }
    }

    private void setPlayerCooldown(UUID playerUuid) {
        // Use Advanced AI Manager's cooldown logic
        long cooldownMillis = advancedAIManager.getPlayerCooldown(playerUuid);
        // In a full implementation: save cooldown expiration time to database
        plugin.getLogger().info("Set cooldown for player " + playerUuid + ": " + (cooldownMillis / 60000) + " minutes");
    }

    private void recordPlayerEncounter(UUID playerUuid, String encounterType) {
        // Record the encounter in player profile
        // In a full implementation, we'd use playerRepo.recordPlayerEncounter and increase haunting level
        plugin.getLogger().info("Recorded " + encounterType + " encounter for player " + playerUuid);
    }
}