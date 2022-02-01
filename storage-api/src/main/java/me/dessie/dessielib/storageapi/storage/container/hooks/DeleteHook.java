package me.dessie.dessielib.storageapi.storage.container.hooks;

import java.util.function.Consumer;


/**
 * Hooks into a {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}
 * to specify how the StorageContainer should delete data from the data structure.
 *
 * If you're creating your own StorageContainer implementation, the hook
 * will provide you the path that the user wants to delete.
 *
 * This hooks {@link Consumer} will always be executed asynchronously by the StorageContainer.
 */
public class DeleteHook extends StorageHook<DeleteHook> {

    private final Consumer<String> consumer;

    /**
     * @param consumer How the hook behaves when deleting from the structure.
     *                 The {@link Consumer} will accept the path to the data.
     */
    public DeleteHook(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    /**
     * @return The behavior {@link Consumer} for this hook.
     */
    public Consumer<String> getConsumer() {
        return consumer;
    }
}
