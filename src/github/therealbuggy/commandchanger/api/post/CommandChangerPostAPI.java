package github.therealbuggy.commandchanger.api.post;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import github.therealbuggy.commandchanger.api.CommandChanger;
import github.therealbuggy.commandchanger.api.CommandChangerAPI;

/**
 * Created by jonathan on 10/02/16.
 */
public class CommandChangerPostAPI {

    private final CommandChangerAPI commandChangerAPI;

    public CommandChangerPostAPI(CommandChangerAPI commandChangerAPI) {
        this.commandChangerAPI = commandChangerAPI;
    }

    public void handle(CommandChanger commandChanger, Plugin plugin) {
        commandChangerAPI.processAll(commandChanger);
        commandChangerAPI.addChanger(commandChanger, plugin);
    }

    public void handle(Listener listener) {
        commandChangerAPI.processListener(listener);
    }
}
