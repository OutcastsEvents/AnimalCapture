package dev.acronical.animalcapture;

import org.bukkit.plugin.java.JavaPlugin;

public final class AnimalCapture extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PluginEvents(), this);
        getCommand("captureinit").setExecutor(new PluginCommands());
        getCommand("capturestart").setExecutor(new PluginCommands());
        getCommand("capturestop").setExecutor(new PluginCommands());
        getServer().getConsoleSender().sendMessage("[AnimalCapture] Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[AnimalCapture] Plugin disabled!");
    }
}
