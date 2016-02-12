package github.therealbuggy.commandchanger.config.locale;

public class PortugueseChangerLocale extends ChangerLocale {

    @Override
    public Keys getIdOf(String s) {

        Keys keys = super.getIdOf(s);

        if (keys == Keys.UNKNOWN) {
            for (PTKeys key : PTKeys.values()) {
                if (key.getDef() != null &&
                        key.getDef().equalsIgnoreCase(s)) {
                    return key.getKey();
                }
            }
        }

        return keys;
    }

    @Override
    public String translate(Keys keys) {
        for(PTKeys ptKey : PTKeys.values()) {
            if(ptKey.getDef() == null)
                continue;

            if(ptKey.getKey() == keys) {
                return ptKey.getDef();
            }
        }
        return keys.getDef();
    }

    public enum PTKeys {
        FROM("de", Keys.FROM),
        TO("para", Keys.TO),
        REGEX("expressaoRegular", Keys.REGEX),
        FORCE("forcar", Keys.FORCE),
        TRUE("sim", Keys.TRUE),
        FALSE("nao", Keys.FALSE);

        private final String def;
        private final Keys key;

        PTKeys(String def, Keys key) {
            this.def = def;
            this.key = key;
        }

        public String getDef() {
            return def;
        }

        public Keys getKey() {
            return key;
        }
    }


}
// DESLIGUEI O FIO DA REDE MEMO