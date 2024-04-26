package dev.acronical.animalcapture;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class PluginEvents implements Listener {

    Logger logger = Logger.getLogger("AnimalCapture");

    File scoreFile = new File(Bukkit.getPluginsFolder() + "/animalcapture", "scores.yml");
    FileConfiguration data = YamlConfiguration.loadConfiguration(scoreFile);

    public int redScore = data.getInt("redScore") == 0 ? 0 : data.getInt("redScore");
    public int blueScore = data.getInt("blueScore") == 0 ? 0 : data.getInt("blueScore");

    String[][] mobList = {
            { "mushroom_cow", "-136", "-50", "75" },
            { "polar_bear", "-68", "-52", "75" },
            { "chicken", "-151", "-52", "53" },
            { "iron_golem", "-115", "-52", "114" },
            { "sheep", "-154", "-52", "74" },
            { "zombified_piglin", "-100", "-52", "95" },
            { "skeleton", "-90", "-52", "110" },
            { "horse", "-57", "-52", "133" },
            { "turtle", "-101", "-52", "56" },
            { "slime", "-67", "-51", "113" },
            { "strider", "-58", "-53", "93" },
            { "zoglin", "-79", "-52", "91" },
            { "pig", "-124", "-52", "125" },
            { "cow", "-35", "-52", "89" },
            { "wolf", "-91", "-52", "37" },
            { "pillager", "-131", "-52", "109" },
            { "sniffer", "-103", "-52", "133" },
            { "donkey", "-79", "-52", "132" },
            { "glow_squid", "-45", "-51", "112" },
            { "camel", "-34", "-52", "54" },
            { "axolotl", "-126", "-52", "96" },
            { "panda", "-121", "-52", "19" },
            { "ocelot", "-148", "-52", "91" },
            { "rabbit", "-27", "-51", "72" },
            { "fox", "-59", "-52", "56" },
            { "snowman", "-48", "-52", "73" },
            { "skeleton_horse", "-125", "-51", "54" },
            { "goat", "-135", "-52", "34" },
            { "trader_llama", "-68", "-52", "34" },
            { "dolphin", "-50", "-52", "32" },
            { "squid", "-80", "-52", "55" },
            { "llama", "-58", "-52", "15" },
            { "zombie", "-103", "-52", "17" },
            { "frog", "-76", "-52", "20" },
            { "spider", "-115", "-52", "35" },
            { "mule", "-111", "-52", "73" }
    };
    /*
         ! Logic to respawn mobs every 2 minutes
         * Respawn mobs at their respective locations
         * Use MobList to get the mob name and coordinates
         * Use the coordinates to spawn the mob at the location
         * Use the mob name to get the mob type
         * Use the mob type to spawn the mob
         ! Only two mobs of each type can be in the world at a time
         ! Entities must be invulnerable and non-aggressive
     */

    public BukkitTask summonTask = Bukkit.getServer().getScheduler().runTaskTimer(AnimalCapture.getPlugin(AnimalCapture.class), () -> {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
        World world = Objects.requireNonNull(Bukkit.getServer().getOnlinePlayers().stream().findFirst().orElse(null)).getWorld();
        logger.info("Got world");

        for (String[] mobArray : mobList) {
            logger.info("Mob array: " + Arrays.toString(mobArray));
            String mobName = mobArray[0];
            String x = mobArray[1];
            String y = mobArray[2];
            String z = mobArray[3];
            if (mobName.isEmpty() || x.isEmpty() || y.isEmpty() || z.isEmpty()) continue;
            Location mobLocation = new Location(world, Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
            EntityType found = null;
            for (EntityType type : EntityType.values()) {
                if (type.name().equalsIgnoreCase(mobName)) {
                    logger.info("Found entity type " + type.name().toLowerCase() + " for mob " + mobName);
                    found = type;
                    break;
                }
            }
            if (found == null) continue;
            EntityType finalFound = found;
            int mobCount = (int) world.getEntities().stream().filter(entity -> entity.getType() == finalFound).count();
            for (int i = 2; mobCount < i; mobCount++) {
                Entity mob = world.spawnEntity(mobLocation, found);
                logger.info("Spawned entity " + found.name().toLowerCase() + " at " + x + ", " + y + ", " + z);
                mob.setInvulnerable(true);
                mob.setPersistent(true);
                if (mob instanceof Animals animal) {
                    animal.setInvulnerable(true);
                    animal.setAggressive(false);
                }
                if (mob instanceof Mob mobEntity) {
                    mobEntity.setInvulnerable(true);
                    mobEntity.setAggressive(false);
                    mobEntity.setTarget(null);
                }
            }
        }
    }, 0L, 2400L);

    /*
         ! Logic for point scoring
         * Score when player has passengers and is on their team's block
         * Add score to player scoreboard and use variables in this file to have the total score
         ! Variable must be publicly accessible from PluginCommands
     */

    public BukkitTask scoreTask = Bukkit.getServer().getScheduler().runTaskTimer(AnimalCapture.getPlugin(AnimalCapture.class), () -> {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            if (redTeam == null || blueTeam == null) continue;
            World world = player.getWorld();
            Location playerLocation = player.getLocation();
            Location belowPlayer = playerLocation.clone().subtract(0, 1, 0);
            if (player.getPassengers().isEmpty()) continue;
            Entity mob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (mob == null) continue;
            if (redTeam.hasEntry(player.getName())) {
                if (belowPlayer.getBlock().getType() == Material.REDSTONE_BLOCK) {
                    redScore++;
                    data.set("redScore", redScore);
                    try {
                        data.save(scoreFile);
                        logger.info("Saved score data");
                        logger.info("Red score: " + data.getInt("redScore"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    player.getScoreboard().getObjectives().forEach(objective -> {
                        if (objective.getName().equalsIgnoreCase("captured")) {
                            objective.getScore(player.getName()).setScore(redScore);
                        }
                    });
                    player.teleport(new Location(world, -10, -47, 73));
                    player.removePassenger(mob);
                    mob.remove();
                    player.sendMessage("§2You have scored a point for the §4§lRED§r§2 team!");
                    Bukkit.broadcastMessage("§4§lRED§r team has scored a point!");
                }
            } else if (blueTeam.hasEntry(player.getName())) {
                if (belowPlayer.getBlock().getType() == Material.LAPIS_BLOCK) {
                    blueScore++;
                    data.set("blueScore", blueScore);
                    try {
                        data.save(scoreFile);
                        logger.info("Saved score data");
                        logger.info("Blue score: " + data.getInt("blueScore"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    player.getScoreboard().getObjectives().forEach(objective -> {
                        if (objective.getName().equalsIgnoreCase("captured")) {
                            objective.getScore(player.getName()).setScore(blueScore);
                        }
                    });
                    player.teleport(new Location(world, -172, -47, 73));
                    player.removePassenger(mob);
                    mob.remove();
                    player.sendMessage("§2You have scored a point for the §1§lBLUE§r§2 team!");
                    Bukkit.broadcastMessage("§1§lBLUE§r team has scored a point!");
                }
            }
        }
    }, 0L, 20L);

    @EventHandler
    public void onEntityClimb(EntityMoveEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getEntity().isClimbing()) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        int playerCount = player.getServer().getOnlinePlayers().size();
        int halfPlayerCount = playerCount / 2;
        Team redTeam = player.getScoreboard().getTeam("red");
        Team blueTeam = player.getScoreboard().getTeam("blue");
        if (redTeam == null || blueTeam == null) return;
        if (player.getScoreboardTags().contains("admin")) return;
        if (player.getName().equalsIgnoreCase("Yrrah908") || player.getName().equalsIgnoreCase("Wenzo") || player.isOp()) return;
        if (redTeam.hasEntry(player.getName())) {
            player.teleportAsync(new Location(world, -10, -47, 73));
            player.setRespawnLocation(new Location(world, -10, -47, 73));
        } else if (blueTeam.hasEntry(player.getName())) {
            player.teleportAsync(new Location(world, -172, -47, 73));
            player.setRespawnLocation(new Location(world, -172, -47, 73));
        }
        player.setRespawnLocation(new Location(world, -90, 0, 69));
        player.teleport(new Location(world, -90, 0, 69));
        if (redTeam.getSize() < halfPlayerCount) {
            redTeam.addEntry(player.getName());
            player.teleportAsync(new Location(world, -10, -47, 73));
            player.setRespawnLocation(new Location(world, -10, -47, 73));
            String msg = "You are on the §4§lRED§r team!";
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.sendMessage("§2The game has started!");
            player.sendMessage(msg);
        } else if (blueTeam.getSize() < halfPlayerCount) {
            blueTeam.addEntry(player.getName());
            player.teleportAsync(new Location(world, -172, -47, 73));
            player.setRespawnLocation(new Location(world, -172, -47, 73));
            String msg = "You are on the §1§lBLUE§r team!";
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.sendMessage("§2The game has started!");
            player.sendMessage(msg);
        } else {
            if (redTeam.getSize() < blueTeam.getSize()) {
                redTeam.addEntry(player.getName());
                player.teleportAsync(new Location(world, -10, -47, 73));
                player.setRespawnLocation(new Location(world, -10, -47, 73));
                String msg = "You are on the §4§lRED§r team!";
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.sendMessage("§2The game has started!");
                player.sendMessage(msg);
            } else {
                blueTeam.addEntry(player.getName());
                player.teleportAsync(new Location(world, -172, -47, 73));
                player.setRespawnLocation(new Location(world, -172, -47, 73));
                String msg = "You are on the §1§lBLUE§r team!";
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.sendMessage("§2The game has started!");
                player.sendMessage(msg);
            }
        }
    }

    @EventHandler
    public void onAnimalCapture(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) return;
        Entity entity = e.getRightClicked();

        if (entity.getType() == EntityType.VILLAGER) return;

        if (entity instanceof Animals mob) {
            mob.setInvulnerable(true);
            mob.setAggressive(false);
        }
        if (entity instanceof Mob mob) {
            mob.setInvulnerable(true);
            mob.setTarget(null);
            mob.setAggressive(false);
        }
        else entity.setInvulnerable(true);

        Player player = e.getPlayer();

        for (Player p : player.getServer().getOnlinePlayers()) {
            if (p.getPassengers().contains(entity)) {
                return;
            }
        }

        Entity ridingMob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
        logger.info("Player interacted with entity");
        if (player.getPassengers().size() == 1 && ridingMob != null) player.removePassenger(ridingMob);
        logger.info("Mob: " + entity.getName());
        logger.info("Player: " + player.getName());
        // ! Make the mob ride the player
        player.addPassenger(entity);
    }

    @EventHandler
    public void onAnimalDrop(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = e.getPlayer();
            Entity mob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (mob == null || player.getPassengers().isEmpty()) return;
            player.removePassenger(mob);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent e) {
        World world = e.getEntity().getWorld();
        if (!(e.getEntity() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }
        logger.info("Player damaged");
        if (!(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) e.setCancelled(true);
        logger.info("Player attacked by entity");
        if (!(e.getDamager() instanceof Player attacker)) {
            e.setCancelled(true);
        }
        logger.info("Player attacked by player");
        Animals mob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        if (mob == null) return;
        player.removePassenger(mob);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 255, true, true, false));
    }

    @EventHandler
    public void onMobDrop(EntityDropItemEvent e) {
        if (e.getEntity() instanceof Animals || e.getEntity() instanceof Mob) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().isOp()) return;
        Team adminTeam = e.getPlayer().getScoreboard().getTeam("admin");
        adminTeam = (adminTeam == null) ? e.getPlayer().getScoreboard().registerNewTeam("admin") : adminTeam;
        if (adminTeam.hasEntry(e.getPlayer().getName())) return;
        e.setCancelled(true);
    }
}
