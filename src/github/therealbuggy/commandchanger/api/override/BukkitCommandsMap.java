package github.therealbuggy.commandchanger.api.override;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.api.CCommands;
import github.therealbuggy.commandchanger.api.CommandChanger;
import github.therealbuggy.commandchanger.api.ListenerChanger;
import github.therealbuggy.commandchanger.api.event.CommandRegisterEvent;
import github.therealbuggy.commandchanger.api.event.CommandUnregisterEvent;
import github.therealbuggy.commandchanger.api.event.RegCommandEvent;
import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;

/**
 * Created by jonathan on 09/02/16.
 */
public class BukkitCommandsMap extends HashMap<String, Command> {

    private final Server server;
    private final CommandChangePrevent commandChangePrevent;
    private final Map<Plugin, List<CommandChanger>> changers = new HashMap<>();
    private final CCommands cCommands = new CCommands();

    public BukkitCommandsMap(Server server, CommandChangePrevent commandChangePrevent) {
        this.server = server;
        this.commandChangePrevent = commandChangePrevent;
    }

    public void addChanger(Plugin plugin, CommandChanger commandChanger) {
        add(plugin, commandChanger);
    }

    public void reprocess() {
        Map<String, Command> map = new HashMap<>();
    }

    private void add(Plugin plugin, CommandChanger commandChanger) {
        if (!changers.containsKey(plugin)) {
            changers.put(plugin, new ArrayList<>());
        }

        changers.get(plugin).add(commandChanger);
    }

    private void remove(Plugin plugin, CommandChanger commandChanger) {
        if (!changers.containsKey(plugin)) {
            return;
        }

        List<CommandChanger> list = changers.get(plugin);

        list.remove(commandChanger);

        if (list.isEmpty())
            changers.remove(plugin);
    }

    public void removeChanger(Plugin plugin, CommandChanger commandChanger) {
        remove(plugin, commandChanger);
    }

    public void removeAllChangers(Plugin plugin) {
        changers.remove(plugin);
    }

    public void processAll(CommandChanger commandChanger) {

        Map<String, Command> newMap = new HashMap<>();

        Iterator<Entry<String, Command>> iter = this.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Command> entry = iter.next();

            String label = entry.getKey();
            Command command = entry.getValue();

            CCommand cCommand = cCommands.of(label, command, this.commandChangePrevent);

            Objects.requireNonNull(cCommand, "Cannot find command: " + label);

            if (!check(commandChanger, command)) {

                continue;
            }

            commandChanger.receiveCommandRegister(cCommand);

            if (cCommand.getLabel() != null && !cCommand.getLabel().equals(label)) {
                label = cCommand.getLabel();
            }

            command = cCommand.getCommand() != null ? cCommand.getCommand() : command;

            iter.remove();
            newMap.put(label, command);

        }

