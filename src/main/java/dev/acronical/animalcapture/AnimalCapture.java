package dev.acronical.animalcapture;

import org.bukkit.plugin.java.JavaPlugin;

public final class AnimalCapture extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PluginEvents(), this);
        getServer().getConsoleSender().sendMessage("[AnimalCapture] Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[AnimalCapture] Plugin disabled!");
    }
}
