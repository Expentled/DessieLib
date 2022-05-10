package me.dessie.dessielib.storageapi.storage.decomposition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * A DecomposedObject is used for {@link StorageDecomposer}s for
 * decomposing an object into it's decomposed state.
 *
 * DecomposedObjects will store a path, and an Object linked to that path.
 * For example, we may have "x" as a path, and "125.2" as the Object.
 *
 * When we store a DecomposedObject into a data structure,
 * the path will have x and 125.2 would be stored.
 *
 * @see StorageDecomposer to add the Decomposer to the {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}s usage list.
 * @see RecomposedObject for recomposing these back into their original Object.
 *
 */
public class DecomposedObject {
    private final Map<String, Object> decomposedMap = new HashMap<>();

    /**
     * Adds a path->object pair to the object.
     *
     * @param path The path of the data, should only be 1 word.
     * @param data The data to store.
     * @return The {@link DecomposedObject} instance.
     */
    public DecomposedObject addDecomposedKey(String path, Object data) {
        Objects.requireNonNull(path, "Cannot add null path!");

        this.getDecomposedMap().put(path, data);
        return this;
    }

    /**
     * Removes a path from the object.
     *
     * @param path The path to remove.
     * @return The {@link DecomposedObject} instance.
     */
    public DecomposedObject removeDecomposedKey(String path) {
        this.getDecomposedMap().remove(path);
        return this;
    }

    /**
     * @return The current path->object map for this {@link DecomposedObject}.
     */
    public Map<String, Object> getDecomposedMap() {
        return decomposedMap;
    }
}
