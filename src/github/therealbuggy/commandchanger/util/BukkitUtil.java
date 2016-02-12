package github.therealbuggy.commandchanger.util;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Created by jonathan on 10/02/16.
 */
public class BukkitUtil {


    public static boolean isAllPluginEnabled(Server server) {

        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            if (!plugin.isEnabled())
                return false;
        }

        return true;
    }
}
