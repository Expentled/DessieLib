package me.dessie.dessielib.storageapi.storage.container.hooks;

import java.util.function.Function;

/**
 * Hooks into a {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}
 * to specify how the StorageContainer should retrieve data from the data structure.
 *
 * If you're creating your own StorageContainer implementation, the hook
 * will provide you the path that the user wants to retrieve, and you should return the Object at the path.
 *
 * This hook can block a thread, and users should use {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer#retrieveAsync(Class, String)}
 * if they wish to retrieve asynchronously.
 */
public class RetrieveHook extends StorageHook<RetrieveHook> {

    private final Function<String, Object> function;

    /**
     * @param function How the hook behaves when retrieving from the structure.
     *                 The {@link Function} will accept the path to the data, and should return the Object.
     *                 The Function can and should block the Thread to await the Object.
     */
    public RetrieveHook(Function<String, Object> function) {
        this.function = function;
    }

    /**
     * @return The behavior {@link Function} for this hook.
     */
    public Function<String, Object> getFunction() {
        return function;
    }
}
