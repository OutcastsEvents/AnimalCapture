package dev.acronical.animalcapture;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public class PluginCommands implements CommandExecutor {

    Logger logger = Logger.getLogger("AnimalCapture");

    File scoreFile = new File(Bukkit.getPluginsFolder() + "/animalcapture", "scores.yml");
    FileConfiguration data = YamlConfiguration.loadConfiguration(scoreFile);

    int redScore = data.getInt("redScore") == 0 ? 0 : data.getInt("redScore");
    int blueScore = data.getInt("blueScore") == 0 ? 0 : data.getInt("blueScore");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        if (command.getName().equalsIgnoreCase("captureinit") && (commandSender.hasPermission("arenapvp.arenainit") || commandSender.isOp())) {
            World world = player.getWorld();
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setPVP(false);
            Objective captured = player.getScoreboard().getObjective("captured");
            if (captured != null) {
                player.sendMessage("Captured objective already exists, did you reset it?");
                return false;
            }
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
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives add captured dummy \"Mobs Captured\"");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives setdisplay sidebar captured");
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
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpoint @a[team=blue] -172 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpoint @a[team=red] -10 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @a[team=blue] -172 -47 73");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @a[team=red] -10 -47 73");
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
                if (!p.getScoreboardTags().contains("admin") || p.isOp()) {
                    if (!p.getPassengers().isEmpty()) {
                        p.getPassengers().clear();
                    }
                    p.setRespawnLocation(new Location(world, -90, 0, 69));
                    p.getInventory().clear();
                    p.teleportAsync(new Location(world, -90, 0, 69));
                    p.setGameMode(GameMode.SURVIVAL);
                }
            }
            player.sendMessage("Stopped the game, teleported players, reset teams and cleared inventories.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturereset") && (commandSender.hasPermission("animalcapture.capturereset") || commandSender.isOp())) {
            World world = player.getWorld();
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            Objective captured = player.getScoreboard().getObjective("captured");
            if (captured == null) {
                player.sendMessage("Captured objective does not exist, have you reset already?");
                return false;
            }
            redScore = 0;
            blueScore = 0;
            data.set("redScore", redScore);
            data.set("blueScore", blueScore);
            try {
                data.save(scoreFile);
                logger.info("Reset scores to 0.");
                logger.info("Red Score: " + data.getInt("redScore"));
                logger.info("Blue Score: " + data.getInt("blueScore"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.getScoreboard().resetScores("§4Red Team: " + redScore);
            player.getScoreboard().resetScores("§1Blue Team: " + blueScore);
            captured.unregister();
            player.sendMessage("Reset the scoreboard and total scores.");
            return true;
        }

        BukkitTask scoreCheck = Bukkit.getServer().getScheduler().runTaskAsynchronously(AnimalCapture.getPlugin(AnimalCapture.class), () -> {
            redScore = data.getInt("redScore");
            blueScore = data.getInt("blueScore");
        });

        if (command.getName().equalsIgnoreCase("captureannounce") && (commandSender.hasPermission("animalcapture.captureannounce") || commandSender.isOp())) {
            Objective captured = player.getScoreboard().getObjective("captured");
            if (captured == null) {
                player.sendMessage("Captured objective does not exist, have you reset already?");
                return false;
            }

            logger.info("Red Score: " + redScore);
            logger.info("Blue Score: " + blueScore);

            if (redScore > blueScore) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("§4The §lRed Team §rhas won!", NamedTextColor.RED), Component.text("§l" + redScore + "§r points!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("The §lRed Team §rhas won the game with §l" + redScore + " points!");
            } else if (blueScore > redScore) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("§1The §lBlue Team §rhas won!", NamedTextColor.BLUE), Component.text("§l" + blueScore + "§r points!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("The §lBlue Team §rhas won the game with §l" + blueScore + " points!");
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("§eThe game has ended in a draw!", NamedTextColor.YELLOW), Component.text("§l" + redScore + "§r points each!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("§eThe game has ended in a draw!");
            }
            return true;
        }

        return false;
    }
}
