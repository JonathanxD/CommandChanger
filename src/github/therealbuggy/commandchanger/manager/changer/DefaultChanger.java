package github.therealbuggy.commandchanger.manager.changer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.util.BukkitReflection;

/**
 * Created by jonathan on 10/02/16.
 */
public class DefaultChanger implements IChanger {

    private final String id;
    private final String source;
    private final String replacement;
    private final boolean force;

    public DefaultChanger(String id, String source, String replacement, boolean force) {
        this.id = id;
        this.source = source;
        this.replacement = replacement;
        this.force = force;
    }

    public DefaultChanger(String source, String replacement, boolean force) {
        this(null, source, replacement, force);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSource() {
        return this.source;
    }

    @Override
    public String getReplacement() {
        return this.replacement;
    }

    @Override
    public boolean canReplace(String target) {
        return applyReplacement(target) != null;
    }

    @Override
    public String applyReplacement(String prefixAndLabel) {
        if (source.equals(prefixAndLabel))
            return source;

        if (CCommand.getFallbackPrefix(source).equals("*")) {

            String name = CCommand.getCommandName(source);
            String prefixName = CCommand.getCommandName(prefixAndLabel);
            String fallback = CCommand.getFallbackPrefix(prefixAndLabel);

            if (name.equals(prefixAndLabel)
                    || name.equals(prefixName)) {

                return (fallback != null && fallback.trim().isEmpty() ? fallback + ":" : "") + replacement;
            }
        }

        return null;
    }

    @Override
    public Command constructNewCommand(CCommand ccommand) {

        String clazz = getConstructClass();

        if (clazz == null)
            return null;

        String plugin = getConstructPlugin();

        if (plugin == null)
            return null;
        try {
            Class<?> aClass = Class.forName(clazz);

            if (!CommandExecutor.class.isAssignableFrom(aClass)
                    || !Command.class.isAssignableFrom(aClass)) {
                throw new RuntimeException("Cannot setup class " + clazz + "! This class isn't a CommandExecutor or Command");
            }

            Command command = null;

            if (aClass.getConstructor() != null) {

                if (CommandExecutor.class.isAssignableFrom(aClass)) {
                    CommandExecutor commandExecutor;
                    commandExecutor = (CommandExecutor) aClass.newInstance();

                    Plugin plg = Bukkit.getPluginManager().getPlugin(plugin);
                    Objects.requireNonNull(plg, "Plugin cannot be found!");

                    command = BukkitReflection.commandFrom(ccommand.getLabel(), commandExecutor, plg);
                } else if (Command.class.isAssignableFrom(aClass)) {
                    command = (Command) aClass.newInstance();

                }
            }

            if (command != null) {
                return command;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getConstructClass() {
        return null;
    }

    @Override
    public String getConstructPlugin() {
        return null;
    }

    @Override
    public boolean force() {
        return this.force;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + ": {from: " + getSource() + ", to: " + getReplacement() + ", force: "+force+"} ]";
    }
}
