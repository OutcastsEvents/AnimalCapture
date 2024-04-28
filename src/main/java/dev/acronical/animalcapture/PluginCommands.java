package dev.acronical.animalcapture;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
//import java.util.logging.Logger;

public class PluginCommands implements CommandExecutor {

//    Logger logger = Logger.getLogger("AnimalCapture");

    String[] clydeLocation = { "-91", "-52", "75" };

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
            { "zombie_villager", "-76", "-52", "20" },
            { "spider", "-115", "-52", "35" },
            { "mule", "-111", "-52", "73" }
    };

    private void removeMobs(World world) {
        Entity[] entities = world.getEntities().toArray(new Entity[0]);
        for (Entity e : entities) {
            if (e instanceof Player) continue;
            e.remove();
        }
    }

    private void spawnMobs(Player player) {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
        World world = player.getWorld();
//        logger.info("Got world");

        for (String[] mobArray : mobList) {
//            logger.info("Mob array: " + Arrays.toString(mobArray));
            String mobName = mobArray[0];
            String x = mobArray[1];
            String y = mobArray[2];
            String z = mobArray[3];
            if (mobName.isEmpty() || x.isEmpty() || y.isEmpty() || z.isEmpty()) continue;
            Location mobLocation = new Location(world, Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
            EntityType found = null;
            for (EntityType type : EntityType.values()) {
                if (type.name().equalsIgnoreCase(mobName)) {
//                    logger.info("Found entity type " + type.name().toLowerCase() + " for mob " + mobName);
                    found = type;
                    break;
                }
            }
            if (found == null) continue;
            EntityType finalFound = found;
            int mobCount = (int) world.getEntities().stream().filter(entity -> entity.getType() == finalFound).count();
            for (int i = 2; mobCount < i; mobCount++) {
                LivingEntity mob = (LivingEntity) world.spawnEntity(mobLocation, found);
//                logger.info("Spawned entity " + found.name().toLowerCase() + " at " + x + ", " + y + ", " + z);
                mob.setInvulnerable(true);
                mob.setPersistent(true);
                mob.setRemoveWhenFarAway(false);
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
    }

    private void spawnClyde(Player player) {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
        String x = clydeLocation[0];
        String y = clydeLocation[1];
        String z = clydeLocation[2];
        World world = player.getWorld();
        EntityType clydeType = EntityType.FROG;
        int clydeCount = (int) world.getEntities().stream().filter(entity -> entity.getType() == clydeType).count();
        for (int i = 1; clydeCount < i; clydeCount++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon frog " + " " + x + " " + y + " " + z + " {Invulnerable:1b,CustomNameVisible:1b,PersistenceRequired:1b,variant:\"minecraft:temperate\",CustomName:'{\"bold\":true,\"color\":\"gold\",\"text\":\"Clyde\"}'}");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        if (command.getName().equalsIgnoreCase("captureinit") && (commandSender.hasPermission("arenapvp.captureinit") || commandSender.isOp())) {
            World world = player.getWorld();
            removeMobs(world);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            world.setSpawnFlags(true, true);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setPVP(false);
            Objective captured = player.getScoreboard().getObjective("captured");
            Objective redScore = player.getScoreboard().getObjective("redScore");
            Objective blueScore = player.getScoreboard().getObjective("blueScore");
            if (captured != null) {
                player.sendMessage("Score objectives already exist, did you reset them?");
                return false;
            }
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            if (redTeam == null) redTeam = player.getScoreboard().registerNewTeam("red");
            if (blueTeam == null) blueTeam = player.getScoreboard().registerNewTeam("blue");
            redTeam.displayName(Component.text("Red Team", NamedTextColor.RED));
            blueTeam.displayName(Component.text("Blue Team", NamedTextColor.BLUE));
            redTeam.setAllowFriendlyFire(false);
            blueTeam.setAllowFriendlyFire(false);
            redTeam.color(NamedTextColor.RED);
            blueTeam.color(NamedTextColor.BLUE);
            Player[] players = player.getServer().getOnlinePlayers().toArray(new Player[0]);
            int playerCount = players.length;
            int halfPlayerCount = playerCount / 2;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives add redScore dummy \"Red Score\"");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives add blueScore dummy \"Blue Score\"");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives add captured dummy \"Mobs Captured\"");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives setdisplay sidebar captured");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives setdisplay below_name captured");
            spawnMobs(player);
            spawnClyde(player);
            for (Player p : players) {
                if (p.getScoreboardTags().contains("admin")/* || (p.getName().equalsIgnoreCase("Yrrah908") || p.getName().equalsIgnoreCase("_wenzo") || p.isOp())*/) continue;
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
            }
            player.sendMessage("Initialised the plugin, do /capturestart to start the game!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturestart") && (commandSender.hasPermission("animalcapture.capturestart") || commandSender.isOp())) {
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
            Team adminTeam = player.getScoreboard().getTeam("admin");
            if (redTeam == null || blueTeam == null) {
                player.sendMessage("Teams do not exist, have you finished a game or initialised the plugin?");
                return false;
            }
            redTeam.unregister();
            blueTeam.unregister();
            Player[] onlinePlayers = player.getServer().getOnlinePlayers().toArray(new Player[0]);
            world.setSpawnLocation(new Location(world, -90, 0, 69));
            removeMobs(world);
            for (Player p : onlinePlayers) {
                if (adminTeam != null && adminTeam.hasEntry(p.getName())/* || p.isOp()*/) continue;
                if (!p.getPassengers().isEmpty()) {
                    Entity mob = player.getPassengers().stream().filter(Objects::nonNull).findFirst().orElse(null);
                    p.getPassengers().remove(mob);
                }
                p.setRespawnLocation(new Location(world, -90, 0, 69));
                p.getInventory().clear();
                p.teleportAsync(new Location(world, -90, 0, 69));
                p.setGameMode(GameMode.SURVIVAL);

            }
            player.sendMessage("Stopped the game, teleported players, reset teams and cleared inventories.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturereset") && (commandSender.hasPermission("animalcapture.capturereset") || commandSender.isOp())) {
            World world = player.getWorld();
            Team redTeam = player.getScoreboard().getTeam("red");
            Team blueTeam = player.getScoreboard().getTeam("blue");
            Objective captured = player.getScoreboard().getObjective("captured");
            Objective redTeamScore = player.getScoreboard().getObjective("redScore");
            Objective blueTeamScore = player.getScoreboard().getObjective("blueScore");
            if (captured == null || blueTeamScore == null || redTeamScore == null) {
                player.sendMessage("Scoreboard objectives do not exist, have you reset already?");
                return false;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives remove redScore");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives remove blueScore");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives remove captured");
            player.sendMessage("Reset the scoreboard and total scores.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("captureannounce") && (commandSender.hasPermission("animalcapture.captureannounce") || commandSender.isOp())) {
            Objective captured = player.getScoreboard().getObjective("captured");
            Objective redTeamScore = player.getScoreboard().getObjective("redScore");
            Objective blueTeamScore = player.getScoreboard().getObjective("blueScore");
            if (captured == null || blueTeamScore == null || redTeamScore == null) {
                player.sendMessage("Scoreboard objectives do not exist, have you initialised?");
                return false;
            }

            int redTeamScoreVal = redTeamScore.getScore("").getScore();
            int blueTeamScoreVal = blueTeamScore.getScore("").getScore();

//            logger.info("Red Score " + redTeamScore.getScore("").getScore());
//            logger.info("Blue Score " + blueTeamScore.getScore("").getScore());

            if (redTeamScoreVal > blueTeamScoreVal) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("The §4§lRed §rteam has won!", NamedTextColor.RED), Component.text("§l" + redTeamScoreVal + "§r§4 points!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("The §4§lRed §rteam has won the game with §l" + redTeamScoreVal + " points!");
            } else if (blueTeamScoreVal > redTeamScoreVal) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("The §1§lBlue §rteam has won!", NamedTextColor.BLUE), Component.text("§l" + blueTeamScoreVal + "§r§1 points!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("The §1§lBlue §rteam has won the game with §l" + blueTeamScoreVal + " points!");
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(Title.title(Component.text("§eThe game has ended in a draw!", NamedTextColor.YELLOW), Component.text("§l" + redTeamScoreVal + "§r§e points each!", NamedTextColor.WHITE)));
                }
                Bukkit.broadcastMessage("§eThe game has ended in a draw!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("captureprogress") && (commandSender.hasPermission("animalcapture.captureprogress") || commandSender.isOp())) {
            Objective captured = player.getScoreboard().getObjective("captured");
            Objective redTeamScore = player.getScoreboard().getObjective("redScore");
            Objective blueTeamScore = player.getScoreboard().getObjective("blueScore");
            if (captured == null || blueTeamScore == null || redTeamScore == null) {
                player.sendMessage("Scoreboard objectives do not exist, have you initialised?");
                return false;
            }

            int redTeamScoreVal = redTeamScore.getScore("").getScore();
            int blueTeamScoreVal = blueTeamScore.getScore("").getScore();

            if (redTeamScoreVal > blueTeamScoreVal) {
                Bukkit.broadcastMessage("The §4§lRed §rteam is winning with §l" + redTeamScoreVal + " points!");
            } else if (blueTeamScoreVal > redTeamScoreVal) {
                Bukkit.broadcastMessage("The §1§lBlue §rteam is winning with §l" + blueTeamScoreVal + " points!");
            } else {
                Bukkit.broadcastMessage("The game is currently a draw with §l" + redTeamScoreVal + " points each!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("capturehelp") && (commandSender.hasPermission("animalcapture.capturehelp") || commandSender.isOp())) {
            player.sendMessage("§2/captureinit §r- Initialise the plugin and ready for the game");
            player.sendMessage("§2/capturestart §r- Start the game");
            player.sendMessage("§2/capturestop §r- Stop the game");
            player.sendMessage("§2/capturereset §r- Reset the scoreboard and total scores");
            player.sendMessage("§2/captureannounce §r- Announce the winner");
            player.sendMessage("§2/captureprogress §r- Show the current progress");
            return true;
        }

        return false;
    }
}
