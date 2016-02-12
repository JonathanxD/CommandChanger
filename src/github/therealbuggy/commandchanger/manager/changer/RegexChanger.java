package github.therealbuggy.commandchanger.manager.changer;

import org.bukkit.command.Command;

import java.util.Objects;
import java.util.regex.Pattern;

import github.therealbuggy.commandchanger.api.CCommand;

/**
 * Created by jonathan on 10/02/16.
 */
public class RegexChanger implements IChanger {

    private final String id;
    private final String source;
    private final String replacement;
    private final Pattern pattern;
    private final boolean force;

    public RegexChanger(String id, String source, String replacement, boolean force) {
        this.id = id;
        this.force = force;
        this.source = Objects.requireNonNull(source);
        this.replacement = replacement;
        this.pattern = Pattern.compile(source);
    }

    public RegexChanger(String source, String replacement, boolean force) {
        this(null, source, replacement, force);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSource() {
        return this.source;
    }

    @Override
    public String getReplacement() {
        return this.replacement != null ? this.replacement : "";
    }

    @Override
    public boolean canReplace(String target) {
        return pattern.matcher(target).matches();
    }

    @Override
    public String applyReplacement(String value) {
        return value.replaceAll(source, getReplacement());
    }

    @Override
    public String getConstructClass() {
        return null;
    }

    @Override
    public String getConstructPlugin() {
        return null;
    }

    @Override
    public Command constructNewCommand(CCommand command) {
        return null;
    }

    @Override
    public boolean force() {
        return this.force;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + ": {from: " + getSource() + ", to: " + getReplacement() + ", force: "+force+"} ]";
    }
}
