package github.therealbuggy.commandchanger.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

import github.therealbuggy.bukkitconfigurator.BukkitConfigurator;
import github.therealbuggy.bukkitconfigurator.backend.BukkitConfiguratorBackend;
import github.therealbuggy.commandchanger.config.template.ChangerTemplate;
import github.therealbuggy.configurator.data.DataProvider;

/**
 * Created by jonathan on 10/02/16.
 */

public class CommandChangerConfigurator<E> extends BukkitConfigurator<E> {

    public CommandChangerConfigurator(File configFile, Plugin plugin) {
        super(new CommandChangerBackend(configFile), plugin);
    }


    @DataProvider({ChangerTemplate.class})
    private static class CommandChangerBackend extends BukkitConfiguratorBackend {

        public CommandChangerBackend(File file) {
            super(file, YamlConfiguration.loadConfiguration(file));
        }

    }
}
