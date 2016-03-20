package github.therealbuggy.commandchanger.config.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import github.therealbuggy.bukkitconfigurator.backend.BukkitConfiguratorBackend;
import github.therealbuggy.commandchanger.config.CommandChangerConfigurator;
import github.therealbuggy.commandchanger.config.ConfigTags;
import github.therealbuggy.commandchanger.manager.remover.RegexRemover;
import github.therealbuggy.commandchanger.manager.remover.Remover;
import github.therealbuggy.configurator.IConfigurator;
import github.therealbuggy.configurator.key.Key;
import github.therealbuggy.configurator.transformer.Transformer;
import github.therealbuggy.configurator.transformer.exception.TransformException;
import github.therealbuggy.configurator.types.Type;
import github.therealbuggy.configurator.types.ValueTypes;
import github.therealbuggy.configurator.utils.Reference;

/**
 * Created by jonathan on 19/03/16.
 */
public class RemoverTransformer implements Transformer<List<Remover>> {

    public static final Reference REFERENCE = Reference.referenceTo().a(List.class).of(Remover.class).build();

    private final BukkitConfiguratorBackend backend;

    public RemoverTransformer(CommandChangerConfigurator<ConfigTags> bukkitConfigurator) {
        this.backend = bukkitConfigurator.getBackend();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<Remover>> transformSection(Key<?> key, IConfigurator<?> iConfigurator) throws TransformException {

        List<Remover> removerList = new ArrayList<>();

        String path = key.getPath();

        Map<String, Object> map = backend.getAllOnPath(path);


        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String subPath = entry.getKey();
            String entryKey = entry.getKey();

            if (entryKey.contains(".")) {
                String keys[] = entryKey.split("\\.");
                entryKey = keys[keys.length - 1];
                subPath = keys[0];
            }

            Object entryValue = entry.getValue();

            String stringValue = String.valueOf(entryValue);

            if (!subPath.equals(entryKey)) {
                throw new TransformException("Error during parse, cannot find ID!");
            } else {
                if (!(entryValue instanceof Collection)) {
                    throw new TransformException("Error during parse, cannot parse value!");
                }
                Collection<Object> collection = (Collection<Object>) entryValue;

                Set<String> list = collection.stream().map(Object::toString).collect(Collectors.toSet());

                removerList.add(new RegexRemover(entryKey, list));
            }
        }


        return Optional.of(removerList);
    }

    @Override
    public void constructSection(Key<?> key, List<Remover> removerList, IConfigurator<?> iConfigurator) throws TransformException {
        String setPath = key.getPath();

        //Cleanup section

        backend.setValueToPath(setPath, null);

        for (Remover remover : removerList) {

            String id = remover.getId();
            Set<String> source = remover.getSource();

            Type<List<String>> listType = ValueTypes.ListType(new ArrayList<>(source));

            backend.setValueToPath(Locals.get(setPath, id), listType);
        }
    }

    @Deprecated
    @Override
    public boolean canConstruct(Object value) {

        if (value instanceof Collection) {
            value = new ArrayList<Object>((Collection) value);
        }

        if (value instanceof List) {
            List list = (List) value;
            if (list.isEmpty())
                return false;

            if (list.get(0) instanceof Remover) {
                return true;
            }
        }


        return false;
    }

    @Override
    public boolean supports(Reference reference) {
        return RemoverTransformer.REFERENCE.compareTo(reference) == 0;
    }
}
