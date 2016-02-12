package github.therealbuggy.commandchanger.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import github.therealbuggy.commandchanger.api.event.CommandRegisterEvent;
import github.therealbuggy.commandchanger.api.prevent.ChangeEnforce;
import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;
import github.therealbuggy.commandchanger.util.Reflection;

/**
 * Created by jonathan on 10/02/16.
 */
@ChangeEnforce
public class ListenerChanger implements CommandChanger {

    private final Listener listener;
    private final CommandChangePrevent prevent;

    public ListenerChanger(Listener listener, CommandChangePrevent prevent) {
        this.listener = listener;
        this.prevent = prevent;
    }

    @Override
    public void receiveCommandRegister(CCommand command) {

        if (listener instanceof RegisteredListener) {
            RegisteredListener registeredListener = (RegisteredListener) listener;
            if (!registeredListener.getPlugin().isEnabled()) {
                return;
            }
        }


        if (prevent.canChange(listener, command.getCommand())) {

            CCommand cCommand = command.copyOfThis();

            CommandRegisterEvent commandRegisterEvent = new CommandRegisterEvent(cCommand);
            Reflection.callMethod(listener, null, null, new Class<?>[]{CommandRegisterEvent.class}, new Object[]{commandRegisterEvent});

            if (!commandRegisterEvent.isCancelled())
                command.apply(cCommand);
        }
    }
}
