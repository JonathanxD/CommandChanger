package github.therealbuggy.commandchanger.api.services;

import github.therealbuggy.commandchanger.api.CommandChangerAPI;
import github.therealbuggy.commandchanger.api.prevent.CommandChangePrevent;

/**
 * Created by jonathan on 10/02/16.
 */
public class CommandChangerServices {

    public static final Class<CommandChangerAPI> COMMAND_CHANGE_API = CommandChangerAPI.class;
    public static final Class<CommandChangePrevent> COMMAND_CHANGE_PREVENT = CommandChangePrevent.class;

}
