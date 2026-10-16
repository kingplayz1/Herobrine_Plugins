package com.kingplayz.herobrine.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.kingplayz.herobrine.world.AnomalyManager;
import com.kingplayz.herobrine.world.BlockChangeJournal;
import com.kingplayz.herobrine.audio.AudioDirector;
import com.kingplayz.herobrine.audio.AudioDirector.EncounterStage;

import java.util.UUID;

/**
 * Controls the Herobrine entity lifecycle, including spawning, movement, and despawning.
 * Works with AppearanceService to ensure proper visual representation.
 * Also handles world anomaly creation and audio as part of encounters.
 */
public class HerobrineEntityController {

    private final JavaPlugin plugin;
    private final AppearanceService appearanceService;
    private final EntityRegistry entityRegistry;
    private final AnomalyManager anomalyManager;
    private final BlockChangeJournal blockChangeJournal;
    private final AudioDirector audioDirector;

    public HerobrineEntityController(JavaPlugin plugin, AppearanceService appearanceService,
                                     EntityRegistry entityRegistry, AnomalyManager anomalyManager,
                                     BlockChangeJournal blockChangeJournal, AudioDirector audioDirector) {
        this.plugin = plugin;
        this.appearanceService = appearanceService;
        this.entityRegistry = entityRegistry;
        this.anomalyManager = anomalyManager;
        this.blockChangeJournal = blockChangeJournal;
        this.audioDirector = audioDirector;
    }

    /**
     * Spawns a Herobrine entity for a specific player encounter.
     * @param targetPlayer The player to haunt
     * @param encounterId Unique identifier for this encounter
     * @param spawnLocation Where to spawn the entity
     * @param durationSeconds How long the entity should exist before despawning
     * @return The spawned entity or null if failed
     */
    public Entity spawnHerobrine(Player targetPlayer, String encounterId, Location spawnLocation, int durationSeconds) {
        // Check if the world is loaded
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().warning("Cannot spawn Herobrine: world is not loaded at " + spawnLocation);
            return null;
        }

        // Create the entity using our appearance service
        Entity entity = appearanceService.createHerobrineEntity();
        if (entity == null) {
            plugin.getLogger().severe("Failed to create Herobrine entity");
            return null;
        }

        // Set the entity's location
        entity.teleport(spawnLocation);

        // Apply Herobrine's appearance
        appearanceService.applyHerobrineAppearance(entity);

        // Register the entity for tracking
        long now = System.currentTimeMillis();
        long expirationTime = now + (durationSeconds * 1000L);
        entityRegistry.registerEntity(entity.getUniqueId(), targetPlayer.getUniqueId(), encounterId, now, expirationTime);

        // Set entity to be silent and invisible if needed
        customizeEntity(entity);

        // Start encounter audio
        audioDirector.startEncounterAudio(targetPlayer.getUniqueId(), encounterId);

        plugin.getLogger().info("Spawned Herobrine entity for player " + targetPlayer.getName() + " at " + spawnLocation);
        return entity;
    }

    /**
     * Customizes the entity to behave like Herobrine (silent, no AI, etc.)
     * @param entity The entity to customize
     */
    private void customizeEntity(Entity entity) {
        // Remove AI if possible (version dependent)
        // Make silent
        // Prevent interaction with other entities
        // These would be version-specific implementations
        // For now, we'll rely on the armor stand being inert
    }

    /**
     * Despawns a Herobrine entity and cleans up its data
     * Also handles restoration of any world changes made during the encounter
     * @param entityUuid The UUID of the entity to despawn
     * @param encounterId The encounter ID associated with this entity
     */
    public void despawnHerobrine(UUID entityUuid, String encounterId) {
        Entity entity = Bukkit.getEntity(entityUuid);
        if (entity != null && !entity.isDead()) {
            entity.remove();
            plugin.getLogger().info("Despawned Herobrine entity " + entityUuid);
        }
        entityRegistry.unregisterEntity(entityUuid);

        // End encounter audio
        audioDirector.endEncounterAudio(entityUuid);

        // Restore world changes if enabled
        if (plugin.getConfig().getBoolean("world.restore-modified-blocks", true)) {
            int restored = blockChangeJournal.restoreAll(encounterId);
            if (restored > 0) {
                plugin.getLogger().info("Restored " + restored + " blocks for encounter " + encounterId);
            }
        }
    }

    /**
     * Updates an existing Herobrine entity's state
     * @param entityUuid The UUID of the entity to update
     * @param newLocation New location for the entity
     */
    public void updateHerobrineLocation(UUID entityUuid, Location newLocation) {
        Entity entity = Bukkit.getEntity(entityUuid);
        if (entity != null && !entity.isDead()) {
            entity.teleport(newLocation);
        }
    }

    /**
     * Updates the encounter stage and triggers appropriate effects
     * @param entityUuid The UUID of the Herobrine entity
     * @param stage The new encounter stage
     */
    public void updateEncounterStage(UUID entityUuid, EncounterStage stage) {
        // Get the target player for this entity
        UUID targetPlayerUuid = getTargetPlayer(entityUuid);
        if (targetPlayerUuid == null) {
            return;
        }

        // Update audio based on stage
        audioDirector.updateEncounterAudio(targetPlayerUuid, stage);

        // Create evidence if we're entering the EVIDENCE stage
        if (stage == EncounterStage.EVIDENCE) {
            Player targetPlayer = Bukkit.getPlayer(targetPlayerUuid);
            if (targetPlayer != null) {
                // We need the encounter ID - get it from entity data
                EntityRegistry.EntityData data = entityRegistry.getEntityData(entityUuid);
                if (data != null) {
                    createEncounterEvidence(targetPlayer, data.getEncounterId());
                }
            }
        }
    }

    /**
     * Creates evidence of Herobrine's presence as part of an encounter
     * @param targetPlayer The player being haunted
     * @param encounterId Unique ID for this encounter
     */
    public void createEncounterEvidence(Player targetPlayer, String encounterId) {
        // Only create evidence if world modifications are enabled
        if (!plugin.getConfig().getBoolean("world.modify-blocks", false)) {
            return;
        }

        anomalyManager.createEvidenceAnomaly(targetPlayer, encounterId);
    }

    /**
     * Checks if an entity is a tracked Herobrine entity
     * @param entityUuid The UUID to check
     * @return true if it's a Herobrine entity we're tracking
     */
    public boolean isTrackedHerobrine(UUID entityUuid) {
        return entityRegistry.isHerobrineEntity(entityUuid);
    }

    /**
     * Gets the target player for a Herobrine entity
     * @param entityUuid The UUID of the Herobrine entity
     * @return The target player's UUID or null if not found
     */
    public UUID getTargetPlayer(UUID entityUuid) {
        EntityRegistry.EntityData data = entityRegistry.getEntityData(entityUuid);
        return data != null ? data.getTargetPlayerUuid() : null;
    }

    /**
     * Gets how long until an entity expires (in milliseconds)
     * @param entityUuid The UUID of the entity
     * @return Time until expiration or -1 if not found/expired
     */
    public long getTimeUntilExpiration(UUID entityUuid) {
        EntityRegistry.EntityData data = entityRegistry.getEntityData(entityUuid);
        if (data == null) {
            return -1;
        }
        long timeLeft = data.getExpirationTime() - System.currentTimeMillis();
        return Math.max(0, timeLeft);
    }

    /**
     * Gets all expired entities that should be despawned
     * @return Array of entity UUIDs that have expired
     */
    public UUID[] getExpiredEntities() {
        return entityRegistry.getExpiredEntities(System.currentTimeMillis());
    }
}