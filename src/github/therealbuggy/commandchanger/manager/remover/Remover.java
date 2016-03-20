package github.therealbuggy.commandchanger.manager.remover;

import java.util.List;
import java.util.Set;

/**
 * Created by jonathan on 19/03/16.
 */
public interface Remover {

    String getId();

    Set<String> getSource();

    boolean canRemove(String name);

}
