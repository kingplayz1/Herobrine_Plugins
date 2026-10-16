package com.kingplayz.herobrine.audio;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages audio playback for Herobrine encounters.
 * Handles different sound profiles for different encounter stages.
 */
public class AudioDirector {

    private final JavaPlugin plugin;
    private final Map<UUID, EncounterSoundState> activeEncounters = new HashMap<>();

    public AudioDirector(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts audio for a new encounter.
     * @param playerUuid The UUID of the player being haunted
     * @param encounterId Unique ID for this encounter
     */
    public void startEncounterAudio(UUID playerUuid, String encounterId) {
        if (!plugin.getConfig().getBoolean("audio.enabled", true)) {
            return; // Audio disabled
        }

        activeEncounters.put(playerUuid, new EncounterSoundState(encounterId));
        plugin.getLogger().info("Started audio for encounter " + encounterId + " for player " + playerUuid);
    }

    /**
     * Updates audio for an encounter based on its current stage.
     * @param playerUuid The UUID of the player being haunted
     * @param stage The current encounter stage
     */
    public void updateEncounterAudio(UUID playerUuid, EncounterStage stage) {
        if (!plugin.getConfig().getBoolean("audio.enabled", true)) {
            return; // Audio disabled
        }

        EncounterSoundState state = activeEncounters.get(playerUuid);
        if (state == null) {
            return; // No active encounter for this player
        }

        // Play appropriate sound based on stage
        switch (stage) {
            case OBSERVING:
                playObservingSound(playerUuid, state);
                break;
            case EVIDENCE:
                playEvidenceSound(playerUuid, state);
                break;
            case MANIFESTING:
                playManifestingSound(playerUuid, state);
                break;
            case VANISHING:
                playVanishingSound(playerUuid, state);
                break;
            default:
                // No specific sound for other stages
                break;
        }
    }

    /**
     * Ends audio for an encounter (cleanup).
     * @param playerUuid The UUID of the player
     */
    public void endEncounterAudio(UUID playerUuid) {
        activeEncounters.remove(playerUuid);
    }

    /**
     * Plays the observation sound (distant presence).
     * @param playerUuid The player's UUID
     * @param state The encounter sound state
     */
    private void playObservingSound(UUID playerUuid, EncounterSoundState state) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null) {
            return;
        }

