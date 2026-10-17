package com.kingplayz.herobrine.entity;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wrapper appearance provider that delegates to WhiteEyedAppearanceProvider.
 * <p>
 * This provider maintains the API contract for future Citizens NPC integration
 * while ensuring the plugin builds and runs successfully without the Citizens
 * dependency. When Citizens becomes available in the build environment,
 * this provider can be updated to return actual player-model NPCs (Steve-style
 * body) with white-eye customization through skins/textures.
 * <p>
 * Current behavior: Delegates entirely to WhiteEyedAppearanceProvider, which
 * creates and manages armor-stand Herobrine entities with maximum possible
 * customization within the Paper/Spigot API.
 */
public class CitizensAppearanceProvider implements AppearanceService {

    private final WhiteEyedAppearanceProvider fallback;

    public CitizensAppearanceProvider(JavaPlugin plugin) {
        this.fallback = new WhiteEyedAppearanceProvider(plugin);
    }

    @Override
    public Entity createHerobrineEntity() {
        // Delegates to WhiteEyedAppearanceProvider (armor-stand implementation)
        // To enable Citizens NPC player-model Herobrine in the future:
        // 1. Add Citizens Maven repository to build.gradle
        // 2. Add compileOnly "net.citizensnpcs:citizens:2.0.0-RC11" to dependencies
        // 3. Update this method to create and configure Citizens NPCs
        return fallback.createHerobrineEntity();
    }

    @Override
    public void applyHerobrineAppearance(Entity entity) {
        // Delegates to WhiteEyedAppearanceProvider
        fallback.applyHerobrineAppearance(entity);
    }

    @Override
    public boolean supportsWhiteEyes() {
        // Delegates to WhiteEyedAppearanceProvider
        // Currently returns false (armor stand API limitation)
        // When Citizens is integrated: will return true (custom skins/textures enable white eyes)
        return fallback.supportsWhiteEyes();
    }

    @Override
    public String getDisplayName() {
        // Delegates to WhiteEyedAppearanceProvider
        return fallback.getDisplayName();
    }
}