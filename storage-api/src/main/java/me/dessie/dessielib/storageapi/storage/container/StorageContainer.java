package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.cache.CachedObject;
import me.dessie.dessielib.storageapi.storage.cache.StorageCache;
import me.dessie.dessielib.storageapi.storage.container.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.container.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class StorageContainer {

    private static final List<StorageDecomposer<?>> storageDecomposers = new ArrayList<>();

    private final StorageCache cache;
    private final StorageSettings settings;

    //Temporarily stores all things that were changed and will need to be pushed to the data source.
    private final Map<String, Object> setCache = new HashMap<>();

    //List of all paths that have been removed using the remove method.
    private final List<String> removeCache = new ArrayList<>();

    /**
     * Creates a StorageContainer with a default {@link StorageSettings}.
     *
     * @see StorageSettings#StorageSettings() for the default settings.
     */
    public StorageContainer() {
        this(new StorageSettings());
    }

    /**
     * Creates a StorageContainer with the specified {@link StorageSettings}.
     *
     * @param settings The StorageSettings for the container.
     */
    public StorageContainer(StorageSettings settings) {
        if(!StorageAPI.isRegistered()) {
            throw new NullPointerException("You need to register your plugin before creating a StorageContainer!");
        }

        this.settings = settings;
        this.cache = new StorageCache(this.getSettings().getCacheDuration());

        //The repeating task to push add modified changes to the data source.
        //Uses the settings for the length.
        Bukkit.getScheduler().runTaskTimer(StorageAPI.getPlugin(), () -> {
            if(this.getSetCache().isEmpty()) return;

            //Store and Delete the caches.
            this.getSetCache().keySet().forEach(path -> {
                this.storeHook().getConsumer().accept(path, this.getSetCache().get(path));
            });

            this.getRemoveCache().forEach(path -> {
                this.deleteHook().getConsumer().accept(path);
            });

            this.getSetCache().clear();
            this.getRemoveCache().clear();
        }, this.getSettings().getUpdate() * 20L, this.getSettings().getUpdate() * 20L);
    }

    protected abstract StoreHook storeHook();
    protected abstract DeleteHook deleteHook();
    protected abstract RetrieveHook retrieveHook();

    /**
     * Returns a cached object.
     *
     * @param path The path to get the data from.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     * @throws ClassCastException If the cached object is not of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path) throws ClassCastException {
        CachedObject cachedObject = this.getCache().get(path);
        return cachedObject == null ? null : (T) cachedObject.getObject();
    }

    /**
     * Sets data into a cache that will eventually be updated into the data structure.
     * Data that is set may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, change the {@link StorageSettings}.
     *
     * @see StorageContainer#store(String, Object) for storing data within the source.
     *
     * @param path The path of the data.
     * @param data The data to set.
     */
    public void set(String path, Object data) {
        this.getSetCache().put(path, data);
        this.getRemoveCache().remove(path);
    }

    /**
     * Puts data into a cache that will eventually be updated into the data structure to remove them.
     * Data that is removed may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, change the {@link StorageSettings}.
     *
     * @see StorageContainer#delete(String)  for immediately deleting data within the source.
     *
     * @param path The path of the data to remove.
     */
    public void remove(String path) {
        this.getRemoveCache().add(path);

        //Don't need to set anything, since now it was removed.
        this.getSetCache().remove(path);
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     *
     * @see StorageContainer#set(String, Object) for caching objects instead of writing directly to the structure.
     *
     * @param path The path to store the data to.
     * @param data The data to store in the file format.
     */
    public void store(String path, Object data) throws IOException {
        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            StorageDecomposer<?> decomposer = getDecomposer(data.getClass());
            if(decomposer != null) {
                //Add the placeholder for each decomposed path.
                String decomposePath = path + ".%path%";
                DecomposedObject object = decomposer.applyDecompose(data);
                for(String decomposedPath : object.getDecomposedMap().keySet()) {
                    this.storeHook().getConsumer().accept(decomposePath.replace("%path%", decomposedPath), object.getDecomposedMap().get(decomposedPath));
                }
            } else {
                this.storeHook().getConsumer().accept(path, data);
            }
            this.storeHook().complete();
        });
    }

    /**
     * Removes a path from the data source. This method is executed asynchronously.
     *
     * @see StorageContainer#remove(String) for removing data using a cache. (Recommended)
     *
     * @param path The path to remove.
     */
    public void delete(String path) {
        this.getRemoveCache().remove(path);
        this.getSetCache().remove(path);

        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            this.deleteHook().getConsumer().accept(path);
            this.deleteHook().complete();
        });
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * This method is executed asynchronously.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     *
     * @see StorageContainer#retrieve(String) to get the value with implicit casting.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     * @throws ClassCastException If the object at the retrieved path is not of type T.
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> retrieve(Class<T> type, String path) throws ClassCastException {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            StorageDecomposer<?> decomposer = getDecomposer(type);
            CompletableFuture<T> data;
            if(decomposer != null) {
                //Only append %path% if it doesn't already exist in the String.
                data = (CompletableFuture<T>) decomposer.applyRecompose(this, path.contains("%path%") ? path : path + ".%path%");
            } else {
                if(this.isCached(path)) {
                    data = CompletableFuture.completedFuture(this.get(path));
                } else {
                    data = CompletableFuture.completedFuture((T) this.retrieveHook().getFunction().apply(path));
                    this.retrieveHook().complete();

                    //Catch the object
                    data.thenAccept(obj -> this.cache(path, obj));
                }
            }

            data.thenAccept(future::complete);
        });
        return future;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * @see StorageContainer#retrieve(Class, String) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     * @throws ClassCastException If the object returned is not of type T.
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> retrieve(String path) throws ClassCastException {
        if(this.isCached(path)) {
            return CompletableFuture.completedFuture(this.get(path));
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            T obj = (T) this.retrieveHook().getFunction().apply(path);
            this.retrieveHook().complete();
            this.cache(path, obj);
            future.complete(obj);
        });
        return future;
    }

    /**
     * Caches a retrieved object to the cache.
     *
     * @param path The path to cache to
     * @param object The object to cache
     */
    public void cache(String path, Object object) {
        this.getCache().cache(path, object);
    }

    /**
     * Returns if the provided path is cached.
     *
     * @param path The path to check.
     * @return If the path is cached.
     */
    public boolean isCached(String path) {
        return this.getCache().isCached(path) || this.getSetCache().containsKey(path);
    }

    /**
     * Returns the {@link StorageSettings} for this Container.
     *
     * @return The StorageSettings
     */
    public StorageSettings getSettings() {
        return settings;
    }

    /**
     * Returns the {@link StorageCache} that is cached objects.
     * @return The StorageCache
     */
    public StorageCache getCache() {
        return cache;
    }

    /**
     * Returns the cache of objects that have been set and not updated to the data structure.
     * This cache is cleared once the cache has been pushed to the structure.
     * @return The current set cache
     */
    public Map<String, Object> getSetCache() {
        return setCache;
    }

    /**
     * Returns the cache of objects that have been removed and not updated to the data structure.
     * This cache is cleared once the cache has been pushed to the structure.
     * @return The current remove cache
     */
    public List<String> getRemoveCache() {
        return removeCache;
    }

    public static void addStorageDecomposer(StorageDecomposer<?> decomposer) {
        getStorageDecomposers().add(decomposer);
    }

    public static StorageDecomposer<?> getDecomposer(Class<?> clazz) {
        return getStorageDecomposers().stream().filter(decomposer -> decomposer.getType() == clazz).findFirst().orElse(null);
    }

    public static List<StorageDecomposer<?>> getStorageDecomposers() {
        return storageDecomposers;
    }
}
