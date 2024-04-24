package dev.acronical.animalcapture;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class PluginEvents implements Listener {

    String[][] MobList = {
        {"mooshroom", "200", "82","195" },
        {"polar bear", "264", "82","191" },
//        {"chicken", "", "", ""},
//        {"cow", "", "", ""},
//        {"sheep", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
//        {"", "", "", ""},
    };

    Logger logger = Logger.getLogger("AnimalCapture");

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
        player.setRespawnLocation(new Location(world, -90, 0, 69));
        player.teleport(new Location(world, -90, 0, 69));
        if (redTeam.getSize() < halfPlayerCount) {
            redTeam.addEntry(player.getName());
            player.teleportAsync(new Location(world, -10.5, -47, 73));
            player.setRespawnLocation(new Location(world, -10.5, -47, 73));
            String msg = "You are on the §4§lRED§r team!";
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.sendMessage("§2The game has started!");
            player.sendMessage(msg);
        } else if (blueTeam.getSize() < halfPlayerCount) {
            blueTeam.addEntry(player.getName());
            player.teleportAsync(new Location(world, -172.5, -47, 73));
            player.setRespawnLocation(new Location(world, -172.5, -47, 73));
            String msg = "You are on the §1§lBLUE§r team!";
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.sendMessage("§2The game has started!");
            player.sendMessage(msg);
        } else {
            if (redTeam.getSize() < blueTeam.getSize()) {
                redTeam.addEntry(player.getName());
                player.teleportAsync(new Location(world, -10.5, -47, 73));
                player.setRespawnLocation(new Location(world, -10.5, -47, 73));
                String msg = "You are on the §4§lRED§r team!";
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.sendMessage("§2The game has started!");
                player.sendMessage(msg);
            } else {
                blueTeam.addEntry(player.getName());
                player.teleportAsync(new Location(world, -172.5, -47, 73));
                player.setRespawnLocation(new Location(world, -172.5, -47, 73));
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
        Animals mob = (Animals) e.getRightClicked();
        Player player = e.getPlayer();
        Animals ridingMob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        logger.info("Player interacted with entity");
        if (player.getPassengers().size() == 1 && ridingMob != null) player.removePassenger(ridingMob);
        logger.info("Mob: " + mob.getName());
        logger.info("Player: " + player.getName());
        mob.setInvulnerable(true);
        // ! Make the mob ride the player
        player.addPassenger(mob);
    }

    @EventHandler
    public void onAnimalDrop(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            Animals mob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
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
        // ! Reset the mob on the player's leash
        Animals mob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        if (mob == null) return;
        player.removePassenger(mob);
        // ! Get the mob respawn location from MobList
        String mobName = mob.getName().toLowerCase();
        String[] mobData = null;
        for (String[] mobEntry : MobList) {
            logger.info("Checking list!");
            logger.info(Arrays.toString(mobEntry) + "\n" + mobName);
            if (mobEntry[0].equals(mobName)) {
                logger.info("Found mob!");
                mobData = mobEntry;
                logger.info("Set mob!");
                break;
            }
        }
        if (mobData == null) return;
        // ! Respawn the mob
        logger.info("Teleporting mob!");
        if (attacker.getPassengers().size() == 1) mob.teleport(new Location(world, Double.parseDouble(Objects.requireNonNull(mobData[1])), Double.parseDouble(Objects.requireNonNull(mobData[2])), Double.parseDouble(Objects.requireNonNull(mobData[3]))));
        else attacker.addPassenger(mob);
    }

    /**
     TODO: Logic for point scoring
     * * Score when player has passengers and is on their team's block
     * * Add score to player scoreboard and use variables in this file to have the total score
     * ! Variable must be publicly accessible from PluginCommands
     **/
}
