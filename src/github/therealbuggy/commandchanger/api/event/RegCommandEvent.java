package github.therealbuggy.commandchanger.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 10/02/16.
 */
public abstract class RegCommandEvent extends Event implements Cancellable {
    private final CCommand cCommand;
    private boolean cancelled;

    protected RegCommandEvent(CCommand cCommand) {
        this.cCommand = cCommand;
    }

    public CCommand getCCommand() {
        return cCommand;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
