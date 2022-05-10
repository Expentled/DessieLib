package me.dessie.dessielib.storageapi.storage.decomposition.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field as a field that can be stored and/or retrieved from using a {@link me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer}
 *
 * If a field is being used as a recompose field as well, it must be in the same order that the {@link RecomposeConstructor}'s
 * parameters are defined in.
 * Additionally, the amount of {@link Stored} fields should match the amount of parameters in the {@link RecomposeConstructor} constructor.
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

    /**
     * Sets the Class type that this list will be stored with.
     * If you're storing an array or list, you will need to specify this type.
     *
     * By setting to Stored.class, the variable type of the Field itself will be used.
     *
     * @return The type that this field will be stored as.
     */
    Class<?> type() default Stored.class;
}
