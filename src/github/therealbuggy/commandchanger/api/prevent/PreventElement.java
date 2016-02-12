package github.therealbuggy.commandchanger.api.prevent;

import java.util.Objects;

/**
 * Created by jonathan on 10/02/16.
 */
public class PreventElement implements Comparable<Class<?>> {

    private final Class<?> aClass;
    private final boolean assignable;

    public PreventElement(Class<?> aClass, boolean assignable) {
        this.aClass = aClass;
        this.assignable = assignable;
    }

    public PreventElement(Class<?> aClass) {
        this(aClass, false);
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public boolean isAssignable() {
        return assignable;
    }


    @Override
    public int compareTo(Class<?> o) {

        if (o == null) {
            return -1;
        }

        if (this.assignable)
            return this.getaClass().isAssignableFrom(o) ? 0 : -1;

        return this.getaClass() == o ? 0 : -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Class))
            return false;

        Class localAClass = (Class) obj;

        return compareTo(localAClass) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aClass.hashCode(), assignable);
    }
}
