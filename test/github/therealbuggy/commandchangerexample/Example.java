package github.therealbuggy.commandchangerexample;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import github.therealbuggy.commandchanger.api.CCommand;
import github.therealbuggy.commandchanger.api.CommandChanger;
import github.therealbuggy.commandchanger.api.CommandChangerAPI;
import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;

/**
 * Created by jonathan on 10/02/16.
 */
public class Example extends JavaPlugin {

    CommandChangerAPI commandChangerAPI;
    Listener myListener;

    @Override
    public void onEnable() {
        // LOAD PHASE = STARTUP AND DEPEND = [BukkitCommandChanger,..]

        RegisteredServiceProvider<CommandChangerAPI> commandChangerProvider = getServer().getServicesManager().getRegistration(CommandChangerAPI.class);

        //... CHECK SERVICE

        // Set
        commandChangerAPI = commandChangerProvider.getProvider();


        // You can handle command registration via CommandChanger or via Listener handling the CommandRegisterEvent

        myListener = new MyListener();

        commandChangerAPI.addChanger(new CommandChanger() {
            @Override
            public void receiveCommandRegister(CCommand command) {
                if(command.getCommandName().equals("tp"))
                    command.setCommandName("teleport", false);
            }
        }, this);

        // LOAD PHASE = POST_WORLD (default) OR SoftDepende = [BukkitCommandChanger, ...]

        // Will loop all setted commands
        commandChangerAPI.postAPI().handle(new CommandChanger() {
            @Override
            public void receiveCommandRegister(CCommand command) {
                if(command.getCommandName().equals("tp"))
                    command.setCommandName("teleport", false);
            }
        }, this);

        // Or send commands to listener

        commandChangerAPI.postAPI().handle(myListener);

        // Prevent command changing

        // getCommand("bucket").setExecutor(new BucketCommand());

        // Or
        CommandExecutor commandExecutor = new BucketCommand();
        PluginCommand pluginCommand = commandChangerAPI.registerCommandExecutor("bucket", commandExecutor, this);
        pluginCommand.setPermission("admin.give.bucket");

        RegisteredServiceProvider<CommandChangePrevent> commandChangePreventProvider = getServer().getServicesManager().getRegistration(CommandChangePrevent.class);

        if(commandChangePreventProvider != null) {
            CommandChangePrevent commandChangePrevent = commandChangePreventProvider.getProvider();

            commandChangePrevent.preventExecutor(commandExecutor);
            // Or
            //commandChangePrevent.preventCommand(pluginCommand);
            // Or You can annotate CommandExecutor with @ChangePrevent
        }

    }
}
