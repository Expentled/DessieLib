package me.dessie.dessielib.storageapi.storage.container.hooks;

import java.util.function.Consumer;

public class DeleteHook extends StorageHook {

    private final Consumer<String> consumer;

    public DeleteHook(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }
}
