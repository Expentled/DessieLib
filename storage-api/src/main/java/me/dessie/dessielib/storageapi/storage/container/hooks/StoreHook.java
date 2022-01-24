package me.dessie.dessielib.storageapi.storage.container.hooks;

import java.util.function.BiConsumer;

public class StoreHook extends StorageHook {

    private final BiConsumer<String, Object> consumer;

    public StoreHook(BiConsumer<String, Object> consumer) {
        this.consumer = consumer;
    }

    public BiConsumer<String, Object> getConsumer() {
        return consumer;
    }
}
