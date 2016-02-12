package github.therealbuggy.commandchanger.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by jonathan on 09/02/16.
 */
public class Reflection {

    @SuppressWarnings({"Duplicates", "unchecked"})
    public static <T> T getFieldValue(Object object, String fieldName) {
        Field field = getField(object, fieldName);
        return getFieldValue(object, field);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, Class<T> fieldType) {
        Field field = getField(object, fieldType);
        return getFieldValue(object, field);

    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, Field field) {
        if (field == null)
            return null;

        field.setAccessible(true);

        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Field getField(Object object, String fieldName) {
        try {
            return object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
        }

        return null;
    }

    public static Field getField(Object object, Class<?> fieldType) {

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getType() == fieldType) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }

    public static <T> T setFinalStatic(Object instance, Field field, T newValue) {
        try {
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(instance, newValue);

            return getFieldValue(instance, field);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T setField(Object instance, Field field, T newValue) {
        try {
            field.setAccessible(true);

            field.set(instance, newValue);

            return getFieldValue(instance, field);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object callMethod(Object instance, String methodName, Class<?> returnType, Class<?>[] assignableParameters, Object[] parameters) {
        try {

            for (Method m : instance.getClass().getDeclaredMethods()) {
                if ((methodName == null || m.getName().equals(methodName)) &&
                        (returnType == null || returnType == m.getReturnType())
                        ) {
                    boolean allAssignable = true;

                    for (Class<?> parameter : m.getParameterTypes()) {
                        for (Class<?> assParameter : assignableParameters) {
                            if (!assParameter.isAssignableFrom(parameter)) {
                                allAssignable = false;
                            }
                        }
                    }

                    if (allAssignable) {
                        return m.invoke(instance, parameters);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Constructor<T> getConstructor(Class<T> aClass, Class<?>[] params) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor(params);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T construct(Constructor<T> constructor, Object[] params) {
        try {
            constructor.setAccessible(true);

            return constructor.newInstance(params);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
