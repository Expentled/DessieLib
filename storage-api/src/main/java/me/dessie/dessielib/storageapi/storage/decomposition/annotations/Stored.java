package me.dessie.dessielib.storageapi.storage.decomposition.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
