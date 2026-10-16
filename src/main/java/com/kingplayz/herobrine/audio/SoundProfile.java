package com.kingplayz.herobrine.audio;

import org.bukkit.Sound;

/**
 * Represents a sound profile with a specific sound and volume level.
 */
public class SoundProfile {

    private final Sound sound;
    private final float volume;

    public SoundProfile(Sound sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    /**
     * Creates a sound profile for observation (distant presence).
     * @return SoundProfile for observation
     */
    public static SoundProfile observationProfile() {
        // Using ambient cave sounds for observation
        return new SoundProfile(Sound.AMBIENT_CAVE, 0.1f);
    }

    /**
     * Creates a sound profile for evidence (clue discovered).
     * @return SoundProfile for evidence
     */
    public static SoundProfile evidenceProfile() {
        // Using note block pling for evidence
        return new SoundProfile(Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f);
    }

    /**
     * Creates a sound profile for manifestation (Herobrine appears).
     * @return SoundProfile for manifestation
     */
    public static SoundProfile manifestationProfile() {
        // Using wither ambient sound for manifestation (more likely to exist)
        return new SoundProfile(Sound.ENTITY_WITHER_AMBIENT, 0.8f);
    }

    /**
     * Creates a sound profile for disappearance (Herobrine vanishes).
     * @return SoundProfile for disappearance
     */
    public static SoundProfile disappearanceProfile() {
        // Using wither shoot sound for disappearance (more likely to exist)
        return new SoundProfile(Sound.ENTITY_WITHER_SHOOT, 0.6f);
    }
}