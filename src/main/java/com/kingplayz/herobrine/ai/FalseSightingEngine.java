package com.kingplayz.herobrine.ai;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

/**
 * False Sighting Engine that occasionally shows a distant silhouette
 * that is not the actual Herobrine entity. It disappears before the
 * player gets a clear view. Each player has a cooldown to avoid
 * overusing the illusion.
 */
public class FalseSightingEngine {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> falseSightingCooldowns = new HashMap<>();

    // Configuration defaults
    private double falseSightingChance = 0.05; // 5% chance per evaluation
    private int falseSightingCooldownSeconds = 300; // 5 minutes
    private int falseSightingDistanceMin = 64; // Minimum blocks away
    private int falseSightingDistanceMax = 128; // Maximum blocks away
    private int falseSightingDurationTicks = 40; // 2 seconds visible

    public FalseSightingEngine(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Determines if a false sighting should be generated for a player.
     * @param player The player to check
     * @return Location for false sighting or null if none
     */
    public Location maybeGenerateFalseSighting(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is on cooldown for false sightings
        Long lastSighting = falseSightingCooldowns.get(playerUuid);
        long now = System.currentTimeMillis() / 1000; // Convert to seconds

        if (lastSighting != null && (now - lastSighting) < falseSightingCooldownSeconds) {
            return null; // Still on cooldown
        }

        // Check if we should generate a false sighting based on chance
        if (random.nextDouble() > falseSightingChance) {
            return null;
        }

        // Generate the false sighting location
        Location falseSightingLoc = generateFalseSightingLocation(player);

        // Set cooldown for this player
        falseSightingCooldowns.put(playerUuid, now);

        // Schedule the disappearance of the false sighting
        scheduleFalseSightingDisappearance(falseSightingLoc);

        plugin.getLogger().info("Generated false sighting for player " + player.getName() +
                " at " + falseSightingLoc.getBlockX() + "," + falseSightingLoc.getBlockY() + "," + falseSightingLoc.getBlockZ());

        return falseSightingLoc;
    }

    /**
     * Generates a location for a false sighting.
     * @param player The player for whom to generate the sighting
     * @return Location where the false sighting should appear
     */
    private Location generateFalseSightingLocation(Player player) {
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();

        if (world == null) {
            return playerLoc.clone().add(0, 10, 0); // Fallback
        }

        // Generate random distance and angle
        double distance = falseSightingDistanceMin +
                random.nextDouble() * (falseSightingDistanceMax - falseSightingDistanceMin);
        double angle = random.nextDouble() * 2 * Math.PI;

        // Calculate location
        double x = playerLoc.getX() + distance * Math.cos(angle);
        double z = playerLoc.getZ() + distance * Math.sin(angle);
        double y = playerLoc.getY() + 2; // Slightly above player eye level

        Location sightingLoc = new Location(world, x, y, z);

        // Ensure the location is valid (not in solid ground, etc.)
        return ensureValidLocation(sightingLoc);
    }

    /**
     * Ensures the location is valid for an entity to appear.
     * @param location The location to check
     * @return Valid location
     */
    private Location ensureValidLocation(Location location) {
        // Simple validation - in practice would check for solid blocks, etc.
        // For now, just return the location
        return location;
    }

    /**
     * Schedules the disappearance of a false sighting.
     * In a full implementation, this would actually remove a temporary entity.
     * @param location Where the false sighting was generated
     */
    private void scheduleFalseSightingDisappearance(Location location) {
        // In a full implementation, we'd:
        // 1. Spawn a temporary entity (like an invisible armor stand with a nametag)
        // 2. Schedule its removal after falseSightingDurationTicks
        // 3. Make it visible only briefly

        // For now, we'll just log that it should disappear
        long delayTicks = falseSightingDurationTicks;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getLogger().info("False sighting at " + location.getBlockX() + "," +
                    location.getBlockY() + "," + location.getBlockZ() + " has disappeared");
        }, delayTicks);
    }

    /**
     * Gets the remaining cooldown time for false sightings for a player.
     * @param player The player to check
     * @return Cooldown time in seconds, or 0 if not on cooldown
     */
    public int getFalseSightingCooldownRemaining(Player player) {
        UUID playerUuid = player.getUniqueId();
        Long lastSighting = falseSightingCooldowns.get(playerUuid);
        if (lastSighting == null) {
            return 0;
        }

        long now = System.currentTimeMillis() / 1000;
        long elapsed = now - lastSighting;
        long remaining = falseSightingCooldownSeconds - elapsed;

        return Math.max(0, (int) remaining);
    }

    /**
     * Configuration getters and setters
     */
    public double getFalseSightingChance() {
        return falseSightingChance;
    }

    public void setFalseSightingChance(double falseSightingChance) {
        this.falseSightingChance = falseSightingChance;
    }

    public int getFalseSightingCooldownSeconds() {
        return falseSightingCooldownSeconds;
    }

    public void setFalseSightingCooldownSeconds(int falseSightingCooldownSeconds) {
        this.falseSightingCooldownSeconds = falseSightingCooldownSeconds;
    }

    public int getFalseSightingDistanceMin() {
        return falseSightingDistanceMin;
    }

    public void setFalseSightingDistanceMin(int falseSightingDistanceMin) {
        this.falseSightingDistanceMin = falseSightingDistanceMin;
    }

    public int getFalseSightingDistanceMax() {
        return falseSightingDistanceMax;
    }

    public void setFalseSightingDistanceMax(int falseSightingDistanceMax) {
        this.falseSightingDistanceMax = falseSightingDistanceMax;
    }

    public int getFalseSightingDurationTicks() {
        return falseSightingDurationTicks;
    }

    public void setFalseSightingDurationTicks(int falseSightingDurationTicks) {
        this.falseSightingDurationTicks = falseSightingDurationTicks;
    }
}