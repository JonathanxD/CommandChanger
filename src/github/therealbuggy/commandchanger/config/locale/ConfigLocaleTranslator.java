package github.therealbuggy.commandchanger.config.locale;

import github.therealbuggy.commandchanger.config.ConfigLocale;
import github.therealbuggy.configurator.IConfigurator;
import github.therealbuggy.configurator.translator.VariableTranslator;

/**
 * Created by jonathan on 10/02/16.
 */
public class ConfigLocaleTranslator extends VariableTranslator<ConfigLocale> {

    public ConfigLocaleTranslator(IConfigurator configurator) {
        super(configurator);
    }

    @Override
    protected ConfigLocale valueTranslate(String s) {
        return ConfigLocale.valueOf(s.toUpperCase());
    }
}
