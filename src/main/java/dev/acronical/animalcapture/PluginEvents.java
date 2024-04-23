package dev.acronical.animalcapture;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class PluginEvents implements Listener {

    String[][] MobList = {
        {"mooshroom", "200", "82","195" },
        {"polar_bear", "264", "82","191" }
    };

    Logger logger = Logger.getLogger("AnimalCapture");

    @EventHandler
    public void onAnimalCapture(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) return;
        Animals mob = (Animals) e.getRightClicked();
        Player player = e.getPlayer();
        Animals ridingMob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        logger.info("Player interacted with entity");
        if (player.getPassengers().size() == 1 || ridingMob != null) player.removePassenger(ridingMob);
        logger.info("Mob: " + mob.getName());
        logger.info("Player: " + player.getName());
        mob.setInvulnerable(true);
        // ! Make the mob ride the player
        player.addPassenger(mob);
        e.setCancelled(true);
    }

    @EventHandler
    public void onAnimalDrop(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Animals mob = (Animals) player.getPassengers().stream().filter(entity -> entity instanceof Animals).findFirst().orElse(null);
        if (mob == null || player.getPassengers().isEmpty()) {
            e.setCancelled(true);
            return;
        }
        player.removePassenger(mob);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent e) {
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
        mob.teleport(new Location(mob.getWorld(), Double.parseDouble(mobData[1]), Double.parseDouble(mobData[2]), Double.parseDouble(mobData[3])));
    }
}
