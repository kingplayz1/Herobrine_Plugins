package com.kingplayz.herobrine.entity;

import org.bukkit.entity.Entity;

/**
 * Service responsible for Herobrine's visual appearance.
 * Handles the white-eyed Steve model and visual customization.
 */
public interface AppearanceService {

    /**
     * Creates a Herobrine entity with the appropriate appearance.
     * @return Entity representing Herobrine
     */
    Entity createHerobrineEntity();

    /**
     * Applies Herobrine's visual properties to an entity.
     * @param entity The entity to modify
     */
    void applyHerobrineAppearance(Entity entity);

    /**
     * Checks if the current appearance setup supports white eyes.
     * @return true if white eyes are supported, false otherwise
     */
    boolean supportsWhiteEyes();

    /**
     * Gets the display name for Herobrine (should be null/empty for nameless appearance).
     * @return Display name or null
     */
    String getDisplayName();
}