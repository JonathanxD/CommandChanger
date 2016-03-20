package github.therealbuggy.commandchanger;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import github.therealbuggy.bukkitconfigurator.BukkitConfigurator;
import github.therealbuggy.commandchanger.api.CommandChangerAPI;
import github.therealbuggy.commandchanger.api.CommandChangerReload;
import github.therealbuggy.commandchanger.api.services.CommandChangerServices;
import github.therealbuggy.commandchanger.commands.CommandChangerExecutor;
import github.therealbuggy.commandchanger.config.CommandChangerConfigurator;
import github.therealbuggy.commandchanger.config.ConfigLocale;
import github.therealbuggy.commandchanger.config.ConfigTags;
import github.therealbuggy.commandchanger.config.locale.ConfigLocaleTranslator;
import github.therealbuggy.commandchanger.config.locale.PortugueseChangerLocale;
import github.therealbuggy.commandchanger.config.template.ChangerTemplate;
import github.therealbuggy.commandchanger.config.transformer.ChangerTransformer;
import github.therealbuggy.commandchanger.config.transformer.RemoverTransformer;
import github.therealbuggy.commandchanger.listener.CommandChangerListener;
import github.therealbuggy.commandchanger.manager.CommandChangeManager;
import github.therealbuggy.commandchanger.manager.SimpleChangerManager;
import github.therealbuggy.commandchanger.manager.changer.IChanger;
import github.therealbuggy.commandchanger.manager.remover.Remover;
import github.therealbuggy.commandchanger.util.BukkitReflection;
import github.therealbuggy.commandchanger.util.BukkitUtil;
import github.therealbuggy.configurator.IConfigurator;
import github.therealbuggy.configurator.key.Key;
import github.therealbuggy.configurator.nav.In;
import github.therealbuggy.configurator.transformer.TransformedObject;
import github.therealbuggy.configurator.utils.Require;

public class CommandChangerPlugin extends JavaPlugin implements CommandChangerReload {

    private final CommandChangerAPI commandChangerAPI;
    private final CommandChangeManager manager;
    private ConfigLocale configLocale;
    private CommandChangerConfigurator<ConfigTags> changerConfigurator;
    private List<IChanger> changerList = new ArrayList<>();
    private List<Remover> removerList = new ArrayList<>();
    private Listener commandChangeListener;
    private String name;
    private Key<?> configKey;
    private Key<?> blockKey;

