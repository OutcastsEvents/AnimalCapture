package dev.acronical.animalcapture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

    int redScore = data.getInt("redScore") == 0 ? 0 : data.getInt("redScore");
    int blueScore = data.getInt("blueScore") == 0 ? 0 : data.getInt("blueScore");

    /**
     * TODO: Logic for point scoring
     * * Score when player has passengers and is on their team's block
     * * Add score to player scoreboard and use variables in this file to have the total score
     * ! Variable must be publicly accessible from PluginCommands
     **/

    public BukkitTask scoreTask = Bukkit.getServer().getScheduler().runTaskTimer(AnimalCapture.getPlugin(AnimalCapture.class), () -> {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            if (redTeam == null || blueTeam == null) return;
            World world = player.getWorld();
            Location playerLocation = player.getLocation();
            Location belowPlayer = playerLocation.clone().subtract(0, 1, 0);
            if (player.getPassengers().isEmpty()) return;
            Entity mob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (mob == null) return;
            if (player.getScoreboard().getEntityTeam(player).getName().equalsIgnoreCase("red")) {
                if (belowPlayer.getBlock().getType() == Material.REDSTONE_BLOCK) {
                    data.set("redScore", redScore + 1);
                    redScore = data.getInt("redScore");
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
                    player.sendMessage("§2You have scored a point for the §4§lRED§r team!");
                    Bukkit.broadcastMessage("§4§lRED§r team has scored a point!");
                }
            } else if (player.getScoreboard().getEntityTeam(player).getName().equalsIgnoreCase("blue")) {
                if (belowPlayer.getBlock().getType() == Material.LAPIS_BLOCK) {
                    data.set("blueScore", blueScore + 1);
                    blueScore = data.getInt("blueScore");
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
                    player.sendMessage("§2You have scored a point for the §1§lBLUE§r team!");
                    Bukkit.broadcastMessage("§1§lBLUE§r team has scored a point!");
                }
            }
        }
    }, 0L, 20L);

    String[][] MobList = {
        {"mooshroom", "200", "82", "195" },
        {"polar bear", "264", "82", "191" },
        {"chicken", "", "", ""},
        {"cow", "", "", ""},
        {"sheep", "-154", "-52", "74"},
        {"piglin", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
        {"", "", "", ""},
    };

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
        if (redTeam.hasEntry(player.getName()) || blueTeam.hasEntry(player.getName())) return;
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
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            Entity mob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (mob == null || player.getPassengers().isEmpty()) return;
            player.removePassenger(mob);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent e) {
        World world = e.getEntity().getWorld();
        if (!(e.getEntity() instanceof Player player)) return;
        logger.info("Player damaged");
        if (!(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        logger.info("Player attacked by entity");
        if (!(e.getDamager() instanceof Player attacker)) return;
        logger.info("Player attacked by player");
        Animals mob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        if (mob == null) return;
        player.removePassenger(mob);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 255, true, true, false));
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
