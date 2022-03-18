package me.dessie.dessielib.storageapi.storage.decomposition;

import me.dessie.dessielib.storageapi.storage.container.StorageContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class provides an extremely easy way to store large objects
 * by splitting them into components to store into the data structure.
 *
 * These components are then saved individually, and then can be retrieved to re-compose the Object.
 *
 * For example, we may want to store a {@link org.bukkit.Location}.
 * We can add a StorageDecomposer, to split the Location into a World, X, Y, and Z.
 * The StorageDecomposer would then save these components for us automatically whenever a Location
 * is provided into {@link StorageContainer#store(String, Object)}.
 *
 * To retrieve the Location back, we can use {@link StorageContainer#retrieve(Class, String)} with the Location.class parameter.
 *
 * @param <T> The type of class this Decomposer refers to.
 */
public class StorageDecomposer<T> {

    private final Class<T> type;
    private final Function<T, DecomposedObject> decomposeFunction;
    private final BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> recomposeFunction;

    /**
     * @param type The type of class this Decomposer refers to.
     * @param decomposeFunction A {@link Function} that determines how this type of Object is decomposed.
     *                          This should return a {@link DecomposedObject} with the provided paths and objects.
     * @see DecomposedObject
     * @see DecomposedObject#addDecomposedKey(String, Object)
     */
    public StorageDecomposer(Class<T> type, Function<T, DecomposedObject> decomposeFunction) {
        this(type, decomposeFunction, null);
    }

    /**
     * @param type The type of class this Decomposer refers to.
     * @param decomposeFunction A {@link Function} that determines how this type of Object is decomposed.
     *                          This Function accepts the Object that a user was going to store, and will always be of type T.
     *                          This should return a {@link DecomposedObject} with the provided paths and objects.
     *
     * @param recomposeFunction A {@link BiFunction} How the StorageDecomposer should retrieve and recompose the Object.
     *                          This accepts the {@link StorageContainer} and the {@link RecomposedObject}, and should return a CompletableFuture for returning the final Object.
     * @see DecomposedObject
     * @see DecomposedObject#addDecomposedKey(String, Object)
     * @see RecomposedObject#addRecomposeKey(String, Class, Function)
     * @see RecomposedObject#addCompletedRecomposeKey(String, Function)
     */
    public StorageDecomposer(Class<T> type, Function<T, DecomposedObject> decomposeFunction, BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> recomposeFunction) {
        this.type = type;
        this.decomposeFunction = decomposeFunction;
        this.recomposeFunction = recomposeFunction;
    }

    /**
     * Applies the decomposed function to an Object.
     *
     * @param object The Object to decompose using this StorageDecomposer.
     * @return The {@link DecomposedObject} that is returned by the {@link Function}.
     * @throws ClassCastException if the provided Object is not of type T.
     */
    @SuppressWarnings("unchecked")
    public DecomposedObject applyDecompose(Object object) throws ClassCastException {
        return this.getDecomposeFunction().apply((T) object);
    }

    /**
     * Applies the recompose function to a Container and Path.
     * Within the path, a %path% string should exist. This will be replaced with each of the
     * recomposed keys.
     *
     * For example, if we have homes.cool_home.%path%, the recompose function should retrieve
     * homes.cool_home.x
     * homes.cool_home.y
     * homes.cool_home.z
     * homes.cool_home.world
     *
     * @param container The container to retrieve data from.
     * @param path The path to retrieve data from for the recomposition.
     * @return A {@link CompletableFuture} that will complete when the recompose is finished, or null if the function does not exist.
     */
    public CompletableFuture<T> applyRecompose(StorageContainer container, String path) {
        if(this.getRecomposeFunction() == null) return null;
        RecomposedObject<T> recomposedObject = new RecomposedObject<>();
        CompletableFuture<T> completed = this.getRecomposeFunction().apply(container, recomposedObject);

        List<CompletableFuture<?>> composedFutures = new ArrayList<>();
        for(String compose : recomposedObject.getRecomposedMap().keySet()) {
            String composedPath = path.replace("%path%", compose);

            if(container.isCached(composedPath)) {
                //Get the cached object
                T cached = container.get(composedPath);

                //Add the composed future with the cached object.
                composedFutures.add(CompletableFuture.completedFuture(cached));

                //Make sure the completed path is also notified of this completion, since we're not actually completing via retrieve.
                recomposedObject.getCompletedPath().get(compose).complete(cached);
            } else {
                CompletableFuture<Object> future = recomposedObject.getRecomposedMap().get(compose).apply(composedPath);
                composedFutures.add(future);

                //Cache the object once its returned.
                future.thenAccept(obj -> {
                    container.cache(composedPath, obj);
                });
            }
        }

        CompletableFuture.allOf(composedFutures.toArray(new CompletableFuture<?>[]{})).thenRun(() -> {
            completed.complete(recomposedObject.complete());
        });

        return completed;
    }

    /**
     * @return The type of the StorageDecomposer
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return The decomposition {@link Function} that will be used for decomposing.
     */
    public Function<T, DecomposedObject> getDecomposeFunction() {return decomposeFunction;}

    /**
     * @return The recomposition {@link BiFunction} that will be used for recomposing.
     */
    public BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> getRecomposeFunction() {return recomposeFunction;}
}
