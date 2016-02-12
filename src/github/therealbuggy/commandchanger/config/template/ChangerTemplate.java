package github.therealbuggy.commandchanger.config.template;

import github.therealbuggy.commandchanger.config.locale.ChangerLocale;
import github.therealbuggy.commandchanger.config.locale.ChangerLocale.Keys;
import github.therealbuggy.configurator.locale.ILocale;
import github.therealbuggy.configurator.locale.LocaleList;

/**
 * Created by jonathan on 10/02/16.
 */
public class ChangerTemplate {
    private static final ChangerLocale defaultLocale = new ChangerLocale();
    LocaleList<String, Keys> localeList = new LocaleList<>();

    public ChangerTemplate() {
        this.localeList.addLocale(defaultLocale);
    }

    public String getFromKey() {
        return localeList.translate(Keys.FROM.getDef());
    }

    public String getToKey() {
        return localeList.translate(Keys.TO.getDef());
    }

    public String getRegexKey() {
        return localeList.translate(Keys.REGEX.getDef());
    }

    public Keys get(String name) {
        return localeList.translateId(name);
    }

    public void addLocale(ILocale<String, Keys> locale) {
        this.localeList.addLocale(locale);
    }

    public void removeLocale(ILocale<String, Keys> locale) {
        this.localeList.removeLocale(locale);
    }

    public LocaleList<String, Keys> getLocaleList() {
        return localeList;
    }

    public String getForceKey() {
        return localeList.translate(Keys.FORCE.getDef());
    }
}
