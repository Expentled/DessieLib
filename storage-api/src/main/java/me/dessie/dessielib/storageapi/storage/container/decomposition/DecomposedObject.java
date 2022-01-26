package me.dessie.dessielib.storageapi.storage.container.decomposition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DecomposedObject {
    private final Map<String, Object> decomposedMap = new HashMap<>();

    public DecomposedObject addDecomposedKey(String path, Object data) {
        Objects.requireNonNull(path, "Cannot add null path!");

        this.getDecomposedMap().put(path, data);
        return this;
    }

    public DecomposedObject removeDecomposedKey(String path) {
        this.getDecomposedMap().remove(path);
        return this;
    }

    public Map<String, Object> getDecomposedMap() {
        return decomposedMap;
    }
}
