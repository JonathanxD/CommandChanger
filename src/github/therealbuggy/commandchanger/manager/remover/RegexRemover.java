package github.therealbuggy.commandchanger.manager.remover;

import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

/**
 * Created by jonathan on 19/03/16.
 */
public class RegexRemover implements Remover {

    private final String id;
    private final Set<String> regex;

    public RegexRemover(String id, Set<String> regex) {
        this.id = id;
        this.regex = regex;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Set<String> getSource() {
        return this.regex;
    }

    @Override
    public boolean canRemove(String name) {

        if(name == null || name.isEmpty())
            return false;

        return regex.stream().anyMatch(n -> {
            try{
                return name.matches(n);
            } catch (PatternSyntaxException e) {
                throw new RuntimeException("Error during test of regex '"+n+"' and text '"+name+"'", e);
            }
        });
    }
}
