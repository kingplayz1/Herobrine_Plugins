package com.kingplayz.herobrine.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

/**
 * Manages world anomalies created by Herobrine encounters.
 * Creates subtle environmental changes like missing leaves, simple structures, etc.
 */
public class AnomalyManager {

    private final JavaPlugin plugin;
    private final ProtectionService protectionService;
    private final BlockChangeJournal blockChangeJournal;
    private final Random random = new Random();

    public AnomalyManager(JavaPlugin plugin, ProtectionService protectionService, BlockChangeJournal blockChangeJournal) {
        this.plugin = plugin;
        this.protectionService = protectionService;
        this.blockChangeJournal = blockChangeJournal;
    }

    /**
     * Creates a small anomaly near the player as evidence of Herobrine's presence.
     * @param player The player being haunted
     * @param encounterId Unique ID for this encounter
     */
    public void createEvidenceAnomaly(Player player, String encounterId) {
        if (!plugin.getConfig().getBoolean("world.modify-blocks", false)) {
            return; // World modifications disabled
        }

        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();

        // Try to create a few different types of anomalies
        int anomalyType = random.nextInt(4);

        switch (anomalyType) {
            case 0:
                createMissingLeavesAnomaly(playerLoc, encounterId);
                break;
            case 1:
                createTinyStructureAnomaly(playerLoc, encounterId);
                break;
            case 2:
                createSuspiciousTunnelAnomaly(playerLoc, encounterId);
                break;
            case 3:
                createMissingTorchAnomaly(playerLoc, encounterId);
                break;
        }
    }

    /**
     * Creates an anomaly where leaves are missing from trees.
     * @param center The center location to search around
     * @param encounterId Unique ID for this encounter
     */
    private void createMissingLeavesAnomaly(Location center, String encounterId) {
        // Search for a tree with leaves nearby
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    Location checkLoc = center.clone().add(x, y, z);
                    Block block = checkLoc.getBlock();

                    // Check if this is a log (tree trunk)
                    if (isLog(block.getType())) {
                        // Check leaves above and around the log
                        for (int dy = 1; dy <= 4; dy++) {
                            Location leafLoc = checkLoc.clone().add(0, dy, 0);
                            Block leafBlock = leafLoc.getBlock();
                            if (isLeaves(leafBlock.getType()) &&
                                    protectionService.canModify(null, leafLoc)) {

                                // Record and remove leaves
                                blockChangeJournal.recordChange(encounterId, leafBlock);
                                leafBlock.setType(Material.AIR);
                                return; // Only remove one set of leaves per anomaly
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a tiny suspicious structure (like a small pyramid).
     * @param center The center location to build near
     * @param encounterId Unique ID for this encounter
     */
    private void createTinyStructureAnomaly(Location center, String encounterId) {
        // Build a small 3x3x1 platform or pyramid of stone/sand
        Material buildMaterial = random.nextBoolean() ? Material.STONE : Material.SAND;
        int size = 3;
        int startX = center.getBlockX() - size/2;
        int startZ = center.getBlockZ() - size/2;
        int y = center.getBlockY() - 1; // Build at feet level

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Location loc = new Location(center.getWorld(), startX + x, y, startZ + z);
                Block block = loc.getBlock();

                // Only replace grass or dirt, and only if we can modify it
                if (isReplaceableGround(block.getType()) &&
                        protectionService.canModify(null, loc)) {

                    blockChangeJournal.recordChange(encounterId, block);
                    block.setType(buildMaterial);
                }
            }
        }
    }

    /**
     * Creates a suspicious 1x1 horizontal tunnel.
     * @param center The center location to start the tunnel
     * @param encounterId Unique ID for this encounter
     */
    private void createSuspiciousTunnelAnomaly(Location center, String encounterId) {
        // Create a short horizontal tunnel
        int length = 4 + random.nextInt(3); // 4-6 blocks long
        int startX = center.getBlockX() - length/2;
        int y = center.getBlockY();
        int z = center.getBlockZ();

        for (int x = 0; x < length; x++) {
            Location loc = new Location(center.getWorld(), startX + x, y, z);
            Block block = loc.getBlock();

            // Only replace replaceable ground
            if (isReplaceableGround(block.getType()) &&
                    protectionService.canModify(null, loc)) {

                blockChangeJournal.recordChange(encounterId, block);
                block.setType(Material.STONE); // Make it look deliberately placed
            }
        }
    }

    /**
     * Creates an anomaly where a torch is missing.
     * @param center The center location to search around
     * @param encounterId Unique ID for this encounter
     */
    private void createMissingTorchAnomaly(Location center, String encounterId) {
        // Search for a placed torch nearby
        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    Location checkLoc = center.clone().add(x, y, z);
                    Block block = checkLoc.getBlock();

                    // Check if this is a wall torch or standing torch
                    if (isTorch(block.getType()) &&
                            protectionService.canModify(null, checkLoc)) {

                        // Record and remove the torch
                        blockChangeJournal.recordChange(encounterId, block);
                        block.setType(Material.AIR);
                        return; // Only remove one torch per anomaly
                    }
                }
            }
        }
    }

    // Helper methods to check block types
    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_WOOD");
    }

    private boolean isLeaves(Material material) {
        return material.name().endsWith("_LEAVES");
    }

    private boolean isTorch(Material material) {
        return material == Material.TORCH || material == Material.WALL_TORCH ||
                material == Material.SOUL_TORCH || material == Material.SOUL_WALL_TORCH;
    }

    private boolean isReplaceableGround(Material material) {
        return material == Material.GRASS_BLOCK || material == Material.DIRT ||
                material == Material.PODZOL || material == Material.MYCELIUM ||
                material == Material.SAND || material == Material.RED_SAND ||
                material == Material.GRAVEL;
    }
}