        this.putAll(newMap);

    }

    public boolean check(Object changeSource, Command command) {
        return commandChangePrevent.canChange(changeSource, command);
    }

    public void processListener(Listener listener) {
        processAll(new ListenerChanger(listener, this.commandChangePrevent));
    }

    private Set<CommandChanger> getChangers() {

        Set<CommandChanger> set = new HashSet<>();

        changers.values().forEach(set::addAll);

        return set;
    }

    @Override
    public Command put(String key, Command value) {

        RegisterResult registerResult = rHandle(key, value, RegisterType.REGISTER);

        key = registerResult.getLabelOr(key);
        value = registerResult.getCommandOr(value);

        CCommand command = cCommands.of(key, value, this.commandChangePrevent);

        Objects.requireNonNull(command, "Failed to handle command!");

        Command tmp;
        if(containsKey(key) && (tmp = get(key)) == value) {

            return tmp;
        }

        return super.put(key, value);
    }


    private RegisterResult rHandle(String key, Command value, RegisterType registerType) {
        CCommand cCommand = cCommands.ofOrNew(key, value, this.commandChangePrevent);

        CCommand safe = cCommand.copyOfThis();

        RegCommandEvent regCommandEvent;

        if (registerType == RegisterType.REGISTER) {
            regCommandEvent = new CommandRegisterEvent(safe);
        } else {
            regCommandEvent = new CommandUnregisterEvent(safe);
        }

        commandChangePrevent.fireCheck(server, regCommandEvent, value);

        if (!regCommandEvent.isCancelled())
            cCommand.apply(safe);


        for (CommandChanger commandChanger : getChangers()) {
            if (!check(commandChanger, value)) {
                continue;
            }
            commandChanger.receiveCommandRegister(cCommand);
        }

        if (cCommand.getLabel() != null)
            key = cCommand.getLabel();
        if (cCommand.getCommand() != null)
            value = cCommand.getCommand();

        return new RegisterResult(key, value, regCommandEvent.isCancelled());
    }

    @Override
    public boolean remove(Object key, Object value) {

        if (!(key instanceof String))
            throw new RuntimeException("Invalid key element, class hacking?");
        if (!(value instanceof Command))
            throw new RuntimeException("Invalid value element, class hacking?");

        if (!this.get(key).equals(value))
            return false;

        RegisterResult registerResult = rHandle((String) key, (Command) value, RegisterType.UNREGISTER);

        if (registerResult.isCancelled()) {
            return false;
        }

        key = registerResult.getLabelOr((String) key);
        value = registerResult.getCommandOr((Command) value);

        CCommand command = cCommands.of((String) key, (Command) value, this.commandChangePrevent);

        Objects.requireNonNull(command, "Cannot handle command");

        cCommands.remove(command);

        return super.remove(key, value);
    }

    @Override
    public Command remove(Object key) {
        if (!(key instanceof String))
            throw new RuntimeException("Invalid key element, class hacking?");

        if (!this.containsKey(key))
            return null;

        RegisterResult registerResult = rHandle((String) key, this.get(key), RegisterType.UNREGISTER);

        if (registerResult.isCancelled()) {
            return null;
        }

        key = registerResult.getLabelOr((String) key);

        CCommand command = cCommands.of((String) key, this.get(key), this.commandChangePrevent);

        Objects.requireNonNull(command, "Cannot handle command");

        cCommands.remove(command);

        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Command> m) {
        for (Entry<? extends String, ? extends Command> entry : m.entrySet()) {
            // Put not changed
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Command, ? extends Command> function) {

        Map<String, Command> current = new HashMap<>(this);

        for (Entry<String, Command> entry : this.entrySet()) {
            String key = entry.getKey();
            Command value = entry.getValue();

            Command otherValue = function.apply(key, value);

            if (otherValue != value) {

                if (!current.remove(key, value)) {
                    continue;
                }

                current.put(key, otherValue);
            }
        }
        this.clear();
        this.putAll(current);
    }

    @Override
    public boolean replace(String key, Command oldValue, Command newValue) {

        if (!this.remove(key, oldValue))
            return false;

        this.put(key, newValue);

        return true;
    }

    @Override
    public Command putIfAbsent(String key, Command value) {
        if (!this.containsKey(key))
            return null;

        if (this.get(key) != null)
            return this.get(key);

        return this.put(key, value);
    }

    public void forceRemove(String commandKey) {
        Iterator<Entry<String, Command>> iter = this.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Command> entry = iter.next();
            if (entry.getKey().equals(commandKey)) {
                iter.remove();
            }
        }

    }

    public void remove(Command command) {
        Iterator<Entry<String, Command>> iter = this.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Command> entry = iter.next();
            if (entry.getValue().equals(command)) {
                iter.remove();
            }
        }

    }

    public void restore() {

        Map<String, Command> commandMap = new HashMap<>(this);

        for (Map.Entry<String, Command> entry : this.entrySet()) {

            CCommand command = cCommands.of(entry.getKey(), entry.getValue(), commandChangePrevent);
            if (command != null) {
                if(!entry.getKey().equals(command.getOriginalLabel())) {
                    commandMap.put(command.getOriginalLabel(), command.getOriginalCommand());
                }
            }

        }

        cCommands.removeAll();
        this.clear();
        this.putAll(commandMap);
    }

    public CCommands getCCommands() {
        return cCommands;
    }
}
