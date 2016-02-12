package github.therealbuggy.commandchanger.manager.changer;

import org.bukkit.command.Command;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 10/02/16.
 */
public interface IChanger {

    String getId();

    String getSource();

    String getReplacement();

    boolean canReplace(String target);

    String applyReplacement(String value);

    String getConstructClass();

    String getConstructPlugin();

    boolean force();

    Command constructNewCommand(CCommand command);

}
