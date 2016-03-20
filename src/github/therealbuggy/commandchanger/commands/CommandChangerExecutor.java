package github.therealbuggy.commandchanger.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import github.therealbuggy.commandchanger.CommandChangerPlugin;
import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.api.CommandChangerAPI;
import github.therealbuggy.commandchanger.manager.CommandChangeManager;
import github.therealbuggy.commandchanger.manager.changer.DefaultChanger;
import github.therealbuggy.commandchanger.manager.changer.IChanger;
import github.therealbuggy.commandchanger.manager.changer.RegexChanger;
import github.therealbuggy.commandchanger.manager.remover.RegexRemover;
import github.therealbuggy.commandchanger.manager.remover.Remover;

/**
 * Created by jonathan on 11/02/16.
 */
public class CommandChangerExecutor implements CommandExecutor {

    private final CommandChangerPlugin commandChangerPlugin;

    public CommandChangerExecutor(CommandChangerPlugin commandChangerPlugin) {
        this.commandChangerPlugin = commandChangerPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        String subCmd = args.length > 0 ? args[0] : "";

        if (!commandSender.hasPermission("commandchanger." + subCmd)) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return false;
        }


        if (args.length == 0) {
            view(commandSender, command, label, args);
            return false;
        }

        if (subCmd.equalsIgnoreCase("reload")) {
            reloadCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("list-changed")) {
            listChangedCommands(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("list-changers")) {
            listChangersCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("list-removed")) {
            listRemovedCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("locale")) {
            localeCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("find-command")) {
            findCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("command-info")) {
            commandInfo(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("change")) {
            changeCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("remove-changer")) {
            removeChangerCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("remove-command")) {
            removeCommandCommand(commandSender, command, label, args);
        } else if (subCmd.equalsIgnoreCase("remove-remover")) {
            removeRemoverCommand(commandSender, command, label, args);
        } else {
            view(commandSender, command, label, args);
            return false;
        }


        return false;
    }

    private void view(CommandSender commandSender, Command command, String label, String[] args) {
        commandSender.sendMessage(ChatColor.AQUA + "===" + ChatColor.GREEN + " Commands " + ChatColor.AQUA + "===");
        commandSender.sendMessage(ChatColor.AQUA + "[] = Required, <> = Optional, ... = List");
        sendMessageTo(commandSender, label, "reload - Restore commands, reload config, setup commands.");
        sendMessageTo(commandSender, label, "list-changed - List changed commands.");
        sendMessageTo(commandSender, label, "list-changers - List config changers.");
        sendMessageTo(commandSender, label, "list-removed - List removed commands (regex).");
        sendMessageTo(commandSender, label, "locale - View current locale.");
        sendMessageTo(commandSender, label, "find-command [command] <Regex yes/no> - Find a command.");
        sendMessageTo(commandSender, label, "command-info [command] <Regex yes/no> - Show complete command information, including change history.");
        sendMessageTo(commandSender, label, "change [id] [command] [new command] <Regex yes/no> <Force yes/no> - Change 'command' to 'new_command'.");
        sendMessageTo(commandSender, label, "remove-changer [id] - Remove a config changer.");
        sendMessageTo(commandSender, label, "remove-command [id] [regex]... - Remove a command.");
        sendMessageTo(commandSender, label, "remove-remover [id] - Remove a command 'remover'.");
    }

    private void listChangersCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Collection<IChanger> changers = commandChangerPlugin.getManager().changers();

        if (changers.isEmpty()) {
            commandSender.sendMessage(ChatColor.GREEN + "No changers!");
        }

        for (IChanger changer : changers) {
            showChanger(commandSender, changer);
        }
    }

    private void showChanger(CommandSender commandSender, IChanger changer) {
        String id = changer.getId();
        String from = changer.getSource();
        String to = changer.getReplacement();
        boolean isRegex = changer instanceof RegexChanger;
        boolean force = changer.force();

        commandSender.sendMessage(ChatColor.AQUA + "=================================");
        commandSender.sendMessage(ChatColor.GREEN + "ID: " + ChatColor.AQUA + id);
        commandSender.sendMessage(ChatColor.GREEN + "From: " + ChatColor.AQUA + from);
        commandSender.sendMessage(ChatColor.GREEN + "To: " + ChatColor.AQUA + to);
        commandSender.sendMessage(ChatColor.GREEN + "isRegex: " + ChatColor.AQUA + isRegex);
        commandSender.sendMessage(ChatColor.GREEN + "Force: " + ChatColor.AQUA + force);
        commandSender.sendMessage(ChatColor.AQUA + "=================================");

    }

