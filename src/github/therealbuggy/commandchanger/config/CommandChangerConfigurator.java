package github.therealbuggy.commandchanger.config;

import org.bukkit.plugin.Plugin;

import java.io.File;

import github.therealbuggy.bukkitconfigurator.BukkitConfigurator;

/**
 * Created by jonathan on 10/02/16.
 */

/**
 * This way aren't recommended, the BukkitConfigurator API is in Alpha development stage
 */
public class CommandChangerConfigurator<E> extends BukkitConfigurator<E> {

    public CommandChangerConfigurator(File configFile, Plugin plugin) {
        super(configFile, plugin);
    }

}
