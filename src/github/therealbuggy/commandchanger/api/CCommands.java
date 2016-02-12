package github.therealbuggy.commandchanger.api;

import org.bukkit.command.Command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;

/**
 * Created by jonathan on 11/02/16.
 */
public class CCommands {
    private final Set<CCommand> commands = new HashSet<>();

    public CCommand of(String label, Command command, CommandChangePrevent commandChangePrevent) {
        for (CCommand cCommand : commands) {
            if (cCommand.getLastLabel().equals(label)) {

                if (cCommand.getCommand() != command)
                    cCommand.setCommand(command);

                return cCommand;
            }
        }

        return null;
    }

    public CCommand ofOrNew(String label, Command command, CommandChangePrevent commandChangePrevent) {

        CCommand command0 = of(label, command, commandChangePrevent);

        if (command0 != null)
            return command0;

        commands.add(command0 = new CCommand(label, command, commandChangePrevent));

        return command0;
    }

    public void remove(CCommand command) {
        commands.remove(command);
    }

    public void removeAll() {
        commands.clear();
    }

    public Collection<CCommand> getCommands() {
        return Collections.unmodifiableCollection(commands);
    }
}
