package com.kingplayz.herobrine.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.kingplayz.herobrine.config.ConfigManager;
import com.kingplayz.herobrine.entity.AppearanceService;
import com.kingplayz.herobrine.entity.EntityRegistry;
import com.kingplayz.herobrine.entity.HerobrineEntityController;
import com.kingplayz.herobrine.world.BlockChangeJournal;
import java.util.UUID;

public class HerobrineCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HerobrineCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "Herobrine Haunting Commands:");
            sender.sendMessage(ChatColor.GREEN + "/herobrine help" + ChatColor.WHITE + " - Show this help");
            sender.sendMessage(ChatColor.GREEN + "/herobrine status" + ChatColor.WHITE + " - Show haunting status");
            sender.sendMessage(ChatColor.GREEN + "/herobrine enable" + ChatColor.WHITE + " - Enable haunting");
            sender.sendMessage(ChatColor.GREEN + "/herobrine disable" + ChatColor.WHITE + " - Disable haunting");
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GREEN + "/herobrine optout" + ChatColor.WHITE + " - Toggle personal encounters");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sender.sendMessage(ChatColor.AQUA + "Herobrine Haunting Commands:");
                sender.sendMessage(ChatColor.GREEN + "/herobrine help" + ChatColor.WHITE + " - Show this help");
                sender.sendMessage(ChatColor.GREEN + "/herobrine status" + ChatColor.WHITE + " - Show haunting status");
                sender.sendMessage(ChatColor.GREEN + "/herobrine enable" + ChatColor.WHITE + " - Enable haunting");
                sender.sendMessage(ChatColor.GREEN + "/herobrine disable" + ChatColor.WHITE + " - Disable haunting");
                if (sender instanceof Player) {
                    sender.sendMessage(ChatColor.GREEN + "/herobrine optout" + ChatColor.WHITE + " - Toggle personal encounters");
                }
                sender.sendMessage(ChatColor.GREEN + "/herobrine spawn <player>" + ChatColor.WHITE + " - Force a test encounter (admin)");
                sender.sendMessage(ChatColor.GREEN + "/herobrine despawn <player|all>" + ChatColor.WHITE + " - End active encounters (admin)");
                sender.sendMessage(ChatColor.GREEN + "/herobrine event <type> <player>" + ChatColor.WHITE + " - Trigger specific event (admin)");
                sender.sendMessage(ChatColor.GREEN + "/herobrine inspect <player>" + ChatColor.WHITE + " - Show encounter state (admin)");
                sender.sendMessage(ChatColor.GREEN + "/herobrine reset <player>" + ChatColor.WHITE + " - Reset haunting progress (admin)");
                sender.sendMessage(ChatColor.GREEN + "/herobrine reload" + ChatColor.WHITE + " - Reload configuration (admin)");
                break;
            case "status":
                ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                boolean enabled = configManager.getConfig().getBoolean("haunting.enabled", true);
                EntityRegistry entityRegistry = ((HerobrinePlugin) plugin).getEntityRegistry();
                int activeEntities = entityRegistry.getEntityCount();
                boolean worldModEnabled = configManager.getConfig().getBoolean("world.modify-blocks", false);

                sender.sendMessage(ChatColor.AQUA + "Herobrine Haunting Status:");
                sender.sendMessage(ChatColor.GREEN + "Enabled: " + ChatColor.WHITE + (enabled ? "true" : "false"));
                sender.sendMessage(ChatColor.GREEN + "Active Entities: " + ChatColor.WHITE + activeEntities);
                sender.sendMessage(ChatColor.GREEN + "World Modifications: " + ChatColor.WHITE + (worldModEnabled ? "enabled" : "disabled"));
                sender.sendMessage(ChatColor.GREEN + "White Eyes Supported: " + ChatColor.WHITE +
                        ((HerobrinePlugin) plugin).getAppearanceService().supportsWhiteEyes());
                break;
            case "enable":
                if (sender.hasPermission("herobrine.admin")) {
                    ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                    configManager.getConfig().set("haunting.enabled", true);
                    configManager.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Herobrine haunting enabled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "disable":
                if (sender.hasPermission("herobrine.admin")) {
                    ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                    configManager.getConfig().set("haunting.enabled", false);
                    configManager.saveConfig();
                    sender.sendMessage(ChatColor.RED + "Herobrine haunting disabled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "optout":
                if (sender instanceof Player) {
                    // Placeholder for opt-out logic
                    sender.sendMessage(ChatColor.YELLOW + "Opt-out toggled. (Not implemented yet)");
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
                break;
            case "spawn":
                if (sender.hasPermission("herobrine.admin")) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /herobrine spawn <player>");
                        break;
                    }

                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                        break;
                    }

                    // Get configuration for spawn settings
                    ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                    int observationDuration = configManager.getConfig().getInt("appearance.observation-duration-seconds", 8);

                    // Spawn Herobrine near the player (behind them)
                    Location playerLoc = target.getLocation();
                    Location spawnLoc = playerLoc.clone().add(0, 1, 0); // Start above player's head

                    // Generate a unique encounter ID
                    String encounterId = "test-" + System.currentTimeMillis();

                    // Spawn the entity
                    HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();
                    Entity entity = entityController.spawnHerobrine(
                            target,
                            encounterId,
                            spawnLoc,
                            observationDuration
                    );

                    if (entity != null) {
                        // Also create some evidence as part of the spawn
                        entityController.createEncounterEvidence(target, encounterId);
                        sender.sendMessage(ChatColor.GREEN + "Spawned Herobrine entity for " + target.getName() +
                                " (will despawn in " + observationDuration + " seconds)");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to spawn Herobrine entity");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "despawn":
                if (sender.hasPermission("herobrine.admin")) {
                    EntityRegistry entityRegistry = ((HerobrinePlugin) plugin).getEntityRegistry();
                    HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();
                    BlockChangeJournal blockChangeJournal = ((HerobrinePlugin) plugin).getBlockChangeJournal();

                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /herobrine despawn <player|all>");
                        break;
                    }

                    if (args[1].equalsIgnoreCase("all")) {
                        // Despawn all entities
                        UUID[] entityUuids = entityRegistry.getEntityDataMap().keySet().toArray(new UUID[0]);
                        int count = 0;
                        for (UUID uuid : entityUuids) {
                            // Get encounter ID for proper cleanup
                            EntityRegistry.EntityData data = entityRegistry.getEntityData(uuid);
                            String encounterId = data != null ? data.getEncounterId() : "unknown";
                            entityController.despawnHerobrine(uuid, encounterId);
                            count++;
                        }
                        sender.sendMessage(ChatColor.GREEN + "Despawned " + count + " Herobrine entities");
                    } else {
                        // Despawn entities for a specific player
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                            break;
                        }

                        // Find and despawn entities targeting this player
                        int count = 0;
                        for (UUID entityUuid : entityRegistry.getEntityDataMap().keySet()) {
                            UUID targetUuid = ((HerobrinePlugin) plugin).getEntityController().getTargetPlayer(entityUuid);
                            if (targetUuid != null && targetUuid.equals(target.getUniqueId())) {
                                // Get encounter ID for proper cleanup
                                EntityRegistry.EntityData data = entityRegistry.getEntityData(entityUuid);
                                String encounterId = data != null ? data.getEncounterId() : "unknown";
                                entityController.despawnHerobrine(entityUuid, encounterId);
                                count++;
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "Despawned " + count + " Herobrine entities targeting " + target.getName());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "event":
                if (sender.hasPermission("herobrine.admin")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /herobrine event <type> <player>");
                        sender.sendMessage(ChatColor.RED + "Event types: sighting, evidence, manifestation");
                        break;
                    }

                    String eventType = args[1].toLowerCase();
                    Player target = plugin.getServer().getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                        break;
                    }

                    // Generate a unique encounter ID for this event
                    String encounterId = "event-" + eventType + "-" + System.currentTimeMillis();

                    switch (eventType) {
                        case "sighting":
                            // Direct sighting - spawn Herobrine briefly
                            ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                            int observationDuration = configManager.getConfig().getInt("appearance.observation-duration-seconds", 8);
                            Location playerLoc = target.getLocation();
                            Location spawnLoc = playerLoc.clone().add(0, 1, 0);

                            HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();
                            Entity entity = entityController.spawnHerobrine(
                                    target,
                                    encounterId,
                                    spawnLoc,
                                    observationDuration
                            );

                            if (entity != null) {
                                sender.sendMessage(ChatColor.GREEN + "Triggered sighting event for " + target.getName());
                            } else {
                                sender.sendMessage(ChatColor.RED + "Failed to trigger sighting event");
                            }
                            break;
                        case "evidence":
                            // Just create evidence without spawning entity
                            HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();
                            entityController.createEncounterEvidence(target, encounterId);
                            sender.sendMessage(ChatColor.GREEN + "Triggered evidence event for " + target.getName());
                            break;
                        case "manifestation":
                            // Full manifestation with evidence and brief appearance
                            ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                            int observationDuration = configManager.getConfig().getInt("appearance.observation-duration-seconds", 8);
                            Location playerLoc = target.getLocation();
                            Location spawnLoc = playerLoc.clone().add(0, 1, 0);

                            HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();
                            Entity entity = entityController.spawnHerobrine(
                                    target,
                                    encounterId,
                                    spawnLoc,
                                    observationDuration
                            );

                            if (entity != null) {
                                entityController.createEncounterEvidence(target, encounterId);
                                sender.sendMessage(ChatColor.GREEN + "Triggered manifestation event for " + target.getName());
                            } else {
                                sender.sendMessage(ChatColor.RED + "Failed to trigger manifestation event");
                            }
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "Unknown event type. Use: sighting, evidence, manifestation");
                            break;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "inspect":
                if (sender.hasPermission("herobrine.admin")) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /herobrine inspect <player>");
                        break;
                    }

                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                        break;
                    }

                    EntityRegistry entityRegistry = ((HerobrinePlugin) plugin).getEntityRegistry();
                    HerobrineEntityController entityController = ((HerobrinePlugin) plugin).getEntityController();

                    boolean foundActive = false;
                    for (UUID entityUuid : entityRegistry.getEntityDataMap().keySet()) {
                        UUID targetUuid = entityController.getTargetPlayer(entityUuid);
                        if (targetUuid != null && targetUuid.equals(target.getUniqueId())) {
                            EntityRegistry.EntityData data = entityRegistry.getEntityData(entityUuid);
                            long timeLeft = entityController.getTimeUntilExpiration(entityUuid);

                            sender.sendMessage(ChatColor.AQUA + "Herobrine Encounter Info for " + target.getName() + ":");
                            sender.sendMessage(ChatColor.GREEN + "Encounter ID: " + ChatColor.WHITE + data.getEncounterId());
                            sender.sendMessage(ChatColor.GREEN + "Spawned at: " + ChatColor.WHITE + data.getSpawnTime());
                            sender.sendMessage(ChatColor.GREEN + "Time remaining: " + ChatColor.WHITE + (timeLeft / 1000) + " seconds");
                            foundActive = true;
                        }
                    }

                    if (!foundActive) {
                        sender.sendMessage(ChatColor.AQUA + "No active Herobrine encounter found for " + target.getName());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "reset":
                if (sender.hasPermission("herobrine.admin")) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /herobrine reset <player>");
                        break;
                    }

                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                        break;
                    }

                    // In a full implementation, this would reset the player's haunting level and history
                    sender.sendMessage(ChatColor.GREEN + "Reset haunting progress for " + target.getName() + ". (Not fully implemented)");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            case "reload":
                if (sender.hasPermission("herobrine.admin")) {
                    ConfigManager configManager = ((HerobrinePlugin) plugin).getConfigManager();
                    configManager.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /herobrine help for a list of commands.");
                break;
        }

        return true;
    }
}