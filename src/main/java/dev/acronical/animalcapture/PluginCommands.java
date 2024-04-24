package dev.acronical.animalcapture;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class PluginCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        if (command.getName().equalsIgnoreCase("captureinit") && (commandSender.hasPermission("arenapvp.arenainit") || commandSender.isOp())) {
            World world = player.getWorld();
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setPVP(false);
//            logger.info("Initializing arena...");
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
//            logger.info("Getting teams...");
            if (redTeam == null) redTeam = player.getScoreboard().registerNewTeam("red");
            if (blueTeam == null) blueTeam = player.getScoreboard().registerNewTeam("blue");
//            logger.info("Got teams");
            redTeam.displayName(Component.text("Red Team", NamedTextColor.RED));
            blueTeam.displayName(Component.text("Blue Team", NamedTextColor.BLUE));
//            logger.info("Set team display names");
            redTeam.setAllowFriendlyFire(false);
            blueTeam.setAllowFriendlyFire(false);
//            logger.info("Set team friendly fire");
            redTeam.color(NamedTextColor.RED);
            blueTeam.color(NamedTextColor.BLUE);
//            logger.info("Set team colors");
            Player[] players = player.getServer().getOnlinePlayers().toArray(new Player[0]);
//            logger.info("Got players");
            int playerCount = players.length;
            int halfPlayerCount = playerCount / 2;
//            logger.info("Got player count and half player count");
            for (Player p : players) {
                if (p.getScoreboardTags().contains("admin") || (p.getName().equalsIgnoreCase("Yrrah908") || p.getName().equalsIgnoreCase("Wenzo") || p.isOp())) continue;
                p.setGameMode(GameMode.SURVIVAL);
                if (redTeam.getSize() < blueTeam.getSize()) {
                    redTeam.addEntry(p.getName());
                } else if (blueTeam.getSize() < redTeam.getSize()) {
                    blueTeam.addEntry(p.getName());
                } else if (redTeam.getSize() < halfPlayerCount) {
                    redTeam.addEntry(p.getName());
                } else if (blueTeam.getSize() < halfPlayerCount) {
                    blueTeam.addEntry(p.getName());
                } else {
                    redTeam.addEntry(p.getName());
                }
//                logger.info("Added player " + p.getName() + " to team " + (p.getScoreboard().getEntityTeam(p).getName()) + ".");
            }
//            logger.info("Arena initialized!");
            player.sendMessage("Initialised the plugin, do /capturestart to start the game!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturestart") && (commandSender.hasPermission("animalcapture.capturestop") || commandSender.isOp())) {
            // Disable command output
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            console.sendMessage("Starting the game...");
            World world = player.getWorld();
            world.setPVP(true);
            Player[] players = player.getServer().getOnlinePlayers().toArray(new Player[0]);
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            if (redTeam == null || blueTeam == null) {
                player.sendMessage("Teams do not exist, have you initialised the plugin?");
                return false;
            }
            for (Player p : players) {
                Team team = (redTeam.hasEntry(p.getName())) ? redTeam : blueTeam;
                String colour = (team.getName().equalsIgnoreCase("red")) ? "§4" : "§1";
                String msg = "You are on the §l" + colour + team.getName().toUpperCase() + "§r team!";
                p.setHealth(20);
                p.setFoodLevel(20);
                p.setSaturation(20);
                p.sendMessage("§2The game has started!");
                p.sendMessage(msg);
                p.setGameMode(GameMode.SURVIVAL);
            }

            // Teleport players to their respective spawn points
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpoint @a[team=blue] -172.5 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpoint @a[team=red] -10.5 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @a[team=blue] -172.5 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @a[team=red] -10.5 -47 73");
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturestop") && (commandSender.hasPermission("animalcapture.capturestop") || commandSender.isOp())) {
            World world = player.getWorld();
            world.setPVP(false);
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            if (redTeam == null || blueTeam == null) {
                player.sendMessage("Teams do not exist, have you finished a game or initialised the plugin?");
                return false;
            }
            redTeam.unregister();
            blueTeam.unregister();
            Player[] onlinePlayers = player.getServer().getOnlinePlayers().toArray(new Player[0]);
            world.setSpawnLocation(new Location(world, -90, 0, 69));
            for (Player p : onlinePlayers) {
                p.getInventory().clear();
                if (!p.getScoreboardTags().contains("admin") || p.isOp()) {
                    p.teleportAsync(new Location(world, -90, 0, 69));
                    p.setRespawnLocation(new Location(world, -90, 0, 69));
                    p.setGameMode(GameMode.SURVIVAL);
                }
            }
            player.sendMessage("Stopped the game, teleported players, reset teams and cleared inventories.");
            return true;
        }

        return false;
    }
}
