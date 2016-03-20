package github.therealbuggy.commandchanger.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.function.Supplier;

import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.api.event.CommandRegisterEvent;
import github.therealbuggy.commandchanger.api.prevent.ChangeEnforce;
import github.therealbuggy.commandchanger.manager.CommandChangeManager;

/**
 * Created by jonathan on 10/02/16.
 */
@ChangeEnforce
public class CommandChangerListener implements Listener {

    private final CommandChangeManager manager;

    public CommandChangerListener(CommandChangeManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void commandRegisterHandler(CommandRegisterEvent event) {
        CCommand cCommand = event.getCCommand();

        if (manager.isCommandRemoved(cCommand.getLabel())) {
            event.setCancelled(true);
        }

        if (manager.isCommandChanged(cCommand.getLabel())) {

            Supplier<String> label = cCommand::getLabel;

            manager.getCommandChanged(label, (changed, changer) -> {
                if(cCommand.isChangePrevent() && !changer.force()) {
                    return;
                }
                if (changed != null) {
                    if(!changed.equals(cCommand.getOriginalLabel())) {
                        cCommand.setLabel(changed);
                    }
                }
            });
        }

    }

}