    public CommandChangerPlugin() {

        manager = new SimpleChangerManager();

        getLogger().info("Creating CommandChangerAPI instance and trying to change fields, please wait...");
        getLogger().info("Criando uma instancia da API de mudanças de comandos e tentando mudar variaveis, favor aguardar...");
        commandChangerAPI = new CommandChangerAPI(getServer());
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling...");

        getLogger().info("Configuring...");

        commandChangerAPI.registerCommandExecutor("commandchanger", new CommandChangerExecutor(this), this);

        getLogger().info("Configuration complete!");

        getLogger().info("Registering listener...");
        getServer().getPluginManager().registerEvents(commandChangeListener = new CommandChangerListener(manager), this);

        name = this.getDescription().getName();

        getServer().getServicesManager().register(CommandChangerServices.COMMAND_CHANGE_API, commandChangerAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(CommandChangerServices.COMMAND_CHANGE_PREVENT, commandChangerAPI.getPrevent(), this, ServicePriority.Normal);

        getLogger().info("Enabled!");

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                postLoad();
            }
        });
    }

    @Override
    public void onDisable() {
        changerConfigurator = null;
        changerList.clear();
        manager.clearChangers();
        HandlerList.unregisterAll(this);
    }

    private void setupConfig() {
        changerList.clear();
        removerList.clear();
        manager.clearChangers();
        manager.clearRemovers();

        saveDefaultConfig();
        loadFromFile();
        doDirectivesLoad();
    }

    private void loadFromFile() {

        changerConfigurator = null;

        File configFile = new File(getDataFolder(), "config.yml");
        changerConfigurator = new CommandChangerConfigurator<>(configFile, this);

        //changerConfigurator.getBackend().getData().registerData(new ChangerTemplate());

        changerConfigurator.setKeyAlias(ConfigTags.LANGUAGE, "locale", new ConfigLocaleTranslator(changerConfigurator));

        changerConfigurator.getTransformerHandler().addTransformer(new ChangerTransformer(changerConfigurator));
        changerConfigurator.getTransformerHandler().addTransformer(new RemoverTransformer(changerConfigurator));

        Key<?> key = changerConfigurator.getValue(ConfigTags.LANGUAGE, In.main());

        String changeKey = "change";
        String blockKey = "block";

        if (key != null) {

            configLocale = key.<ConfigLocale>getValue().getValue();
            if (configLocale == ConfigLocale.PT) {
                changeKey = "mudar";
                blockKey = "bloquear";

                Require.require(changerConfigurator.getBackend()
                        .extraData()
                        .getDataAssignable(ChangerTemplate.class)) // End of Require
                        .getLocaleList()
                        .addLocale(new PortugueseChangerLocale());
                getLogger().info("Changed language to PT-BR");
            }
        }


        this.configKey = changerConfigurator.setSectionAlias(ConfigTags.CHANGE, changeKey);
        this.blockKey = changerConfigurator.setSectionAlias(ConfigTags.BLOCK, blockKey);
    }

    public void updateManager() {
        changerConfigurator.constructSection(configKey, ChangerTransformer.REFERENCE.but().hold(getManager().changers()).build());
        changerConfigurator.constructSection(blockKey, RemoverTransformer.REFERENCE.but().hold(getManager().removers()).build());

        changerConfigurator.getBackend().save();
        reload(changerConfigurator);
    }

    private static void reload(IConfigurator<?> iConfigurator) {
        if (iConfigurator instanceof BukkitConfigurator) {
            BukkitConfigurator bukkitConfigurator = (BukkitConfigurator) iConfigurator;
            Plugin plugin = bukkitConfigurator.getPlugin();

            bukkitConfigurator.getPlugin().reloadConfig();

            if (plugin instanceof CommandChangerReload) {
                CommandChangerReload reload = (CommandChangerReload) plugin;
                reload.doConfigReload();
            }
        }
    }

    private void doDirectivesLoad() {

        changerList.forEach(manager::removeChanger);

        Optional<TransformedObject<List<IChanger>>> transformedSectionOpt = changerConfigurator.getTransformedSection(In.path(ConfigTags.CHANGE), ChangerTransformer.REFERENCE);
        if (transformedSectionOpt.isPresent()) {
            TransformedObject<List<IChanger>> changerTransformed = transformedSectionOpt.get();

            if (changerTransformed.getTransformer() != ChangerTransformer.class) {
                throw new RuntimeException("Error in section transform process!");
            }

            changerList = changerTransformed.getObject();
            changerList.forEach(manager::addChanger);
            if (!changerList.isEmpty())
                getLogger().info("Changed " + changerList.size() + " commands. You can type '/commandchanger list-changers' to list all changed commands!");

        } else {
            getLogger().severe("Cannot transform section, are you reloading the plugin? Don't do that!");
        }

        removerList.forEach(manager::removeRemover);

        Optional<TransformedObject<List<Remover>>> transformedRemoverSectOpt = changerConfigurator.getTransformedSection(In.path(ConfigTags.BLOCK), RemoverTransformer.REFERENCE);


        if(transformedRemoverSectOpt.isPresent()) {
            TransformedObject<List<Remover>> removerTransformed = transformedRemoverSectOpt.get();

            if(removerTransformed.getTransformer() != RemoverTransformer.class) {
                throw new RuntimeException("Error in section transform process!");
            }

            removerList = removerTransformed.getObject();

            removerList.forEach(manager::addRemover);

            if (!removerList.isEmpty())
                getLogger().info("Removed " + removerList.size() + " commands. You can type '/commandchanger list-removed' to list all removed commands!");


        }else{
            getLogger().severe("Cannot transform section, are you reloading the plugin? Don't do that!");
        }

    }

    private void postLoad() {
        int force = 0;

        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            if (plugin != this) {
                PluginDescriptionFile pluginDescriptionFile = plugin.getDescription();
                if (!pluginDescriptionFile.getDepend().contains(name)) {
                    ++force;
                    BukkitReflection.forceDependency(pluginDescriptionFile, name);
                }
            }
        }

        if (force > 0) {
            getLogger().info("'BukkitCommandChanger' dependency forced for " + force + " plugins!");
        }

        while (!BukkitUtil.isAllPluginEnabled(getServer())) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        setupConfig();

        commandChangerAPI.processListener(commandChangeListener);

    }

    private void refresh() {
        commandChangerAPI.refresh();
        commandChangerAPI.processListener(commandChangeListener);
    }

    public void reload(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.GREEN + "Reloading config...");
        doConfigReload();
        commandSender.sendMessage(ChatColor.GREEN + "Config reloaded!");
    }

    @Override
    public void doConfigReload() {
        reloadConfig();

        setupConfig();

        refresh();
    }

    public CommandChangeManager getManager() {
        return manager;
    }

    public CommandChangerAPI getCommandChangerAPI() {
        return commandChangerAPI;
    }

    public ConfigLocale getConfigLocale() {
        return configLocale;
    }
}
