package com.kingplayz.herobrine.ai;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.kingplayz.herobrine.persistence.DatabaseManager;
import com.kingplayz.herobrine.persistence.PlayerProfileRepository;
import com.kingplayz.herobrine.persistence.EncounterRepository;
import com.kingplayz.herobrine.entity.EntityRegistry;
import com.kingplayz.herobrine.entity.HerobrineEntityController;
import com.kingplayz.herobrine.ai.AdaptiveStalkingAI.PlayerBehaviorPattern;
import java.util.UUID;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

/**
 * Advanced AI features for Herobrine hauntings.
 * Implements player scoring, cooldown tracking, encounter history, and sophisticated encounter selection.
 * Also includes adaptive stalking AI that learns player behavior patterns.
 */
public class AdvancedAIManager {

    private final JavaPlugin plugin;
    private final DatabaseManager dbManager;
    private final PlayerProfileRepository playerRepo;
    private final EncounterRepository encounterRepo;
    private final EntityRegistry entityRegistry;
    private final HerobrineEntityController entityController;
    private final AdaptiveStalkingAI adaptiveStalkingAI;
    private final Random random = new Random();

    // Cache for player scores to avoid recalculating every tick
    private final Map<UUID, PlayerScore> playerScoreCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_UPDATE_INTERVAL = 5000; // 5 seconds

