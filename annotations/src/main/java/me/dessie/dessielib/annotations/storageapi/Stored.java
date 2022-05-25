package me.dessie.dessielib.annotations.storageapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field as a field that can be stored and/or retrieved from using StorageAPI
 *
 * If a field is being used as a recompose field as well, it must be in the same order that the {@link RecomposeConstructor}'s
 * parameters are defined in.
 * Additionally, the amount of {@link Stored} and {@link StoredList} fields should match the amount of parameters in the {@link RecomposeConstructor} constructor.
 *
 * If you wish to store a list, use {@link StoredList}
 *
 * @see RecomposeConstructor
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Stored {
    /**
     * Sets if the Field is used when recomposing the Object.
     * If false, the field will be stored, but not retrieved.
     *
     * By default, all fields are used in the retrieve process.
     *
     * @return If the Field is used when recomposing the Object.
     */
    boolean recompose() default true;

    /**
     * Manually change the sub-path to store this field as.
     * If no value is set, the Field's name will be used.
     *
     * @return The sub-path to store as. Otherwise, the field name is used.
     */
    String storeAs() default "";
}
