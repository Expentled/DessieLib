package me.dessie.dessielib.storageapi.storage.decomposition.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a constructor as a default way to recompose a {@link me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer}
 * when it's being retrieved from a {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}
 *
 * This constructor must have it's parameters in the same order as all {@link Stored} fields are defined in the class.
 * Additionally, the amount of parameters in this constructor should match the amount of {@link Stored} annotated fields.
 *
 * @see Stored
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecomposeConstructor {

    /**
     * Determines if the parameters of the constructor are allowed to be null.
     * Note, that an object returns null if it does not exist in the data structure.
     *
     * If this is false, and a null argument is found, the object will fail to recompose.
     *
     * @return If null arguments are allowed to be passed to the constructor.
     */
    boolean allowNull() default false;

    /**
     * Determines how recomposing should react in the case it cannot recompose the object.
     * If throw error is true, and it cannot be composed, the error that occurred will be thrown.
     *
     * If this is false, then no error will be thrown, and the recomposed Object will be returned as null.
     *
     * @return Whether an error should be thrown, or the object returned as null when failing.
     */
    boolean throwError() default true;
}
