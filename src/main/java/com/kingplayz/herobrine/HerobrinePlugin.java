package com.kingplayz.herobrine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.kingplayz.herobrine.ai.AdaptiveStalkingAI;
import com.kingplayz.herobrine.ai.AdvancedAIManager;
import com.kingplayz.herobrine.ai.EncounterStateManager;
import com.kingplayz.herobrine.ai.FalseSightingEngine;
import com.kingplayz.herobrine.ai.HauntingDirector;
import com.kingplayz.herobrine.command.HerobrineCommand;
import org.bukkit.Location;
import com.kingplayz.herobrine.config.ConfigManager;
import com.kingplayz.herobrine.entity.AppearanceService;
import com.kingplayz.herobrine.entity.DefaultAppearanceProvider;
import com.kingplayz.herobrine.entity.EntityRegistry;
import com.kingplayz.herobrine.entity.HerobrineEntityController;
import com.kingplayz.herobrine.entity.CitizensAppearanceProvider;
import com.kingplayz.herobrine.persistence.DatabaseManager;
import com.kingplayz.herobrine.persistence.EncounterRepository;
import com.kingplayz.herobrine.persistence.PlayerProfileRepository;
import com.kingplayz.herobrine.world.AnomalyManager;
import com.kingplayz.herobrine.world.BlockChangeJournal;
import com.kingplayz.herobrine.world.ProtectionService;
import com.kingplayz.herobrine.audio.AudioDirector;

public class HerobrinePlugin extends JavaPlugin {
    private ConfigManager configManager;
    private BukkitTask task;
    private AppearanceService appearanceService;
    private EntityRegistry entityRegistry;
    private HerobrineEntityController entityController;
    private ProtectionService protectionService;
    private BlockChangeJournal blockChangeJournal;
    private AnomalyManager anomalyManager;
    private AudioDirector audioDirector;
    private HauntingDirector hauntingDirector;
    private EncounterStateManager encounterStateManager;
    private FalseSightingEngine falseSightingEngine;

    @Override
    public void onEnable() {
        // Plugin startup logic
        configManager = new ConfigManager(this);

        // Use the Citizens-optional appearance provider
        appearanceService = new CitizensAppearanceProvider(this);

        entityRegistry = new EntityRegistry();
        protectionService = new ProtectionService(this);
        blockChangeJournal = new BlockChangeJournal();
        anomalyManager = new AnomalyManager(this, protectionService, blockChangeJournal);
        audioDirector = new AudioDirector(this);

        // Initialize persistence layer
        DatabaseManager dbManager = new DatabaseManager(this);
        PlayerProfileRepository playerRepo = new PlayerProfileRepository(dbManager);
        EncounterRepository encounterRepo = new EncounterRepository(dbManager);

        // Initialize entity controller (needed by other managers)
        entityController = new HerobrineEntityController(this, appearanceService, entityRegistry, anomalyManager, blockChangeJournal, audioDirector);

        // Create adaptive stalking AI
        AdaptiveStalkingAI adaptiveStalkingAI = new AdaptiveStalkingAI(this, entityRegistry, entityController, playerRepo, dbManager);

        // Create managers with proper dependencies
        AdvancedAIManager advancedAIManager = new AdvancedAIManager(this, dbManager, playerRepo, encounterRepo, entityRegistry, entityController, adaptiveStalkingAI);
        encounterStateManager = new EncounterStateManager(this, entityController, entityRegistry, audioDirector, anomalyManager, blockChangeJournal);
        hauntingDirector = new HauntingDirector(this, configManager, entityController, entityRegistry, advancedAIManager);

        // Create false sighting engine
        falseSightingEngine = new FalseSightingEngine(this);

        getLogger().info("HerobrineHaunting has been enabled!");
        getLogger().info("White eyes supported: " + appearanceService.supportsWhiteEyes());

        // Register commands
        this.getCommand("herobrine").setExecutor(new HerobrineCommand(this));

        // Start the haunting director
        hauntingDirector.start();

        // Start the encounter state manager task
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::updateEncounters, 20L, 20L); // Check every second (20 ticks)
    }

    /**
     * Updates all active encounters and checks for false sightings - called periodically.
     */
    private void updateEncounters() {
        if (encounterStateManager != null) {
            // In a full implementation, we'd iterate through all active entities
            // For now, we'll just note that this should happen
            // encounterStateManager.updateAllEncounters();
        }

        // Check for false sightings for all online players
        if (falseSightingEngine != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location falseSightingLoc = falseSightingEngine.maybeGenerateFalseSighting(player);
                if (falseSightingLoc != null) {
                    // In a full implementation, we would spawn a temporary entity here
                    // For now, we'll just log that a false sighting was generated
                    // The actual visual would be handled by spawning a temporary entity
                    // that disappears after a short time
                    player.sendMessage(ChatColor.DARK_PURPLE + "You think you saw something in the distance...");
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("HerobrineHaunting has been disabled!");
        if (task != null) {
            task.cancel();
        }
        // Stop the haunting director
        if (hauntingDirector != null) {
            hauntingDirector.stop();
        }
        // Clean up resources
        if (configManager != null) {
            configManager.saveConfig();
        }
        if (entityRegistry != null) {
            entityRegistry.clear();
        }
        if (blockChangeJournal != null) {
            blockChangeJournal.clear();
        }
        if (audioDirector != null) {
            // Clean up any active audio encounters
            // In a full implementation, we'd iterate through all players and end their audio
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AppearanceService getAppearanceService() {
        return appearanceService;
    }

    public EntityRegistry getEntityRegistry() {
        return entityRegistry;
    }

    public HerobrineEntityController getEntityController() {
        return entityController;
    }

    public ProtectionService getProtectionService() {
        return protectionService;
    }

    public BlockChangeJournal getBlockChangeJournal() {
        return blockChangeJournal;
    }

    public AnomalyManager getAnomalyManager() {
        return anomalyManager;
    }

    public AudioDirector getAudioDirector() {
        return audioDirector;
    }

    public HauntingDirector getHauntingDirector() {
        return hauntingDirector;
    }

    public EncounterStateManager getEncounterStateManager() {
        return encounterStateManager;
    }
}