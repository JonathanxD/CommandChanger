package github.therealbuggy.commandchanger.api;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import github.therealbuggy.commandchanger.api.override.BukkitCommandsMap;
import github.therealbuggy.commandchanger.api.post.CommandChangerPostAPI;
import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;
import github.therealbuggy.commandchanger.util.BukkitReflection;
import github.therealbuggy.commandchanger.util.Reflection;

/**
 * Created by jonathan on 09/02/16.
 */
public class CommandChangerAPI {

    private final Server serverInstance;
    private final Map<String, Command> controlMap;
    private final CommandChangerPostAPI commandChangerPostAPI;
    private final CommandChangePrevent commandChangePrevent = new CommandChangePrevent();
    private Map<String, Command> knownCommands;
    private SimpleCommandMap simpleCommandMap;
    private Field kcmdsField;


    public CommandChangerAPI(Server server) {
        controlMap = new BukkitCommandsMap(server, commandChangePrevent);
        serverInstance = server;
        setupSimpleCommandMap(server);
        setupKnownCommands(server);
        commandChangerPostAPI = new CommandChangerPostAPI(this);
    }

    private void setupSimpleCommandMap(Server server) {
        Object scm = Reflection.getFieldValue(server, "commandMap");

        if (check(scm, SimpleCommandMap.class)) {
            simpleCommandMap = (SimpleCommandMap) scm;
        } else {

            scm = Reflection.getFieldValue(server, SimpleCommandMap.class);

            if (check(scm, SimpleCommandMap.class)) {
                simpleCommandMap = (SimpleCommandMap) scm;
            }
        }
        Objects.requireNonNull(simpleCommandMap, "Cannot setup simpleCommandMap!");
    }

    @SuppressWarnings("unchecked")
    private void setupKnownCommands(Server server) {
        kcmdsField = Reflection.getField(simpleCommandMap, "knownCommands");
        Object kcmds = Reflection.getFieldValue(simpleCommandMap, kcmdsField);

        if (check(kcmds, Map.class)) {
            knownCommands = (Map<String, Command>) kcmds;
        } else {

            kcmdsField = Reflection.getField(simpleCommandMap, Map.class);
            kcmds = Reflection.getFieldValue(simpleCommandMap, kcmdsField);

            if (check(kcmds, Map.class)) {
                knownCommands = (Map<String, Command>) kcmds;
            }
        }

        Objects.requireNonNull(knownCommands, "Cannot setup knownCommands!");

        if (!knownCommands.isEmpty()) {
            controlMap.putAll(knownCommands);
        }

        Map<String, Command> setResult = setMap(controlMap);
        Objects.requireNonNull(setResult, "Cannot define field!");
    }

    private Map<String, Command> setMap(Map<String, Command> commandMap) {
        return Reflection.setFinalStatic(simpleCommandMap, kcmdsField, commandMap);
    }

    public boolean check(Object object, Class<?> type) {
        return object != null && type != null && type.isInstance(object);
    }

    private BukkitCommandsMap getBukkitCommandsMap() {
        return (BukkitCommandsMap) controlMap;
    }

    public void registerCommand(String prefixAndName, Command command) {
        if (command != null) {
            command.register(simpleCommandMap);
            getBukkitCommandsMap().put(prefixAndName, command);
        }
    }

    public PluginCommand registerCommandExecutor(String prefixAndName, CommandExecutor executor, Plugin plugin) {

        PluginCommand pluginCommand = BukkitReflection.commandFrom(prefixAndName, executor, plugin);

        if (pluginCommand != null) {
            pluginCommand.register(simpleCommandMap);
            getBukkitCommandsMap().put(prefixAndName, pluginCommand);
        }

        return pluginCommand;
    }

    public void unregisterCommand(String prefixAndName) {
        getBukkitCommandsMap().remove(prefixAndName);
    }

    public void unregisterAllCommand(Command command) {

        getBukkitCommandsMap().remove(command);
    }

    public void putIfAbsent(String prefixAndName, Command command) {
        getBukkitCommandsMap().putIfAbsent(prefixAndName, command);
    }

    public void putAll(Map<? extends String, ? extends Command> m) {
        getBukkitCommandsMap().putAll(m);
    }

    public void forEach(BiConsumer<? super String, ? super Command> biConsumer) {
        getBukkitCommandsMap().forEach(biConsumer);
    }

    public void unregisterCommand(String prefixAndName, Command command) {
        getBukkitCommandsMap().remove(prefixAndName, command);
    }

    public void addChanger(CommandChanger commandChanger, Plugin plugin) {
        getBukkitCommandsMap().addChanger(plugin, commandChanger);
    }

    public void removeChanger(CommandChanger commandChanger, Plugin plugin) {
        getBukkitCommandsMap().removeChanger(plugin, commandChanger);
    }

    public void removeAllChangers(Plugin plugin) {
        getBukkitCommandsMap().removeAllChangers(plugin);
    }

    public void forceRemove(String prefixAndName) {
        getBukkitCommandsMap().forceRemove(prefixAndName);
    }

    public void processAll(CommandChanger commandChanger) {
        getBukkitCommandsMap().processAll(commandChanger);
    }

    public void processListener(Listener listener) {
        getBukkitCommandsMap().processListener(listener);
    }

    public void refresh() {
        getBukkitCommandsMap().restore();
    }

    public Collection<String> commandMapKeys() {
        return Collections.unmodifiableCollection(getBukkitCommandsMap().keySet());
    }

    public Collection<CCommand> commands() {
        return getBukkitCommandsMap().getCCommands().getCommands();
    }

    public CommandChangerPostAPI postAPI() {
        return commandChangerPostAPI;
    }

    public CommandChangePrevent getPrevent() {
        return commandChangePrevent;
    }

    public Server getServer() {
        return serverInstance;
    }
}

