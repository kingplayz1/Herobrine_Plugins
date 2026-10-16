package com.kingplayz.herobrine.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import com.kingplayz.herobrine.HerobrinePlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Service for protecting world modifications and preventing griefing.
 */
public class ProtectionService {

    private final HerobrinePlugin plugin;
    private final Set<String> protectedWorlds = new HashSet<>();
    private final Set<UUID> trustedPlayers = new HashSet<>();
    private final int spawnProtectionRadius;

    public ProtectionService(HerobrinePlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.spawnProtectionRadius = config.getInt("protection.spawn-protection-radius", 50);

        // Load protected worlds from config
        if (config.isList("protection.protected-worlds")) {
            protectedWorlds.addAll(config.getStringList("protection.protected-worlds"));
        }

        // Load trusted players from config
        if (config.isList("protection.trusted-players")) {
            for (String playerName : config.getStringList("protection.trusted-players")) {
                // In a real implementation, we would resolve names to UUIDs
                // For now, we'll just add them as names and handle resolution elsewhere
            }
        }
    }

    /**
     * Checks if a block at the given location can be modified.
     * If player is null, checks only if the location is in a protected area.
     * If player is not null, checks if that specific player can modify the location.
     * @param player the player attempting the modification (can be null for general check)
     * @param location the location to check
     * @return true if the block can be modified
     */
    public boolean canModify(Player player, Location location) {
        // Check if world is protected
        String worldName = location.getWorld().getName();
        if (protectedWorlds.contains(worldName)) {
            if (player == null) {
                // If no player specified, just return false for protected worlds (can't modify)
                return false;
            }
            return trustedPlayers.contains(player.getUniqueId());
        }

        // Check spawn protection
        if (location.getWorld().getName().equalsIgnoreCase("world")) { // Default world
            Location spawn = location.getWorld().getSpawnLocation();
            double distance = location.distance(spawn);
            if (distance <= spawnProtectionRadius) {
                if (player == null) {
                    // If no player specified, just return false for spawn protection (can't modify)
                    return false;
                }
                return trustedPlayers.contains(player.getUniqueId()) || player.isOp();
            }
        }

        // Check if player is trusted (only if player is specified)
        if (player == null) {
            // If no player specified and not in protected area, assume can modify
            return true;
        }
        return trustedPlayers.contains(player.getUniqueId()) || player.isOp();
    }

    /**
     * Adds a world to the protected worlds list.
     * @param worldName the name of the world to protect
     */
    public void addProtectedWorld(String worldName) {
        protectedWorlds.add(worldName);
        // Save to config would happen here
    }

    /**
     * Removes a world from the protected worlds list.
     * @param worldName the name of the world to unprotect
     */
    public void removeProtectedWorld(String worldName) {
        protectedWorlds.remove(worldName);
        // Save to config would happen here
    }

    /**
     * Adds a player to the trusted players list.
     * @param playerId the UUID of the player to trust
     */
    public void addTrustedPlayer(UUID playerId) {
        trustedPlayers.add(playerId);
        // Save to config would happen here
    }

    /**
     * Removes a player from the trusted players list.
     * @param playerId the UUID of the player to untrust
     */
    public void removeTrustedPlayer(UUID playerId) {
        trustedPlayers.remove(playerId);
        // Save to config would happen here
    }

    /**
     * Checks if a world is protected.
     * @param worldName the name of the world to check
     * @return true if the world is protected
     */
    public boolean isWorldProtected(String worldName) {
        return protectedWorlds.contains(worldName);
    }

    /**
     * Checks if a player is trusted.
     * @param playerId the UUID of the player to check
     * @return true if the player is trusted
     */
    public boolean isPlayerTrusted(UUID playerId) {
        return trustedPlayers.contains(playerId);
    }
}