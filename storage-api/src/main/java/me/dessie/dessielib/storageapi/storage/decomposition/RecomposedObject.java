package me.dessie.dessielib.storageapi.storage.decomposition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A RecomposedObject is used for {@link StorageDecomposer}s for
 * recomposing multiple paths from the {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer} back into
 * the Object that was decomposed by the {@link DecomposedObject}.
 *
 * RecomposedObjects will store a path, and a function that returns a {@link CompletableFuture}.
 * For example, we may have "x" as a path, and then wait for the data structure to return 125.2
 *
 * Once all CompletableFutures have completed, the RecomposedObject will return the
 * completed Object.
 *
 * @see StorageDecomposer to add the Recomposer to the {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}s usage list.
 * @see DecomposedObject for decomposing an Object into it's components to read from the data structure.
 *
 */
public class RecomposedObject<T> {

    private final Map<String, Function<String, CompletableFuture<Object>>> recomposedMap = new HashMap<>();
    private Function<RecomposedObject<T>, T> completeFunction;
    private final Map<String, CompletableFuture<Object>> pathFutures = new HashMap<>();

    /**
     * Adds a recomposed path and function.
     * @param path The path to add to.
     * @param data The Function, that accepts the path provided and returns a CompletableFuture with the
     *             Object retrieved at that path.
     *             Generally, you'll want to return {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer#retrieveAsync(String)}.
     * @return The RecomposedObject instance.
     */
    public RecomposedObject<T> addRecomposeKey(String path, Function<String, CompletableFuture<Object>> data) {
        Objects.requireNonNull(path, "Cannot add null path!");
        Objects.requireNonNull(data, "The recompose function cannot be null!");

        this.getRecomposedMap().putIfAbsent(path, data.andThen(after -> {
            this.getCompletedPath().put(path, after);
            return after;
        }));

        //Add an empty completable future, in-case the path is cached.
        this.getCompletedPath().put(path, new CompletableFuture<>());

        return this;
    }

    /**
     * Adds a completed recomposed path and function.
     * @param path The path to add to.
     * @param data The Function, that accepts the path provided and returns the data to return for this path.
     * @return The RecomposedObject instance.
     */
    public RecomposedObject<T> addCompletedRecomposeKey(String path, Function<String, Object> data) {
        this.getRecomposedMap().putIfAbsent(path, data.andThen(after -> {
            this.getCompletedPath().put(path, CompletableFuture.completedFuture(after));
            return CompletableFuture.completedFuture(after);
        }));

        this.getCompletedPath().put(path, new CompletableFuture<>());
        return this;
    }

    /**
     * Removes a path from the recompose list.
     *
     * @param path The path to remove.
     * @return The RecomposedObject instance.
     */
    public RecomposedObject<T> removeRecomposedKey(String path) {
        this.getRecomposedMap().remove(path);
        this.getCompletedPath().remove(path);
        return this;
    }

    /**
     * Called once all the Recomposed keys are completed.
     *
     * In this method, you should use {@link RecomposedObject#getCompletedObject(String)}
     * on the paths to rebuild your initial Object, and return the Object when completed.
     *
     * Once you return the Function, the CompletableFuture will be completed and the final
     * Object will be returned to the initial retrieve caller.
     *
     * @param function A function that accepts this RecomposedObject and returns the completed data Object.
     * @return A CompletableFuture, that is completed by the user once the Object is rebuilt.
     */
    public CompletableFuture<T> onComplete(Function<RecomposedObject<T>, T> function) {
        this.completeFunction = function;
        return new CompletableFuture<>();
    }

    /**
     * Calls the {@link RecomposedObject#onComplete(Function)} method.
     * @return The Object that the complete function returns, or null if the Object was unable to be created.
     */
    public T complete() {
        try {
            return this.completeFunction.apply(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the completed object for a specified path.
     * This should only be called in the {@link RecomposedObject#onComplete(Function)} method.
     * This method is Thread blocking, and will wait for the Recompose Functions to be completed to return.
     *
     * @param path The path to get.
     * @return The completed Object once completed by the {@link RecomposedObject#addRecomposeKey(String, Function)}.
     */
    public Object getCompletedObject(String path) {
        return pathFutures.get(path).join();
    }

    /**
     * @return All the current paths in the recompose map.
     */
    public Set<String> getKeys() { return this.getRecomposedMap().keySet(); }

    /**
     * @return The entire Recompose map
     */
    public Map<String, Function<String, CompletableFuture<Object>>> getRecomposedMap() {
        return recomposedMap;
    }

    /**
     * @return The completed path map.
     */
    public Map<String, CompletableFuture<Object>> getCompletedPath() {return pathFutures;}
}
