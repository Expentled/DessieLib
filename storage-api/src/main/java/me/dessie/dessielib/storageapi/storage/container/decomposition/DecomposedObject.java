package me.dessie.dessielib.storageapi.storage.container.decomposition;

import java.util.HashMap;
import java.util.Map;

public class DecomposedObject {
    private final Map<String, Object> decomposedMap = new HashMap<>();

    public DecomposedObject addDecomposedKey(String path, Object data) {
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
