package com.kingplayz.herobrine.entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Entity;

/**
 * Registry for tracking Herobrine entities and their associated data.
 * Prevents memory leaks by ensuring proper cleanup of entities.
 */
public class EntityRegistry {

    // Map of entity UUIDs to their Herobrine encounter data
    private final Map<UUID, EntityData> entityDataMap = new ConcurrentHashMap<>();

    /**
     * Data associated with a Herobrine entity
     */
    public static class EntityData {
        private final UUID targetPlayerUuid;
        private final String encounterId;
        private final long spawnTime;
        private long expirationTime;

        public EntityData(UUID targetPlayerUuid, String encounterId, long spawnTime, long expirationTime) {
            this.targetPlayerUuid = targetPlayerUuid;
            this.encounterId = encounterId;
            this.spawnTime = spawnTime;
            this.expirationTime = expirationTime;
        }

        public UUID getTargetPlayerUuid() {
            return targetPlayerUuid;
        }

        public String getEncounterId() {
            return encounterId;
        }

        public long getSpawnTime() {
            return spawnTime;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }

        public boolean isExpired(long currentTime) {
            return currentTime >= expirationTime;
        }
    }

    /**
     * Register a Herobrine entity with its associated data
     * @param entityUuid The UUID of the entity
     * @param targetPlayerUuid The UUID of the player being haunted
     * @param encounterId Unique ID for this encounter
     * @param spawnTime When the entity was spawned
     * @param expirationTime When the entity should despawn
     */
    public void registerEntity(UUID entityUuid, UUID targetPlayerUuid, String encounterId, long spawnTime, long expirationTime) {
        entityDataMap.put(entityUuid, new EntityData(targetPlayerUuid, encounterId, spawnTime, expirationTime));
    }

    /**
     * Get data for a registered entity
     * @param entityUuid The UUID of the entity
     * @return The entity data or null if not found
     */
    public EntityData getEntityData(UUID entityUuid) {
        return entityDataMap.get(entityUuid);
    }

    /**
     * Unregister an entity (called when it despawns)
     * @param entityUuid The UUID of the entity to unregister
     */
    public void unregisterEntity(UUID entityUuid) {
        entityDataMap.remove(entityUuid);
    }

    /**
     * Check if an entity is registered as a Herobrine entity
     * @param entityUuid The UUID to check
     * @return true if registered, false otherwise
     */
    public boolean isHerobrineEntity(UUID entityUuid) {
        return entityDataMap.containsKey(entityUuid);
    }

    /**
     * Get all expired entities based on current time
     * @param currentTime Current time in milliseconds
     * @return Array of UUIDs for expired entities
     */
    public UUID[] getExpiredEntities(long currentTime) {
        return entityDataMap.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(currentTime))
                .map(Map.Entry::getKey)
                .toArray(UUID[]::new);
    }

    /**
     * Get count of currently registered entities
     * @return Number of registered Herobrine entities
     */
    public int getEntityCount() {
        return entityDataMap.size();
    }

    /**
     * Get the key set of all registered entity UUIDs
     * @return Set of entity UUIDs
     */
    public java.util.Set<UUID> getEntityDataMap() {
        return entityDataMap.keySet();
    }

    /**
     * Clear all registered entities (used on plugin disable)
     */
    public void clear() {
        entityDataMap.clear();
    }
}