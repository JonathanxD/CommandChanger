package github.therealbuggy.commandchangerexample;

import org.bukkit.event.EventHandler;

import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.api.event.CommandRegisterEvent;
import github.therealbuggy.commandchanger.api.event.CommandUnregisterEvent;

/**
 * Created by jonathan on 10/02/16.
 */
public class MyListener implements org.bukkit.event.Listener {

    @EventHandler
    public void handleRegister(CommandRegisterEvent commandRegisterEvent) {
        CCommand cCommand = commandRegisterEvent.getCCommand();
        if (cCommand.getCommandName().equals("more")) {
            cCommand.setCommandName("+", false);
        }
    }

    @EventHandler
    public void handleUnregister(CommandUnregisterEvent commandUnregisterEvent) {
        CCommand cCommand = commandUnregisterEvent.getCCommand();
        if (cCommand.getCommandName().equals("plm")) {
            // Will cancel command unregister
            // May break some things
            commandUnregisterEvent.setCancelled(true);
        }
    }

}
