package github.therealbuggy.commandchanger.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import github.therealbuggy.commandchanger.manager.changer.DefaultChanger;
import github.therealbuggy.commandchanger.manager.changer.IChanger;

/**
 * Created by jonathan on 10/02/16.
 */
public class SimpleChangerManager implements CommandChangeManager {

    private final Set<IChanger> changers = new HashSet<>();

    @Override
    public boolean isCommandChanged(String prefixAndLabel) {
        return getCommandChanged(prefixAndLabel) != null;
    }

    @Override
    public String getCommandChanged(String prefixAndLabel) {

        AtomicReference<String> change = new AtomicReference<>();

        getCommandChanged(() -> prefixAndLabel, (changed, i) -> change.set(changed));

        return change.get();
    }

    @Override
    public void getCommandChanged(Supplier<String> prefixAndLabel, BiConsumer<String, IChanger> biConsumer) {
        for (IChanger iChanger : changers) {
            if (iChanger.canReplace(prefixAndLabel.get())) {
                String replc = iChanger.applyReplacement(prefixAndLabel.get());
                if (replc != null)
                    biConsumer.accept(replc, iChanger);
            }
        }
    }

    @Override
    public void setCommand(String prefixAndLabel, String newPrefixAndLabel) {
        changers.add(new DefaultChanger(prefixAndLabel, newPrefixAndLabel, false));
    }

    @Override
    public void setCommandLabel(String label, String newLabel) {
        changers.add(new DefaultChanger("*:" + label, newLabel, false));
    }

    @Override
    public void addChanger(IChanger iChanger) {
        changers.add(iChanger);
    }

    @Override
    public void removeChanger(IChanger iChanger) {
        changers.remove(iChanger);
    }

    @Override
    public List<IChanger> changers() {
        return Collections.unmodifiableList(new ArrayList<>(changers));
    }

    @Override
    public IChanger findById(String id) {
        return changers.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void clear() {
        changers.clear();
    }
}
