package github.therealbuggy.commandchanger.api.override;

import org.bukkit.command.Command;

/**
 * Created by jonathan on 10/02/16.
 */
public class RegisterResult {

    private final String label;
    private final Command command;
    private final boolean cancelled;

    public RegisterResult(String label, Command command, boolean cancelled) {
        this.label = label;
        this.command = command;
        this.cancelled = cancelled;
    }

    public Command getCommand() {
        return command;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getLabelOr(String another) {
        return label != null ? label : another;
    }

    public Command getCommandOr(Command another) {
        return command != null ? command : another;
    }

}
