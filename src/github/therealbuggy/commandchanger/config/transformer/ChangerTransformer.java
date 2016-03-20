package github.therealbuggy.commandchanger.config.transformer;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import github.therealbuggy.bukkitconfigurator.BukkitConfigurator;
import github.therealbuggy.bukkitconfigurator.backend.BukkitConfiguratorBackend;
import github.therealbuggy.commandchanger.api.CommandChangerReload;
import github.therealbuggy.commandchanger.config.CommandChangerConfigurator;
import github.therealbuggy.commandchanger.config.ConfigTags;
import github.therealbuggy.commandchanger.config.locale.ChangerLocale;
import github.therealbuggy.commandchanger.config.template.ChangerTemplate;
import github.therealbuggy.commandchanger.manager.changer.DefaultChanger;
import github.therealbuggy.commandchanger.manager.changer.IChanger;
import github.therealbuggy.commandchanger.manager.changer.RegexChanger;
import github.therealbuggy.configurator.IConfigurator;
import github.therealbuggy.configurator.key.Key;
import github.therealbuggy.configurator.key.KeyUtil;
import github.therealbuggy.configurator.locale.LocaleList;
import github.therealbuggy.configurator.transformer.Transformer;
import github.therealbuggy.configurator.transformer.exception.TransformException;
import github.therealbuggy.configurator.types.Type;
import github.therealbuggy.configurator.types.ValueTypes;
import github.therealbuggy.configurator.utils.Reference;
import github.therealbuggy.configurator.utils.Require;

/**
 * Created by jonathan on 10/02/16.
 */
public class ChangerTransformer implements Transformer<List<IChanger>> {

    public static final Reference REFERENCE = Reference.referenceTo().a(List.class).of(IChanger.class).build();

    private final BukkitConfiguratorBackend backend;

    public ChangerTransformer(CommandChangerConfigurator<ConfigTags> bukkitConfigurator) {
        this.backend = bukkitConfigurator.getBackend();
    }

    @Override
    public Optional<List<IChanger>> transformSection(Key<?> section, IConfigurator<?> configurator) throws TransformException {

        if (!KeyUtil.isSection(section)) {
            throw new TransformException("Is not a section: Section[" + section + ", Path=" + section.getPath() + ", Name=" + section.getName() + "]");
        }

        List<IChanger> changerList = new ArrayList<>();

        ChangerTemplate changerTemplate = Require.require(backend.extraData().getDataAssignable(ChangerTemplate.class));
        LocaleList<String, ChangerLocale.Keys> localeList = changerTemplate.getLocaleList();

        String path = section.getPath();

        Map<String, Object> sections = backend.getValues(path);

        String id = null;
        String from = null;
        String to = null;
        Boolean isRegex = null;
        Boolean force = null;
        for (Map.Entry<String, Object> entry : sections.entrySet()) {

            String subPath = entry.getKey();
            String entryKey = entry.getKey();

            if (entryKey.contains(".")) {
                String keys[] = entryKey.split("\\.");
                entryKey = keys[keys.length - 1];
                subPath = keys[0];
            }

            Object entryValue = entry.getValue();
            String stringValue = String.valueOf(entryValue);

            ChangerLocale.Keys key = changerTemplate.get(entryKey);


            if (subPath.equals(entryKey)) {
                id = entryKey;
            } else if (key == ChangerLocale.Keys.FROM) {
                from = stringValue;
            } else if (key == ChangerLocale.Keys.TO) {
                to = stringValue;
            } else if (key == ChangerLocale.Keys.REGEX) {
                isRegex = boolGet(entryValue, localeList);
            } else if (key == ChangerLocale.Keys.FORCE) {
                force = boolGet(entryValue, localeList);
            } else if (key == null || key == ChangerLocale.Keys.UNKNOWN) {
                throw new TransformException("Cannot determine value of " + entryKey + "!");
            }

            if (id != null && from != null && to != null && isRegex != null && force != null) {

                if (to.contains(" ")) {
                    System.err.println("BukkitCommandChanger -> Invalid command '"+localeList.translateFromId(ChangerLocale.Keys.TO)+"' input! Command may not work correctly!");
                }

                if (isRegex)
                    changerList.add(new RegexChanger(id, from, to, force));
                else
                    changerList.add(new DefaultChanger(id, from, to, force));

                id = null;
                from = null;
                to = null;
                isRegex = null;
                force = null;
            }
        }


        return Optional.of(changerList);
    }

    private boolean boolGet(Object obj, LocaleList<String, ChangerLocale.Keys> localeList) {

        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else {
            ChangerLocale.Keys key = localeList.translateId(String.valueOf(obj));

            if (key == ChangerLocale.Keys.TRUE) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void constructSection(Key<?> key, List<IChanger> iChangers, IConfigurator<?> iConfigurator) {

        ChangerTemplate changerTemplate = Require.require(backend.extraData().getDataAssignable(ChangerTemplate.class));
        LocaleList<String, ChangerLocale.Keys> localeList = changerTemplate.getLocaleList();


        String setPath = key.getPath();

        //Cleanup section

        backend.setValueToPath(setPath, null);

        for (IChanger iChanger : iChangers) {

            String id = iChanger.getId();
            String from = iChanger.getSource();
            String replacement = iChanger.getReplacement();
            boolean isRegex = iChanger instanceof RegexChanger;
            boolean force = iChanger.force();

            String pathToId = setPath + "." + id;

            String fromKey = localeList.translateFromId(ChangerLocale.Keys.FROM);
            String toKey = localeList.translateFromId(ChangerLocale.Keys.TO);
            String isRegexKey = localeList.translateFromId(ChangerLocale.Keys.REGEX);
            String forceKey = localeList.translateFromId(ChangerLocale.Keys.FORCE);

            Type<String> fromType = ValueTypes.StrType(from);
            Type<String> toType = ValueTypes.StrType(replacement);
            Type<Boolean> regexType = ValueTypes.BoolType(isRegex);
            Type<Boolean> forceType = ValueTypes.BoolType(force);

            backend.setValueToPath(Locals.get(pathToId, fromKey), fromType);
            backend.setValueToPath(Locals.get(pathToId, toKey), toType);
            backend.setValueToPath(Locals.get(pathToId, isRegexKey), regexType);
            backend.setValueToPath(Locals.get(pathToId, forceKey), forceType);

        }

        backend.save();

    }

    @Override
    public boolean canConstruct(Object value) {


        if (value instanceof Collection) {
            value = new ArrayList<Object>((Collection) value);
        }

        if (value instanceof List) {
            List list = (List) value;
            if (list.isEmpty())
                return false;

            if (list.get(0) instanceof IChanger) {
                return true;
            }
        }


        return false;
    }

    @Override
    public boolean supports(Reference reference) {

        return ChangerTransformer.REFERENCE.compareTo(reference) == 0;
    }
}
