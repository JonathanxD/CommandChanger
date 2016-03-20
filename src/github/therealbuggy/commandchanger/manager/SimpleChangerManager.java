package github.therealbuggy.commandchanger.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import github.therealbuggy.commandchanger.manager.changer.DefaultChanger;
import github.therealbuggy.commandchanger.manager.changer.IChanger;
import github.therealbuggy.commandchanger.manager.remover.Remover;

/**
 * Created by jonathan on 10/02/16.
 */
public class SimpleChangerManager implements CommandChangeManager {

    private final Set<IChanger> changers = new HashSet<>();
    private final Set<Remover> removers = new HashSet<>();

    @Override
    public boolean isCommandChanged(String prefixAndLabel) {
        return getCommandChanged(prefixAndLabel) != null;
    }

    @Override
    public boolean isCommandRemoved(String prefixAndLabel) {

        for (Remover remover : removers) {
            if (remover.canRemove(prefixAndLabel)) {
                return true;
            }
        }

        return false;
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
    public void addRemover(Remover remover) {
        removers.add(remover);
    }

    @Override
    public void removeRemover(Remover remover) {
        removers.remove(remover);
    }

    @Override
    public List<Remover> removers() {
        return Collections.unmodifiableList(new ArrayList<>(removers));
    }

    @Override
    public List<IChanger> changers() {
        return Collections.unmodifiableList(new ArrayList<>(changers));
    }

    @Override
    public IChanger findChangerById(String id) {
        return changers.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Remover findRemoverById(String id) {
        return removers.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void clearChangers() {
        changers.clear();
    }

    @Override
    public void clearRemovers() {
        removers.clear();
    }
}
