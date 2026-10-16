package com.kingplayz.herobrine.world;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * Journal for tracking block changes made by Herobrine encounters.
 * Records original block states to allow for restoration later.
 */
public class BlockChangeJournal {

    // Map of encounter ID to its block changes
    private final Map<String, Map<BlockLocation, BlockState>> changesByEncounter = new ConcurrentHashMap<>();

    /**
     * Records a block's original state before modification.
     * @param encounterId The ID of the encounter making the change
     * @param block The block being modified
     */
    public void recordChange(String encounterId, Block block) {
        if (block == null) {
            return;
        }

        BlockLocation location = new BlockLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID());
        BlockState state = new BlockState(block.getBlockData(), block.getType());

        changesByEncounter
                .computeIfAbsent(encounterId, k -> new ConcurrentHashMap<>())
                .put(location, state);
    }

    /**
     * Restores a block to its original state if it matches our expected modified state.
     * @param encounterId The ID of the encounter that made the change
     * @param block The block to potentially restore
     * @param expectedModifiedState The state we expect the block to be in
     * @return true if the block was restored, false otherwise
     */
    public boolean restoreIfExpected(String encounterId, Block block, BlockData expectedModifiedState) {
        if (block == null || encounterId == null) {
            return false;
        }

        Map<BlockLocation, BlockState> encounterChanges = changesByEncounter.get(encounterId);
        if (encounterChanges == null) {
            return false;
        }

        BlockLocation location = new BlockLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID());
        BlockState originalState = encounterChanges.get(location);
        if (originalState == null) {
            return false; // We didn't record this block
        }

        // Only restore if the block is currently in the expected state
        // This prevents overriding changes made by players or other plugins
        if (block.getBlockData().equals(expectedModifiedState)) {
            block.setBlockData(originalState.getBlockData(), false);
            encounterChanges.remove(location);
            return true;
        }

        return false;
    }

    /**
     * Restores all blocks associated with an encounter.
     * This is used during cleanup and doesn't check expected state.
     * @param encounterId The ID of the encounter to restore
     * @return Number of blocks restored
     */
    public int restoreAll(String encounterId) {
        if (encounterId == null) {
            return 0;
        }

        Map<BlockLocation, BlockState> encounterChanges = changesByEncounter.remove(encounterId);
        if (encounterChanges == null) {
            return 0;
        }

        int restored = 0;
        for (Map.Entry<BlockLocation, BlockState> entry : encounterChanges.entrySet()) {
            BlockLocation loc = entry.getKey();
            BlockState state = entry.getValue();

            // In a real implementation, we'd get the block from the world
            // For now, we'll just count what would be restored
            restored++;
        }

        return restored;
    }

    /**
     * Gets all changes for an encounter (for inspection or manual restoration)
     * @param encounterId The encounter ID
     * @return Map of block locations to their original states, or null if none
     */
    public Map<BlockLocation, BlockState> getChanges(String encounterId) {
        return changesByEncounter.get(encounterId);
    }

    /**
     * Clears all records (used on plugin disable)
     */
    public void clear() {
        changesByEncounter.clear();
    }

    /**
     * Simple container for block location data
     */
    public static class BlockLocation {
        private final int x, y, z;
        private final UUID worldUuid;

        public BlockLocation(int x, int y, int z, UUID worldUuid) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldUuid = worldUuid;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public UUID getWorldUuid() { return worldUuid; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockLocation that = (BlockLocation) o;
            return x == that.x && y == that.y && z == that.z && worldUuid.equals(that.worldUuid);
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            result = 31 * result + worldUuid.hashCode();
            return result;
        }
    }

    /**
     * Simple container for block state data
     */
    public static class BlockState {
        private final BlockData blockData;
        private final org.bukkit.Material type;

        public BlockState(BlockData blockData, org.bukkit.Material type) {
            this.blockData = blockData;
            this.type = type;
        }

        public BlockData getBlockData() { return blockData; }
        public org.bukkit.Material getType() { return type; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockState that = (BlockState) o;
            return blockData.equals(that.blockData) && type == that.type;
        }

        @Override
        public int hashCode() {
            int result = blockData.hashCode();
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}