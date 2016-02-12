package github.therealbuggy.commandchangerexample;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by jonathan on 10/02/16.
 */
public class BucketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            player.getInventory().addItem(new ItemStack(Material.BUCKET));
        }

        return false;
    }
}