    public AdvancedAIManager(JavaPlugin plugin, DatabaseManager dbManager,
                             PlayerProfileRepository playerRepo,
                             EncounterRepository encounterRepo,
                             EntityRegistry entityRegistry,
                             HerobrineEntityController entityController,
                             AdaptiveStalkingAI adaptiveStalkingAI) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.playerRepo = playerRepo;
        this.encounterRepo = encounterRepo;
        this.entityRegistry = entityRegistry;
        this.entityController = entityController;
        this.adaptiveStalkingAI = adaptiveStalkingAI;
    }

    /**
     * Calculates a comprehensive eligibility score for a player.
     * This replaces the basic scoring in HauntingDirector with advanced features.
     * Also incorporates adaptive stalking AI insights.
     * @param player The player to score
     * @param currentEncounterStage The current encounter stage (if any)
     * @return A score from 0.0 to 1.0 representing eligibility
     */
    public double calculateAdvancedEligibilityScore(Player player, String currentEncounterStage) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is opted out
        if (playerRepo.isPlayerOptedOut(playerUuid)) {
            return 0.0;
        }

        // Update score cache if needed
        updatePlayerScoreCacheIfNeeded();

        // Get cached score or calculate if not in cache
        PlayerScore baseScore = playerScoreCache.get(playerUuid);
        if (baseScore == null) {
            baseScore = calculatePlayerScore(player);
            playerScoreCache.put(playerUuid, baseScore);
        }

        // Incorporate adaptive stalking AI insights
        double adaptiveModification = 1.0; // Default: no modification
        if (adaptiveStalkingAI != null) {
            // Get the recommended next action based on player behavior
            String recommendedAction = adaptiveStalkingAI.recommendNextAction(player, currentEncounterStage);

            // Modify the score based on adaptive insights
            switch (recommendedAction) {
                case "change_position":
                    // Slightly increase eligibility if Herobrine should change position
                    adaptiveModification *= 1.05;
                    break;
                case "leave_evidence":
                    // Increase eligibility if Herobrine should leave evidence instead of forcing encounter
                    adaptiveModification *= 1.03;
                    break;
                case "standard":
                default:
                    // No modification for standard behavior
                    adaptiveModification *= 1.0;
                    break;
            }
        }

        // Apply randomness to prevent predictability
        double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
        double finalScore = Math.min(1.0, baseScore.getTotalScore() * randomFactor * adaptiveModification);

        return finalScore;
    }

    /**
     * Calculates a detailed player score based on multiple factors.
     * @param player The player to score
     * @return The player score object
     */
    private PlayerScore calculatePlayerScore(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Factor 1: Time since last encounter (normalized)
        double timeScore = calculateTimeSinceLastEncounterScore(playerUuid);

        // Factor 2: Isolation score (fewer nearby players = higher score)
        double isolationScore = calculateIsolationScore(player);

        // Factor 3: Recent activity score (more activity = higher score)
        double activityScore = calculateRecentActivityScore(playerUuid);

        // Factor 4: World suitability score (time, weather, etc.)
        double worldScore = calculateWorldSuitabilityScore(player.getWorld());

        // Factor 5: Haunting level score (higher level = higher score, but with diminishing returns)
        double hauntingLevelScore = calculateHauntingLevelScore(playerUuid);

        // Factor 6: Encounter history diversity (variety of encounter types = higher score)
        double diversityScore = calculateEncounterDiversityScore(playerUuid);

        // Weighted sum (adjusted weights based on importance)
        double totalScore =
            (0.20 * timeScore) +
            (0.15 * isolationScore) +
            (0.15 * activityScore) +
            (0.15 * worldScore) +
            (0.20 * hauntingLevelScore) +
            (0.15 * diversityScore);

        return new PlayerScore(timeScore, isolationScore, activityScore, worldScore,
                              hauntingLevelScore, diversityScore, totalScore);
    }

    /**
     * Calculates time since last encounter score.
     * Longer time = higher score, with diminishing returns after a certain point.
     * @param playerUuid The player's UUID
     * @return Score from 0.0 to 1.0
     */
    private double calculateTimeSinceLastEncounterScore(UUID playerUuid) {
        long timeSinceLastEncounter = playerRepo.getTimeSinceLastEncounter(playerUuid);

        // If never encountered, return maximum score
        if (timeSinceLastEncounter == Long.MAX_VALUE) {
            return 1.0;
        }

        // Convert to minutes for easier calculation
        double minutesSinceLastEncounter = timeSinceLastEncounter / 60000.0;

        // Score increases with time but plateaus after 60 minutes
        // Using a logarithmic curve for diminishing returns
        if (minutesSinceLastEncounter <= 0) {
            return 0.0;
        }

        double score = Math.min(1.0, Math.log(minutesSinceLastEncounter + 1) / Math.log(61));
        return score;
    }

    /**
     * Calculates isolation score based on nearby players.
     * Fewer nearby players = higher score.
     * @param player The player to check
     * @return Score from 0.0 to 1.0
     */
    private double calculateIsolationScore(Player player) {
        int nearbyPlayers = 0;
        Location playerLoc = player.getLocation();
        double checkRadius = 50.0; // Check within 50 blocks

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId()) &&
                other.getWorld().equals(player.getWorld()) &&
                playerLoc.distance(other.getLocation()) < checkRadius) {
                nearbyPlayers++;
            }
        }

        // More isolated = higher score
        // If no other players nearby, score = 1.0
        // If many players nearby, score approaches 0.0
        // Using exponential decay for smoother transition
        return Math.exp(-nearbyPlayers / 10.0);
    }

    /**
     * Calculates recent activity score based on player actions.
     * More recent activity = higher score (haunting prefers active players).
     * @param playerUuid The player's UUID
     * @return Score from 0.0 to 1.0
     */
    private double calculateRecentActivityScore(UUID playerUuid) {
        // In a full implementation, this would check recent block breaks/placements, chat, movement, etc.
        // For now, we'll use a simplified approach based on time since last join or a random factor

        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            return 0.0;
        }

        // Base score on how long they've been online (longer = more likely to be active)
        long timeOnline = System.currentTimeMillis() - player.getFirstPlayed();
        double hoursOnline = timeOnline / 3600000.0;

        // Cap at 10 hours for scoring purposes
        double cappedHours = Math.min(hoursOnline, 10.0);
        double score = cappedHours / 10.0;

        // Add some randomness to prevent predictability
        double randomFactor = 0.7 + (random.nextDouble() * 0.6); // 0.7 to 1.3
        return Math.min(1.0, score * randomFactor);
    }

    /**
     * Calculates world suitability score based on time, weather, etc.
     * @param world The world to check
     * @return Score from 0.0 to 1.0
     */
    private double calculateWorldSuitabilityScore(World world) {
        double score = 0.5; // Base score

        // Time of day factor (haunting more likely at night)
        long time = world.getTime();
        boolean isNight = time < 12000 || time > 23000; // Minecraft night time
        if (isNight) {
            score += 0.3; // Up to 0.8 total
        }

        // Weather factor (haunting more likely during storms)
        if (world.hasStorm()) {
            score += 0.2; // Up to 1.0 total
        }

        // Ensure score stays within bounds
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Calculates haunting level score.
     * Higher haunting level = higher score, but with diminishing returns.
     * @param playerUuid The player's UUID
     * @return Score from 0.0 to 1.0
     */
    private double calculateHauntingLevelScore(UUID playerUuid) {
        int hauntingLevel = playerRepo.getHauntingLevel(playerUuid);

        // Logarithmic scaling to prevent high-level players from being too overwhelming
        // Level 0: 0.0, Level 1: ~0.3, Level 10: ~0.6, Level 100: ~0.8
        if (hauntingLevel <= 0) {
            return 0.0;
        }

        double score = Math.min(0.9, Math.log(hauntingLevel + 1) / Math.log(101) * 0.9);
        return score;
    }

    /**
     * Calculates encounter diversity score.
     * More variety in past encounter types = higher score.
     * @param playerUuid The player's UUID
     * @return Score from 0.0 to 1.0
     */
    private double calculateEncounterDiversityScore(UUID playerUuid) {
        // Get recent encounter history
        var encounters = encounterRepo.getEncounterHistory(playerUuid, 10); // Last 10 encounters

        if (encounters.isEmpty()) {
            return 0.5; // Neutral score for no history
        }

        // Count unique encounter outcomes
        var outcomes = new HashMap<String, Integer>();
        for (var encounter : encounters) {
            String outcome = encounter.getOutcome();
            if (outcome == null || outcome.isEmpty()) {
                outcome = "unknown";
            }
            outcomes.put(outcome, outcomes.getOrDefault(outcome, 0) + 1);
        }

        // Calculate diversity as proportion of unique outcomes
        int uniqueOutcomes = outcomes.size();
        int totalEncounters = encounters.size();

        if (totalEncounters == 0) {
            return 0.5;
        }

        // More unique outcomes = higher score
        // Maximum diversity would be each encounter having a different outcome
        double diversityRatio = (double) uniqueOutcomes / totalEncounters;

        // Apply bonus for having variety, but cap the benefit
        return 0.3 + (diversityRatio * 0.7); // Range: 0.3 to 1.0
    }

    /**
     * Updates the player score cache if it's stale.
     */
    private void updatePlayerScoreCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_UPDATE_INTERVAL) {
            playerScoreCache.clear();
            lastCacheUpdate = currentTime;
        }
    }

    /**
     * Records that a player had an encounter and updates their haunting level.
     * @param playerUuid The player's UUID
     * @param encounterType The type of encounter that occurred
     */
    public void recordPlayerEncounter(UUID playerUuid, String encounterType) {
        // Record the encounter in player profile
        playerRepo.recordPlayerEncounter(playerUuid);

        // Increase haunting level based on encounter type
        int levelIncrease = calculateLevelIncrease(encounterType);
        playerRepo.increaseHauntingLevel(playerUuid, levelIncrease);

        // Clear score cache for this player to force recalculation
        playerScoreCache.remove(playerUuid);
    }

    /**
     * Calculates how much to increase haunting level based on encounter type.
     * @param encounterType The type of encounter
     * @return The amount to increase haunting level by
     */
    private int calculateLevelIncrease(String encounterType) {
        return switch (encounterType.toLowerCase()) {
            case "observation" -> 1;
            case "evidence" -> 2;
            case "manifestation" -> 3;
            case "stalking" -> 1;
            case "pursuing" -> 2;
            default -> 1;
        };
    }

    /**
     * Gets the cooldown time for a player based on their haunting level.
     * Higher haunting level = shorter cooldown (more frequent hauntings).
     * @param playerUuid The player's UUID
     * @return Cooldown time in milliseconds
     */
    public long getPlayerCooldown(UUID playerUuid) {
        int baseCooldownMinutes = 20; // Base cooldown
        int maxCooldownMinutes = 45;  // Maximum cooldown

        // Get haunting level
        int hauntingLevel = playerRepo.getHauntingLevel(playerUuid);

        // Calculate cooldown reduction based on haunting level
        // Level 0: max cooldown (45 min)
        // Level 20: base cooldown (20 min)
        // Level 40+: minimum cooldown (10 min)
        int cooldownReduction = Math.min(hauntingLevel * 1, 25); // Max 25 minutes reduction

        int cooldownMinutes = Math.max(10, maxCooldownMinutes - cooldownReduction);
        return cooldownMinutes * 60 * 1000L; // Convert to milliseconds
    }

    /**
     * Checks if a player is currently on cooldown.
     * @param playerUuid The player's UUID
     * @return True if on cooldown, false otherwise
     */
    public boolean isPlayerOnCooldown(UUID playerUuid) {
        long timeSinceLastEncounter = playerRepo.getTimeSinceLastEncounter(playerUuid);
        long cooldownMillis = getPlayerCooldown(playerUuid);

        return timeSinceLastEncounter < cooldownMillis;
    }

    /**
     * Data class to hold a player's detailed score breakdown.
     */
    public static class PlayerScore {
        private final double timeScore;
        private final double isolationScore;
        private final double activityScore;
        private final double worldScore;
        private final double hauntingLevelScore;
        private final double diversityScore;
        private final double totalScore;

        public PlayerScore(double timeScore, double isolationScore, double activityScore,
                          double worldScore, double hauntingLevelScore, double diversityScore,
                          double totalScore) {
            this.timeScore = timeScore;
            this.isolationScore = isolationScore;
            this.activityScore = activityScore;
            this.worldScore = worldScore;
            this.hauntingLevelScore = hauntingLevelScore;
            this.diversityScore = diversityScore;
            this.totalScore = totalScore;
        }

        public double getTimeScore() {
            return timeScore;
        }

        public double getIsolationScore() {
            return isolationScore;
        }

        public double getActivityScore() {
            return activityScore;
        }

        public double getWorldScore() {
            return worldScore;
        }

        public double getHauntingLevelScore() {
            return hauntingLevelScore;
        }

        public double getDiversityScore() {
            return diversityScore;
        }

        public double getTotalScore() {
            return totalScore;
        }
    }
}