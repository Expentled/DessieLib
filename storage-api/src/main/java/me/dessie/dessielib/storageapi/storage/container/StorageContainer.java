package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.core.utils.tuple.Pair;
import me.dessie.dessielib.core.utils.tuple.Triple;
import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.cache.CachedObject;
import me.dessie.dessielib.storageapi.storage.cache.StorageCache;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public abstract class StorageContainer {

    private static final List<StorageDecomposer<?>> storageDecomposers = new ArrayList<>();

    private final StorageCache cache;
    private final StorageSettings settings;

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

        Objects.requireNonNull(settings, "Settings cannot be null!");

        this.settings = settings;
        this.cache = new StorageCache(this.getSettings().getCacheDuration());

        //The repeating task to push add modified changes to the data source.
        //Uses the settings for the length.

        if(this.getSettings().getUpdate() > 0) {
            Bukkit.getScheduler().runTaskTimer(StorageAPI.getPlugin(), () -> {
                this.getCache().flush(this);
            }, this.getSettings().getUpdate() * 20L, this.getSettings().getUpdate() * 20L);
        }
    }

    /**
     * Required implementation method, specifies how this StorageContainer's
     * store operations are performed.
     *
     * @return The {@link StoreHook} behavior.
     */
    protected abstract StoreHook storeHook();

    /**
     * Required implementation method, specifies how this StorageContainer's
     * delete operations are performed.
     *
     * @return The {@link DeleteHook} behavior.
     */
    protected abstract DeleteHook deleteHook();

    /**
     * Required implementation method, specifies how this StorageContainer's
     * retrieve operations are performed.
     *
     * @return The {@link RetrieveHook} behavior.
     */
    protected abstract RetrieveHook retrieveHook();

    /**
     * Returns a cached object.
     * Objects are only cached after they've initially been retrieved.
     * Therefore, this method will always return null if you haven't retrieved a path yet.
     *
     * @see StorageContainer#retrieve(String)
     * @see StorageContainer#getOrElse(String, Object)
     *
     * @param path The path to get the data from.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     * @throws ClassCastException If the cached object is not of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path) throws ClassCastException {
        Objects.requireNonNull(path, "Cannot get from null path!");

        CachedObject cachedObject = this.getCache().get(path);
        return cachedObject == null ? null : (T) cachedObject.getObject();
    }

    /**
     * Returns a cached object, or an alternative value if it doesn't exist.
     * Objects are only cached after they've initially been retrieved.
     *
     * @see StorageContainer#get(String)
     * @see StorageContainer#retrieveOrElse(String, Object)
     *
     * @param path The path to get the data from.
     * @param orElse The object to return if the path does not exist or returns null.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     * @throws ClassCastException If the cached object is not of type T
     */
    public <T> T getOrElse(String path, T orElse) {
        T obtained = this.get(path);
        return obtained == null ? orElse : obtained;
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
        Objects.requireNonNull(path, "Cannot set to null path!");

        if(!isSupported(data.getClass())) {
            throw new IllegalArgumentException(data.getClass() + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        this.cache(path, data);

        this.getCache().getSetCache().put(path, data);
        this.getCache().getRemoveCache().remove(path);
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
        Objects.requireNonNull(path, "Cannot remove from null path!");

        this.getCache().getRemoveCache().add(path);

        //Don't need to set anything, since now it was removed.
        this.getCache().getSetCache().remove(path);
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     *
     * @see StorageContainer#set(String, Object) for caching objects instead of writing directly to the structure.
     *
     * @param path The path to store the data to.
     * @param data The data to store in the file format.
     */
    @SuppressWarnings("unchecked")
    public void store(String path, Object data) {
        Objects.requireNonNull(path, "Cannot store to null path!");

        if(!isSupported(data.getClass())) {
            throw new IllegalArgumentException(data.getClass() + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        StorageDecomposer<?> decomposer = getDecomposer(data.getClass());
        DecomposedObject object = null;

        //Cache the data.
        if(decomposer != null) {
            String decomposePath = path + ".%path%";
            object = decomposer.applyDecompose(data);
            for(String decomposedPath : object.getDecomposedMap().keySet()) {
                this.cache(decomposePath.replace("%path%", decomposedPath), object.getDecomposedMap().get(decomposedPath));
            }
        } else {
            this.cache(path, data);
        }

        DecomposedObject finalObject = object;
        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            if(decomposer != null) {
                //Add the placeholder for each decomposed path.
                String decomposePath = path + ".%path%";
                for(String decomposedPath : finalObject.getDecomposedMap().keySet()) {
                    String compiledPath = decomposePath.replace("%path%", decomposedPath);
                    Object decomposedObject = finalObject.getDecomposedMap().get(decomposedPath);

                    if(StorageContainer.getDecomposer(decomposedObject.getClass()) != null) {
                        this.store(compiledPath, decomposedObject);
                        continue;
                    }

                    this.storeHook().getConsumer().accept(compiledPath, decomposedObject);
                }
            } else if (this instanceof ArrayContainer arrayContainer && arrayContainer.isList(data)) {
                //Verify the list can be saved.
                arrayContainer.getListStream(data).forEach(obj -> {
                    if (!arrayContainer.isListSupported(obj.getClass())) {
                        throw new IllegalArgumentException(obj.getClass() + " is not a supported storage class for a list within this container!");
                    }
                });

                //Get the Object handler for the container.
                Object handler = arrayContainer.getStoreListHandler();

                //Function to recursively adding StorageDecomposers to the handlerObject list.
                Recursive<Function<Triple<String, Object, StorageDecomposer<?>>, List<Pair<String, Object>>>> recursive = new Recursive<>();
                recursive.function = (triple) -> {
                    List<Pair<String, Object>> temp = new ArrayList<>();
                    StringBuilder currentPath = new StringBuilder(triple.getLeft());
                    StorageDecomposer<?> composer = triple.getRight();

                    for (String decomposedPath : composer.applyDecompose(triple.getMiddle()).getDecomposedMap().keySet()) {
                        Object storedObject = composer.applyDecompose(triple.getMiddle()).getDecomposedMap().get(decomposedPath);
                        currentPath.append(decomposedPath);

                        if (StorageContainer.getDecomposer(storedObject.getClass()) != null) {
                            currentPath.append(".");
                            temp.addAll(recursive.function.apply(new Triple<>(currentPath.toString(), storedObject, StorageContainer.getDecomposer(storedObject.getClass()))));
                            temp.add(new Pair<>(currentPath + "classType", storedObject.getClass().getName()));
                        } else {
                            temp.add(new Pair<>(currentPath.toString(), storedObject));
                        }
                        currentPath = new StringBuilder(triple.getLeft());
                    }
                    return temp;
                };

                //For each object in the list, attempt to add the decomposed object or the normal object to the handleObject list.
                arrayContainer.getListStream(data).forEach(obj -> {
                    List<Pair<String, Object>> storage = new ArrayList<>();
                    StorageDecomposer<?> decomp = StorageContainer.getDecomposer(obj.getClass());

                    if (decomp == null) {
                        storage.add(new Pair<>(null, obj));
                    } else {
                        storage.addAll(recursive.function.apply(new Triple<>("", obj, decomp)));
                    }

                    arrayContainer.handleListObject().accept(handler, storage);
                });

                //Finally, store the handler, which should be something the container natively supports.
                this.storeHook().getConsumer().accept(path, handler);
            } else {
                this.storeHook().getConsumer().accept(path, data);
            }
            this.storeHook().complete();
        });

        //No need to update these later, since this should overwrite them.
        this.getCache().getSetCache().remove(path);
        this.getCache().getRemoveCache().remove(path);
    }

    /**
     * Removes a path from the data source. This method is executed asynchronously.
     *
     * @see StorageContainer#remove(String) for removing data using a cache. (Recommended)
     *
     * @param path The path to remove.
     */
    public void delete(String path) {
        Objects.requireNonNull(path, "Cannot delete from null path!");

        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            this.deleteHook().getConsumer().accept(path);
            this.deleteHook().complete();
        });

        //This should overwrite anything we've already cached to do.
        this.getCache().getRemoveCache().remove(path);
        this.getCache().getSetCache().remove(path);
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * Note: This method is blocking, and will block until the data structure returns an object.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * @see StorageContainer#retrieve(Class, String) for retrieving with explicit casting, or to recompose decomposed objects.
     * @see StorageContainer#retrieveAsync(String) for retrieving data asynchronously.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T retrieve(String path) {
        Objects.requireNonNull(path, "Cannot retrieve from null path!");

        if(this.isCached(path)) {
            return this.get(path);
        }

        T obj = (T) this.retrieveHook().getFunction().apply(path);
        this.retrieveHook().complete();
        this.cache(path, obj);
        return obj;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     *
     * Note: This method is blocking, and will block for up to 5 seconds.
     * It is highly recommended to only use this method if you know your data structure will not block
     * For example, {@link me.dessie.dessielib.storageapi.storage.format.flatfile.YAMLContainer}s should be safe to use this method.
     *
     * @see StorageContainer#retrieve(String) to get the value with implicit casting.
     * @see StorageContainer#retrieveAsync(Class, String) for retrieving data asynchronously.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T retrieve(Class<T> type, String path) {
        Objects.requireNonNull(path, "Cannot retrieve from null path!");
        Objects.requireNonNull(type, "Type must be provided");

        if(!isSupported(type)) {
            throw new IllegalArgumentException(type + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        StorageDecomposer<?> decomposer = getDecomposer(type);
        if (decomposer != null) {
            //Only append %path% if it doesn't already exist in the String.
            //Also needs to be ran async so it doesn't block itself, very smart
            CompletableFuture<CompletableFuture<T>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return (CompletableFuture<T>) decomposer.applyRecompose(this, path.contains("%path%") ? path : path + ".%path%");
                } catch (ClassCastException e) {
                    throw new ClassCastException("Unable to recompose! This can occur if you're using addRecomposeKey instead of addCompletedRecomposeKey when using retrieve. addRecomposeKey should use retrieveAsync and addCompletedRecomposeKey should use retrieve or a straight object.");
                }
            });

            try {
                //Wait for the asynchronous future to be completed.
                return future.get().get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        } else {
            return this.retrieve(path);
        }
        return null;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * @see StorageContainer#retrieveAsync(Class, String) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(String path) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(path));
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     *
     * @see StorageContainer#retrieveAsync(String) to get the value with implicit casting.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Class<T> type, String path) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(type, path));
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * Note: This method is blocking, and will block until the data structure returns an object.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElse(String, Object) for retrieving with explicit casting, or to recompose decomposed objects.
     * @see StorageContainer#retrieveOrElseAsync(String, Object) for retrieving data asynchronously.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieveOrElse(String path, T orElse) {
        T obtained = this.retrieve(path);
        return obtained == null ? orElse : obtained;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     *
     * Note: This method is blocking, and will block for up to 5 seconds.
     * It is highly recommended to only use this method if you know your data structure will not block
     * For example, {@link me.dessie.dessielib.storageapi.storage.format.flatfile.YAMLContainer}s should be safe to use this method.
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElse(String, Object) to get the value with implicit casting.
     * @see StorageContainer#retrieveOrElseAsync(Class, String, Object) for retrieving data asynchronously.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieveOrElse(Class<T> type, String path, T orElse) {
        T obtained = this.retrieve(type, path);
        return obtained == null ? orElse : obtained;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElseAsync(Class, String, Object) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveOrElseAsync(String path, T orElse) {
        CompletableFuture<T> obtained = this.retrieveAsync(path);
        obtained.thenAccept(obj -> {
            obtained.complete(obj == null ? orElse : obj);
        });
        return obtained;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElseAsync(String, Object) to get the value with implicit casting.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveOrElseAsync(Class<T> type, String path, T orElse) {
        CompletableFuture<T> obtained = this.retrieveAsync(type, path);
        obtained.thenAccept(obj -> {
            obtained.complete(obj == null ? orElse : obj);
        });
        return obtained;
    }

    /**
     * Caches a retrieved object to the cache.
     *
     * @param path The path to cache to
     * @param object The object to cache
     */
    public void cache(String path, Object object) {
        Objects.requireNonNull(path, "Cannot cache null path!");
        this.getCache().cache(path, object);
    }

    /**
     * Returns if the provided path is cached.
     *
     * @param path The path to check.
     * @return If the path is cached.
     */
    public boolean isCached(String path) {
        return this.getCache().isCached(path);
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
     * Updates the {@link StorageContainer} with the set and remove caches.
     * After flushing, these maps will be cleared.
     *
     * Flushing will not empty the cached data, only the data that needs to be updated to the structure.
     *
     * @see StorageContainer#set(String, Object) for adding objects into the Set cache.
     * @see StorageContainer#remove(String) for adding paths into the Remove cache.
     */
    public void flush() {
        this.getCache().flush(this);
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        this.getCache().clearCache();
    }

    /**
     * Returns if a class is supported for storage in this container.
     *
     * All primitives and Strings are automatically supported.
     * Other classes will need to have a {@link StorageDecomposer} to be considered supported.
     *
     * @param clazz The type to check.
     * @return If the specified class is able to be stored.
     */
    public boolean isSupported(Class<?> clazz) {
        if(clazz.isPrimitive()) return true;
        if(clazz == String.class) return true;
        if(getDecomposer(clazz) != null) return true;
        if(StorageAPI.getWrappers().containsValue(clazz)) return true;
        if(this instanceof ArrayContainer && (clazz.isArray() || Collection.class.isAssignableFrom(clazz))) return true;

        return false;
    }

    /**
     * Adds a {@link StorageDecomposer} that can be accessed through all StorageContainer instances.
     * These only need to be added once, and a class can only have 1 StorageDecomposer.
     *
     * Attempting to add a second StorageDecomposer for a class will overwrite the first one.
     *
     * @param decomposer The StorageDecomposer to add.
     */
    public static void addStorageDecomposer(StorageDecomposer<?> decomposer) {
        getStorageDecomposers().removeIf(decomp -> decomp.getType() == decomposer.getType());
        getStorageDecomposers().add(decomposer);
    }

    /**
     * Adds an Enum decomposer to support directly storing and obtaining Enums from the container.
     * @param <T> The type of Enum to register
     * @param enumType The Enum class to register.
     */
    public static <T extends Enum<T>> void addStorageEnum(Class<T> enumType) {
        StorageContainer.addStorageDecomposer(new StorageDecomposer<>(enumType, (e) -> {
            DecomposedObject object = new DecomposedObject();
            object.addDecomposedKey("value", e.name());

            return object;
        }, (container, recompose) -> {
            recompose.addRecomposeKey("value", container::retrieveAsync);

            return recompose.onComplete(completed -> Enum.valueOf(enumType, (String) completed.getCompletedObject("value")));
        }));
    }

    /**
     * Returns a {@link StorageDecomposer} from the class instance.
     *
     * @param clazz The class to get the decomposer for.
     * @return The registered StorageDecomposer for the provided class, or null if it doesn't exist.
     */
    public static StorageDecomposer<?> getDecomposer(Class<?> clazz) {
        if(clazz == null) return null;
        return getStorageDecomposers().stream().filter(decomposer -> decomposer.getType() == clazz).findFirst().orElse(null);
    }

    /**
     * @return All registered {@link StorageDecomposer}s
     */
    public static List<StorageDecomposer<?>> getStorageDecomposers() {
        return storageDecomposers;
    }

    public static class Recursive<T> {
        public T function;
    }
}
