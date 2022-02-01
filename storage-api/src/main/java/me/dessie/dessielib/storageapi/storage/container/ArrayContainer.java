package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.storageapi.storage.format.flatfile.JSONContainer;
import me.dessie.dessielib.storageapi.storage.format.flatfile.YAMLContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * For data structures that support array storages (ie, {@link YAMLContainer} and {@link JSONContainer}
 * this interface will allow easier connection with those arrays, if implemented within the container.
 *
 */
public interface ArrayContainer {

    /**
     * Adds objects to the list on the path.
     * If a list does not exist at the specified path, it will be created.
     *
     * @param container The {@link StorageContainer} to add the object to.
     * @param path The path to the array list.
     * @param objects The objects to add
     * @param <T> The type of objects
     *
     * @return A {@link CompletableFuture} that is completed once the data has been set. The Future will have the updated list.
     */
    default <T> CompletableFuture<List<T>> addToList(StorageContainer container, String path, T... objects) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture<List<T>> retrieved = container.retrieveAsync(path);

        retrieved.thenAcceptAsync(list -> {
            if(list == null) {list = new ArrayList<>();}

            list.addAll(Arrays.asList(objects));
            container.set(path, list);
            future.complete(list);
        });
        return future;
    }

    /**
     * Removes objects from the list on the path.
     * A list must already exist at the specified path to remove objects from it.
     *
     * @param container The {@link StorageContainer} to add the object to.
     * @param path The path to the array list.
     * @param objects The objects to remove
     * @param <T> The type of objects
     *
     * @return A {@link CompletableFuture} that is completed once the data has been removed. The Future will have the updated list.
     */
    default <T> CompletableFuture<List<T>> removeFromList(StorageContainer container, String path, T... objects) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture<List<T>> retrieved = container.retrieveAsync(path);

        retrieved.thenAcceptAsync(list -> {
            if(list == null) {
                future.complete(null);
                return;
            }

            list.removeAll(Arrays.asList(objects));
            container.set(path, list);
            future.complete(list);
        });

        return future;
    }

    /**
     * Checks if an object exists within the array list.
     *
     * @param container The {@link StorageContainer} to check.
     * @param path The path to the array list.
     * @param object The object to check the list for.
     * @param <T> The type of the object
     * @throws ClassCastException If the array list cannot support type T.
     *
     * @return A future that when completed will verify if the object exists within the provided array.
     */
    default <T> CompletableFuture<Boolean> listContains(StorageContainer container, String path, T object) {
        CompletableFuture<List<T>> listFuture = container.retrieveAsync(path);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        listFuture.thenAccept(list -> {
            if(list == null) return;
            future.complete(list.contains(object));
        });

        return future;
    }
}
