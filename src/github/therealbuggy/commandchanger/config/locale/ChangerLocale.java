package github.therealbuggy.commandchanger.config.locale;

import github.therealbuggy.configurator.locale.ILocale;

/**
 * Created by jonathan on 10/02/16.
 */
public class ChangerLocale implements ILocale<String, ChangerLocale.Keys> {

    @Override
    public Keys getIdOf(String s) {

        for (Keys key : Keys.values()) {
            if (key.getDef() != null &&
                    key.getDef().equalsIgnoreCase(s)) {
                return key;
            }
        }
        return Keys.UNKNOWN;
    }

    @Override
    public String translate(Keys keys) {
        return keys.getDef();
    }

    public enum Keys {
        FROM("from"),
        TO("to"),
        REGEX("regex"),
        FORCE("force"),
        TRUE("true"),
        FALSE("false"),
        UNKNOWN(null);

        private final String def;

        Keys(String def) {
            this.def = def;
        }

        public String getDef() {
            return def;
        }
    }


}
