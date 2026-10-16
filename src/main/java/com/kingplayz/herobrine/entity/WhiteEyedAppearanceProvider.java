package com.kingplayz.herobrine.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

/**
 * Appearance provider that attempts to create a Herobrine entity with white eyes.
 * Uses a player entity approach where possible, falling back to armor stand.
 * True white eyes require client-side resource packs or specific version support.
 */
public class WhiteEyedAppearanceProvider implements AppearanceService {

    private final JavaPlugin plugin;
    private final boolean useEntityAPI;

    public WhiteEyedAppearanceProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        // Check if we can use the entity API for custom player entities
        this.useEntityAPI = checkEntityAPISupport();
        plugin.getLogger().info("WhiteEyedAppearanceProvider initialized. Using Entity API: " + useEntityAPI);
    }

    /**
     * Checks if the server version supports creating custom player entities.
     * @return true if supported, false otherwise
     */
    private boolean checkEntityAPISupport() {
        // In a full implementation, this would check the server version
        // For now, we'll assume it's not supported and use fallback
        return false;
    }

    @Override
    public Entity createHerobrineEntity() {
        if (useEntityAPI) {
            return createCustomPlayerEntity();
        } else {
            return createFallbackEntity();
        }
    }

    /**
     * Attempts to create a custom player entity with white-eyed skin.
     * This would require ProtocolLib or version-specific NMS code in a full implementation.
     * @return Custom player entity or null if not supported
     */
    private Entity createCustomPlayerEntity() {
        // Placeholder for custom entity creation
        // In reality, this would involve:
        // 1. Using ProtocolLib to spawn a fake player
        // 2. Or using NMS to create a custom entity with white-eyed texture
        // 3. Or using a player-model library

        plugin.getLogger().warning("Custom player entity creation not implemented - using fallback");
        return createFallbackEntity();
    }

    /**
     * Creates a fallback entity using armor stand with custom name tag.
     * While this doesn't give true white eyes, it provides a visible marker.
     * Also attempts to disguise the armor stand to look more like Herobrine
     * by setting appropriate properties.
     * @return Armor stand entity
     */
    private Entity createFallbackEntity() {
        World world = plugin.getServer().getWorlds().get(0);
        if (world == null) {
            plugin.getLogger().severe("No world available to spawn Herobrine entity");
            return null;
        }

        // Create an armor stand as a visible marker for Herobrine
        Entity entity = world.spawnEntity(world.getSpawnLocation(), EntityType.ARMOR_STAND);

        if (entity != null) {
            // Configure the armor stand to be a visible marker
            entity.setCustomName("§r§lHerobrine"); // White bold text
            entity.setCustomNameVisible(true);
            entity.setGravity(false);

            // These methods are specific to ArmorStand
            if (entity.getType() == EntityType.ARMOR_STAND) {
                org.bukkit.entity.ArmorStand armorStand = (org.bukkit.entity.ArmorStand) entity;
                armorStand.setMarker(true);
                armorStand.setSmall(true);
                armorStand.setBasePlate(false);

                // Keep armor stand visible but customize it to look like Herobrine
                // - Small size for slender appearance
                // - No base plate
                // - Marker mode for stable positioning
                // - Invisible set to false so the model is visible
                armorStand.setInvisible(false);
                armorStand.setCollidable(true);

                // Custom name for identification (visible)
                armorStand.setCustomNameVisible(true);

                // Set small size to make it less like a regular mob armor stand
                // and more like a slender Herobrine figure
                // (Already set above via armorStand.setSmall(true))

                // The bold custom name "§r§lHerobrine" makes it identifiable
                // §r resets all formatting, §l makes text bold

                // Note: setVisible is not a standard Entity method in Bukkit
                // ArmorStands are always visible by default
                // The combination of custom name + slender appearance helps players recognize Herobrine
            }
        }

        return entity;
    }

    @Override
    public void applyHerobrineAppearance(Entity entity) {
        if (entity == null) {
            return;
        }

        // Apply Herobrine-specific properties
        entity.setCustomName("§r§lHerobrine"); // White bold text
        entity.setCustomNameVisible(true);
        entity.setGravity(false);

        // These methods are specific to ArmorStand
        if (entity.getType() == EntityType.ARMOR_STAND) {
            org.bukkit.entity.ArmorStand armorStand = (org.bukkit.entity.ArmorStand) entity;
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setBasePlate(false);

            // Keep armor stand visible but customize it to look like Herobrine
            // - Small size for slender appearance
            // - No base plate
            // - Marker mode for stable positioning
            // - Invisible set to false so the model is visible
            armorStand.setInvisible(false);
        }

        // Note: setVisible is not a standard Entity method in Bukkit
        // ArmorStands are always visible by default

        // Additional appearance customization would go here
        // For example, setting equipment, potion effects, etc.
    }

    @Override
    public boolean supportsWhiteEyes() {
        // In a full implementation with resource packs or specific versions, this would return true
        // For now, we indicate that true white eyes are not supported without client-side mods
        return false;
    }

    @Override
    public String getDisplayName() {
        // Herobrine should appear with a custom name but not as a regular player name
        return "§r§lHerobrine";
    }
}