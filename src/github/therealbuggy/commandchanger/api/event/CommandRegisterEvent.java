package github.therealbuggy.commandchanger.api.event;

import org.bukkit.event.HandlerList;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 09/02/16.
 */
public class CommandRegisterEvent extends RegCommandEvent {

    private static final HandlerList handlers = new HandlerList();

    public CommandRegisterEvent(CCommand cCommand) {
        super(cCommand);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }



    public HandlerList getHandlers() {
        return handlers;
    }

}
