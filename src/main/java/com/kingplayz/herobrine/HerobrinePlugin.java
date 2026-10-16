package com.kingplayz.herobrine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.kingplayz.herobrine.ai.EncounterStateManager;
import com.kingplayz.herobrine.ai.HauntingDirector;
import com.kingplayz.herobrine.config.ConfigManager;
import com.kingplayz.herobrine.entity.AppearanceService;
import com.kingplayz.herobrine.entity.DefaultAppearanceProvider;
import com.kingplayz.herobrine.entity.EntityRegistry;
import com.kingplayz.herobrine.entity.HerobrineEntityController;
import com.kingplayz.herobrine.entity.WhiteEyedAppearanceProvider;
import com.kingplayz.herobrine.world.AnomallyManager;
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

    @Override
    public void onEnable() {
        // Plugin startup logic
        configManager = new ConfigManager(this);

        // Use the white-eyed appearance provider
        appearanceService = new WhiteEyedAppearanceProvider(this);

        entityRegistry = new EntityRegistry();
        protectionService = new ProtectionService(this);
        blockChangeJournal = new BlockChangeJournal();
        anomalyManager = new AnomalyManager(this, protectionService, blockChangeJournal);
        audioDirector = new AudioDirector(this);
        encounterStateManager = new EncounterStateManager(this, null, entityRegistry, audioDirector, anomalyManager, blockChangeJournal);
        hauntingDirector = new HauntingDirector(this, configManager, null, entityRegistry);
        entityController = new HerobrineEntityController(this, appearanceService, entityRegistry, anomalyManager, blockChangeJournal, audioDirector);

        // Now that we have the entityController, update the managers that need it
        encounterStateManager = new EncounterStateManager(this, entityController, entityRegistry, audioDirector, anomalyManager, blockChangeJournal);
        hauntingDirector = new HauntingDirector(this, configManager, entityController, entityRegistry);

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
     * Updates all active encounters - called periodically.
     */
    private void updateEncounters() {
        if (encounterStateManager != null) {
            // In a full implementation, we'd iterate through all active entities
            // For now, we'll just note that this should happen
            // encounterStateManager.updateAllEncounters();
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