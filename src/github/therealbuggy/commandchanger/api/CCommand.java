package github.therealbuggy.commandchanger.api;

import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;

/**
 * Created by jonathan on 10/02/16.
 */
public class CCommand implements Comparable<String>, Cloneable {
    private final CommandChangePrevent commandChangePrevent;
    private final List<String> labelHistory = new ArrayList<>();
    private final List<Command> commandHistory = new ArrayList<>();
    private String label;
    private Command command;

    CCommand(String label, Command command, CommandChangePrevent commandChangePrevent) {

        labelHistory.add(label);
        commandHistory.add(command);

        this.label = label;
        this.command = command;
        this.commandChangePrevent = commandChangePrevent;
    }

    public static String getCommandName(String prefixAndLabel) {
        if (!prefixAndLabel.contains(":"))
            return prefixAndLabel;

        return prefixAndLabel.substring(prefixAndLabel.indexOf(":") + 1);
    }

    public static String getFallbackPrefix(String prefixAndLabel) {
        if (!prefixAndLabel.contains(":"))
            return prefixAndLabel;

        return prefixAndLabel.substring(0, prefixAndLabel.indexOf(":"));
    }


    public CommandChangePrevent prevent() {
        return commandChangePrevent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {

        if (label != null && !label.equals(this.label))
            labelHistory.add(label);

        this.label = label;
    }

    public String getLastLabel() {
        return this.labelHistory.get(this.labelHistory.size() - 1);
    }

    public String getOriginalLabel() {
        return this.labelHistory.get(0);
    }

    public Command getOriginalCommand() {
        return this.commandHistory.get(0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getOriginalLabel());
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {

        if (command != this.command)
            commandHistory.add(command);

        this.command = command;
    }

    public void setCommandName(String name, boolean removeFallbackPrefix) {
        if (removeFallbackPrefix) {
            setLabel(name);
        } else {
            setLabel(getFallbackPrefix() + name);
        }
    }

    public String getCommandName() {
        return getCommandName(label);
    }

    public String getFallbackPrefix() {
        return getFallbackPrefix(label);
    }

    @Override
    public int compareTo(String o) {
        if (o == null)
            return -1;

        if (o.equals(getLabel())) {
            return 0;
        }
        if (o.equals(getCommandName()))
            return 1;
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof String)) {
            return false;
        }
        return compareTo((String) obj) == 0;
    }

    public void apply(CCommand cCommand) {
        this.label = cCommand.label;
        this.command = cCommand.command;
        this.labelHistory.addAll(cCommand.labelHistory);
        this.commandHistory.addAll(cCommand.commandHistory);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() {
        return new CCommand(label, command, commandChangePrevent);
    }

    public CCommand copyOfThis() {
        return (CCommand) clone();
    }

    @Override
    public String toString() {
        return "CCommand{label=" + label + ", command=" + command + "}";
    }

    public boolean isChangePrevent() {
        return this.commandChangePrevent.isPrevent(this.command);
    }

    public boolean isEnforcing(Object changingHandler) {
        return this.commandChangePrevent.isEnforcing(changingHandler);
    }

    public List<Command> getCommandHistory() {
        return Collections.unmodifiableList(commandHistory);
    }

    public List<String> getLabelHistory() {
        return Collections.unmodifiableList(labelHistory);
    }
}
