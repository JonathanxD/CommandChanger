package github.therealbuggy.commandchanger.api.prevent;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 10/02/16.
 */
public class CommandChangePrevent {

    Set<PreventElement> preventElements = new HashSet<>();


    public void preventCommand(Command command) {
        preventElements.add(new PreventElement(command.getClass()));
    }

    public void preventAssignable(Command command) {
        preventElements.add(new PreventElement(command.getClass(), true));
    }

    public void preventAssignable(CommandExecutor executor) {
        preventElements.add(new PreventElement(executor.getClass(), true));
    }

    public void preventExecutor(CommandExecutor executor) {
        preventElements.add(new PreventElement(executor.getClass()));
    }

    public boolean isPrevent(Command command) {
        ChangePrevent changePrevent = command.getClass().getAnnotation(ChangePrevent.class);

        if (changePrevent != null) {
            return false;
        }

        if (command instanceof PluginCommand) {
            PluginCommand pluginCommand = (PluginCommand) command;

            if (pluginCommand.getExecutor().getClass().getAnnotation(ChangePrevent.class) != null) {
                return true;
            }
        }

        for (PreventElement preventElement : preventElements) {
            if (preventElement.compareTo(command.getClass()) == 0)
                return true;

            if (command instanceof PluginCommand) {
                PluginCommand pluginCommand = (PluginCommand) command;
                if (preventElement.compareTo(pluginCommand.getExecutor().getClass()) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEnforcing(Object changeSource) {
        return changeSource.getClass().getAnnotation(ChangeEnforce.class) != null;
    }

    public boolean canChange(Object changeSource, Command command) {
        return isEnforcing(changeSource) || !isPrevent(command);

    }

    /**
     * Modified from {@link org.bukkit.plugin.SimplePluginManager#fireEvent(Event)}
     */

    public void fireCheck(Server server, Event event, Command check) {

        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (registration.getPlugin().isEnabled()) {
                try {
                    if (canChange(registration.getListener(), check)) {
                        registration.callEvent(event);
                    }
                } catch (AuthorNagException ex) {
                    Plugin plugin = registration.getPlugin();
                    if (plugin.isNaggable()) {
                        plugin.setNaggable(false);
                        server.getLogger().log(Level.SEVERE, String.format("Nag author(s): \'%s\' of \'%s\' about the following: %s", new Object[]{plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), ex.getMessage()}));
                    }
                } catch (Throwable th) {
                    server.getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), th);
                }
            }
        }

    }
}