        // Play observation sound periodically
        if (state.shouldPlayObservingSound()) {
            SoundProfile observation = SoundProfile.observationProfile();
            float volume = observation.getVolume() *
                    plugin.getConfig().getFloat("audio.observation-volume", 0.25f);
            player.playSound(player.getLocation(), observation.getSound(), volume, 1.0f);
            state.updateObservingSoundTimer();
        }
    }

    /**
     * Plays the evidence sound (clue discovered).
     * @param playerUuid The player's UUID
     * @param state The encounter sound state
     */
    private void playEvidenceSound(UUID playerUuid, EncounterSoundState state) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null) {
            return;
        }

        // Play evidence sound once
        if (!state.hasPlayedEvidenceSound()) {
            SoundProfile evidence = SoundProfile.evidenceProfile();
            float volume = evidence.getVolume() *
                    plugin.getConfig().getFloat("audio.manifestation-volume", 0.65f); // Using manifestation volume for evidence as it's more noticeable
            player.playSound(player.getLocation(), evidence.getSound(), volume, 1.0f);
            state.setEvidenceSoundPlayed(true);
        }
    }

    /**
     * Plays the manifestation sound (Herobrine appears).
     * @param playerUuid The player's UUID
     * @param state The encounter sound state
     */
    private void playManifestingSound(UUID playerUuid, EncounterSoundState state) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null) {
            return;
        }

        // Play manifestation sound once
        if (!state.hasPlayedManifestingSound()) {
            SoundProfile manifestation = SoundProfile.manifestationProfile();
            float volume = manifestation.getVolume() *
                    plugin.getConfig().getFloat("audio.manifestation-volume", 0.65f);
            player.playSound(player.getLocation(), manifestation.getSound(), volume, 1.0f);
            state.setManifestingSoundPlayed(true);

            // Schedule silence after manifestation if enabled
            if (plugin.getConfig().getBoolean("audio.silence-after-manifestation", true)) {
                // In a full implementation, we'd schedule a task to suppress ambient sounds
                // For now, we'll just note that we want silence
                state.setSilenceAfterManifestation(true);
            }
        }
    }

    /**
     * Plays the disappearance sound (Herobrine vanishes).
     * @param playerUuid The player's UUID
     * @param state The encounter sound state
     */
    private void playVanishingSound(UUID playerUuid, EncounterSoundState state) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null) {
            return;
        }

        // Play disappearance sound once
        if (!state.hasPlayedVanishingSound()) {
            SoundProfile disappearance = SoundProfile.disappearanceProfile();
            float volume = disappearance.getVolume() *
                    plugin.getConfig().getFloat("audio.manifestation-volume", 0.65f);
            player.playSound(player.getLocation(), disappearance.getSound(), volume, 1.0f);
            state.setVanishingSoundPlayed(true);
        }
    }

    /**
     * Checks if a player has an active encounter.
     * @param playerUuid The player's UUID
     * @return true if active encounter exists
     */
    public boolean hasActiveEncounter(UUID playerUuid) {
        return activeEncounters.containsKey(playerUuid);
    }

    /**
     * Internal class to track sound state for an encounter.
     */
    private class EncounterSoundState {
        private final String encounterId;
        private int observingSoundTickCounter = 0;
        private boolean evidenceSoundPlayed = false;
        private boolean manifestingSoundPlayed = false;
        private boolean vanishingSoundPlayed = false;
        private boolean silenceAfterManifestation = false;

        public EncounterSoundState(String encounterId) {
            this.encounterId = encounterId;
        }

        public String getEncounterId() {
            return encounterId;
        }

        /**
         * Determines if we should play the observing sound based on timing.
         * @return true if we should play the observing sound
         */
        public boolean shouldPlayObservingSound() {
            // Play observing sound every 2 seconds (40 ticks) by default
            observingSoundTickCounter++;
            if (observingSoundTickCounter >= 40) {
                observingSoundTickCounter = 0;
                return true;
            }
            return false;
        }

        public void updateObservingSoundTimer() {
            // Reset counter after playing
            observingSoundTickCounter = 0;
        }

        public boolean hasPlayedEvidenceSound() {
            return evidenceSoundPlayed;
        }

        public void setEvidenceSoundPlayed(boolean evidenceSoundPlayed) {
            this.evidenceSoundPlayed = evidenceSoundPlayed;
        }

        public boolean hasPlayedManifestingSound() {
            return manifestingSoundPlayed;
        }

        public void setManifestingSoundPlayed(boolean manifestingSoundPlayed) {
            this.manifestingSoundPlayed = manifestingSoundPlayed;
        }

        public boolean hasPlayedVanishingSound() {
            return vanishingSoundPlayed;
        }

        public void setVanishingSoundPlayed(boolean vanishingSoundPlayed) {
            this.vanishingSoundPlayed = vanishingSoundPlayed;
        }

        public boolean isSilenceAfterManifestation() {
            return silenceAfterManifestation;
        }

        public void setSilenceAfterManifestation(boolean silenceAfterManifestation) {
            this.silenceAfterManifestation = silenceAfterManifestation;
        }
    }

    /**
     * Enum representing the stages of a Herobrine encounter.
     */
    public enum EncounterStage {
        DORMANT,
        ELIGIBILITY_CHECK,
        OBSERVING,
        EVIDENCE,
        MANIFESTING,
        STALKING,
        PURSUING,
        VANISHING,
        AFTERMATH,
        COOLDOWN
    }
}