    private void showRemover(CommandSender commandSender, Remover remover) {
        String id = remover.getId();
        Set<String> regexSet = remover.getSource();

        commandSender.sendMessage(ChatColor.AQUA + "=================================");
        commandSender.sendMessage(ChatColor.GREEN + "ID: " + ChatColor.AQUA + id);
        commandSender.sendMessage(ChatColor.GRAY + " |-" + ChatColor.GREEN + "Regex List ");

        for (String aRegex : regexSet) {
            commandSender.sendMessage(ChatColor.GRAY + " | " + ChatColor.AQUA + aRegex);
        }

        commandSender.sendMessage(ChatColor.AQUA + "=================================");

    }

    private void listRemovedCommand(CommandSender commandSender, Command command, String label, String[] args) {
        CommandChangeManager manager = commandChangerPlugin.getManager();

        for (Remover remover : manager.removers()) {
            showRemover(commandSender, remover);
        }
    }


    private void reloadCommand(CommandSender commandSender, Command command, String label, String[] args) {
        commandChangerPlugin.reload(commandSender);
    }

    private void localeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        commandSender.sendMessage(ChatColor.GREEN + "Current locale: " + ChatColor.AQUA + commandChangerPlugin.getConfigLocale());
    }

    private void removeChangerCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }


        String id = args[1];

        IChanger changer = commandChangerPlugin.getManager().findChangerById(id);

        if (changer != null) {
            commandSender.sendMessage(ChatColor.GRAY + "Removing changer...");
            commandChangerPlugin.getManager().removeChanger(changer);
            commandChangerPlugin.updateManager();
            // TODO
            commandSender.sendMessage(ChatColor.GREEN + "Removed!");
        } else {
            commandSender.sendMessage(ChatColor.RED + "Cannot find command changer '" + id + "'!");
        }
    }

    private void removeRemoverCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }

        String id = args[1];

        Remover remover = commandChangerPlugin.getManager().findRemoverById(id);

        if (remover != null) {
            commandSender.sendMessage(ChatColor.GRAY + "Removing remover...");
            commandChangerPlugin.getManager().removeRemover(remover);
            commandChangerPlugin.updateManager();
            commandSender.sendMessage(ChatColor.GREEN + "Removed!");
        } else {
            commandSender.sendMessage(ChatColor.RED + "Cannot find command remover '" + id + "'!");
        }
    }


    private void removeCommandCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 3) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }

        String id = args[1];
        Set<String> regexList = new HashSet<>();

        for (int x = 2; x < args.length; ++x) {
            String regex = args[x];

            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException ex) {
                commandSender.sendMessage(ChatColor.RED + "Invalid Regex '" + regex + "' at argument index '" + (args.length + 1) + "'!");
                return;
            }

            regexList.add(regex);
        }

        CommandChangeManager manager = commandChangerPlugin.getManager();

        Optional<Remover> opt = manager.removers().stream().filter(r -> r.getId().equals(id)).findAny();

        if (opt.isPresent()) {
            commandSender.sendMessage(ChatColor.GRAY + "Adding to existent remover...");
            opt.get().getSource().addAll(regexList);
            commandSender.sendMessage(ChatColor.GREEN + "Added!");
        } else {
            commandSender.sendMessage(ChatColor.GRAY + "Creating remover...");
            Remover remover = new RegexRemover(id, regexList);
            manager.addRemover(remover);
            commandChangerPlugin.updateManager();
            commandSender.sendMessage(ChatColor.GREEN + "Created!");
        }
    }


    private void changeCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (args.length < 4) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }


        String id = args[1];
        String cmd = args[2];
        String newCmd = args[3];

        boolean regex = false;
        if (args.length >= 5) {
            if (args[4].equals("yes"))
                regex = true;
        }

        boolean force = false;
        if (args.length >= 6) {
            if (args[5].equals("yes"))
                force = true;
        }


        IChanger changer;

        if (regex) {
            changer = new RegexChanger(id, cmd, newCmd, force);
        } else {
            changer = new DefaultChanger(id, cmd, newCmd, force);
        }

        commandSender.sendMessage(ChatColor.GRAY + "Creating changer...");

        commandChangerPlugin.getManager().addChanger(changer);
        commandChangerPlugin.updateManager();

        commandSender.sendMessage(ChatColor.GREEN + "Created! See details below");
        showChanger(commandSender, changer);

    }


    private void commandInfo(CommandSender commandSender, Command command, String label, String[] args) {

        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }


        String cmd = args[1];
        boolean regex = false;
        if (args.length >= 3) {
            if (args[2].equals("yes"))
                regex = true;
        }

        final boolean cregex = regex;

        CommandChangerAPI api = commandChangerPlugin.getCommandChangerAPI();

        List<CCommand> cCommands = api.commands().stream().filter(c -> {
            String commandlabel = c.getLabel();
            return (cregex && commandlabel.matches(cmd))
                    || commandlabel.equalsIgnoreCase(cmd);
        }).collect(Collectors.toList());

        if (cCommands.isEmpty()) {
            commandSender.sendMessage(ChatColor.RED + "Cannot find command '" + cmd + "'. Regex: " + regex);
            return;
        }

        for (CCommand cCommand : cCommands) {
            commandSender.sendMessage(ChatColor.AQUA + "===" + ChatColor.GREEN + cCommand.getOriginalLabel() + ChatColor.AQUA + "===");

            commandSender.sendMessage(ChatColor.GREEN + "Current label: " + ChatColor.AQUA + cCommand.getLastLabel());
            commandSender.sendMessage(ChatColor.GREEN + "Current process class: " + ChatColor.AQUA + name(cCommand.getCommand()));

            commandSender.sendMessage(ChatColor.GREEN + "Original label: " + ChatColor.AQUA + cCommand.getOriginalLabel());

            commandSender.sendMessage(ChatColor.GREEN + "Original processor: " + ChatColor.AQUA + name(cCommand.getOriginalCommand()));

            if (cCommand.getLabelHistory().size() > 1) {
                commandSender.sendMessage(ChatColor.GREEN + "Label history: " + ChatColor.AQUA + cCommand.getLabelHistory());
            }

            if (cCommand.getCommandHistory().size() > 1) {
                commandSender.sendMessage(ChatColor.GREEN + "Command history: " + ChatColor.AQUA + names(cCommand.getCommandHistory()));
            }

            commandSender.sendMessage(ChatColor.AQUA + "===" + ChatColor.GREEN + cCommand.getOriginalLabel() + ChatColor.AQUA + "===");
        }


    }

    private String names(List<Command> obj) {
        List<String> names = new ArrayList<>();

        for (Command command : obj) {
            names.add(name(command));
        }

        return names.toString();
    }

    private String name(Object obj) {

        if (obj instanceof PluginCommand) {
            PluginCommand pluginCommand = (PluginCommand) obj;
            return "[Executor] " + pluginCommand.getExecutor().getClass().getCanonicalName();

        } else {
            return obj.getClass().getCanonicalName();
        }

    }

    private void findCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return;
        }


        String cmd = args[1];
        boolean regex = false;
        if (args.length >= 3) {
            if (args[2].equals("yes"))
                regex = true;
        }

        List<String> found = new ArrayList<>();

        CommandChangerAPI api = commandChangerPlugin.getCommandChangerAPI();

        for (String commandStr : api.commandMapKeys()) {
            if ((regex && commandStr.matches(cmd))
                    || commandStr.equalsIgnoreCase(cmd)) {
                found.add(commandStr);
            }
        }

        if (found.isEmpty()) {
            commandSender.sendMessage(ChatColor.RED + "Cannot find command '" + cmd + "'. Regex: " + regex);
            return;
        }

        String commands = found.toString();
        commands = commands.substring(1, commands.length() - 1);

        commandSender.sendMessage(ChatColor.GREEN + "Find commands: " + ChatColor.AQUA + commands);
    }

    private void listChangedCommands(CommandSender commandSender, Command command, String label, String[] args) {
        CommandChangerAPI api = commandChangerPlugin.getCommandChangerAPI();

        List<String> changedCommands = new ArrayList<>();

        api.commands().stream()
                .filter(c -> !c.getLastLabel().equals(c.getOriginalLabel()))
                .forEach(c -> changedCommands.add(c.getOriginalLabel()));

        String commands = changedCommands.toString();
        commands = commands.substring(1, commands.length() - 1);

        commandSender.sendMessage(ChatColor.GREEN + "Changed commands: " + ChatColor.AQUA + commands + "!");

    }

    private void sendMessageTo(CommandSender sender, String label, String message) {
        sender.sendMessage(ChatColor.RED + "/" + ChatColor.AQUA + label + ChatColor.GREEN + " " + message);
    }

}
