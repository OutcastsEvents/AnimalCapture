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
        logger.info("Player interacted with entity");
        e.setCancelled(true);
        Mob mob = (Mob) e.getRightClicked();
        logger.info("Mob: " + mob.getName());
        Player player = e.getPlayer();
        logger.info("Player: " + player.getName());
        mob.setLeashHolder(player);
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
        Animals mob = (Animals) player.getWorld().getEntities().stream().filter(entity -> entity instanceof Animals && ((Animals) entity).getLeashHolder() == player).findFirst().orElse(null);
        if (mob == null) return;
        mob.setLeashHolder(null);
        // ! Get the mob respawn location from MobList
        String mobName = mob.getName().toLowerCase();
        String[] mobData = null;
        for (String[] mobEntry : MobList) {
            if (mobEntry[0].equals(mobName)) {
                mobData = mobEntry;
                break;
            }
        }
        if (mobData == null) return;
        // ! Respawn the mob
        mob.teleport(new Location(mob.getWorld(), Double.parseDouble(mobData[1]), Double.parseDouble(mobData[2]), Double.parseDouble(mobData[3])));
    }
}
