package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.storageapi.storage.format.flatfile.JSONContainer;
import me.dessie.dessielib.storageapi.storage.format.flatfile.YAMLContainer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * For data structures that support array storages (ie, {@link YAMLContainer and {@link JSONContainer }}
 * this interface will allow easier connection with those arrays, if implemented within the container.
 */
public interface ArrayContainer {

    /**
     * Adds objects to the list on the path.
     * A list must already exist at the specified path to add objects to it.
     *
     * @param container The {@link StorageContainer} to add the object to.
     * @param path The path to the array list.
     * @param objects The objects to add
     * @param <T> The type of objects
     * @throws ClassCastException If the array list cannot support type T.
     */
    default <T> void addToList(StorageContainer container, String path, T... objects) throws ClassCastException {
        CompletableFuture<List<T>> future = container.retrieve(path);
        future.thenAcceptAsync(list -> {
            if(list == null) return;

            list.addAll(Arrays.asList(objects));
            container.set(path, list);
        });
    }

    /**
     * Removes objects from the list on the path.
     * A list must already exist at the specified path to remove objects from it.
     *
     * @param container The {@link StorageContainer} to add the object to.
     * @param path The path to the array list.
     * @param objects The objects to remove
     * @param <T> The type of objects
     * @throws ClassCastException If the array list cannot support type T.
     */
    default <T> void removeFromList(StorageContainer container, String path, T... objects) throws ClassCastException {
        CompletableFuture<List<T>> future = container.retrieve(path);

        future.thenAcceptAsync(list -> {
            if(list == null) return;

            list.removeAll(Arrays.asList(objects));
            container.set(path, list);
        });
    }

    /**
     * Checks if an object exists within the array list.
     *
     * @param container The {@link StorageContainer} to check.
     * @param path The path to the array list.
     * @param object The object to check the list for.
     * @param <T> The type of the object
     * @throws ClassCastException If the array list cannot support type T.
     * @return A future that when completed will verify if the object exists within the provided array.
     */
    default <T> CompletableFuture<Boolean> listContains(StorageContainer container, String path, T object) {
        CompletableFuture<List<T>> listFuture = container.retrieve(path);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        listFuture.thenAccept(list -> {
            if(list == null) return;

            future.complete(list.contains(object));
        });

        return future;
    }
}
