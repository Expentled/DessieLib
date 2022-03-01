package me.dessie.dessielib.storageapi.storage.decomposition.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
