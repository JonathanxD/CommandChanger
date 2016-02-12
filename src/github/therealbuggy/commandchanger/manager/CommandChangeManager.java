package github.therealbuggy.commandchanger.manager;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import github.therealbuggy.commandchanger.manager.changer.IChanger;

/**
 * Created by jonathan on 10/02/16.
 */
public interface CommandChangeManager {

    boolean isCommandChanged(String prefixAndLabel);

    String getCommandChanged(String prefixAndLabel);

    void getCommandChanged(Supplier<String> prefixAndLabel, BiConsumer<String, IChanger> biConsumer);

    void setCommand(String prefixAndLabel, String newPrefixAndLabel);

    void setCommandLabel(String label, String newLabel);

    void addChanger(IChanger iChanger);

    void removeChanger(IChanger iChanger);

    List<IChanger> changers();

    IChanger findById(String id);

    void clear();
}
