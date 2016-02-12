package github.therealbuggy.commandchanger.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 09/02/16.
 */
public class BukkitReflection {
    private static final Pattern versionRegex = Pattern.compile("(v|)[0-9][_.][0-9][_.][R0-9]*");

    public static String getServerVersion() {
        return getServerVersion(null);
    }

    public static String getServerVersion(Server server) {
        if (server == null) {
            server = Bukkit.getServer();
        }
        String packag = server.getClass().getPackage().getName();
        String version = packag.substring(packag.lastIndexOf('.') + 1);

        if (!versionRegex.matcher(version).matches()) {
            return "";
        }
        return version;
    }

    public static Package getServerPackage() {
        return getServerPackage(null);
    }

    public static Package getServerPackage(Server server) {
        return Package.getPackage("org.bukkit.craftbukkit." + getServerVersion(server));
    }

    public static Class<?> getServerClass(String fullClassName) {
        return getServerClass(null, fullClassName);
    }

    public static Class<?> getServerClass(Server server, String fullClassName) {

        try {
            return Class.forName(getServerPackage(server).getName() + "." + fullClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void forceDependency(PluginDescriptionFile pluginDescriptionFile, String dependency) {

        if (pluginDescriptionFile.getDepend().contains(dependency))
            return;

        Field field = Reflection.getField(pluginDescriptionFile, "depend");
        Objects.requireNonNull(field, "Cannot force dependency!");

        Object object = Reflection.getFieldValue(pluginDescriptionFile, field);

        if (!(object instanceof List)) {
            throw new RuntimeException("Cannot force dependency!");
        }

        List<String> dependencies = (List<String>) object;

        List<String> newDependencies = new ArrayList<>(dependencies);

        newDependencies.add(dependency);

        Object return_ = Reflection.setField(pluginDescriptionFile, field, newDependencies);
        if (return_ == null)
            throw new RuntimeException("Cannot force dependency!");
    }


    public static PluginCommand commandFrom(String prefixAndName, CommandExecutor executor, Plugin plugin) {
        Constructor<PluginCommand> commandConstructor = Reflection.getConstructor(PluginCommand.class, new Class<?>[]{String.class, Plugin.class});
        PluginCommand pluginCommand = Reflection.construct(commandConstructor, new Object[]{CCommand.getCommandName(prefixAndName), plugin});

        if(pluginCommand != null) {
            pluginCommand.setExecutor(executor);
        }

        return pluginCommand;
    }


}
