package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.core.utils.tuple.Pair;
import me.dessie.dessielib.core.utils.tuple.Triple;
import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.decomposition.RecomposedObject;
import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This interface will allow easier connection with arrays, if implemented within the container.
 *
 * Please note, that it is the implementers' responsibility to add functionality
 * for storing and retrieving of lists within the respective hooks.
 *
 * @param <H> The Handler type, for storing and retrieving lists from the structure.
 *            Generally, this will be some natively supported array implementation such as {@link com.google.gson.JsonArray} or {@link ArrayList}.
 */
public abstract class ArrayContainer<H> extends StorageContainer {

    /**
     * Creates an ArrayContainer with a default {@link StorageSettings}.
     *
     * @see StorageSettings#StorageSettings() for the default settings.
     */
    public ArrayContainer() {super();}

    /**
     * Creates an ArrayContainer with the specified {@link StorageSettings}.
     *
     * @param settings The StorageSettings for the container.
     */
    public ArrayContainer(StorageSettings settings) {
        super(settings);
    }

    /**
     * Implementation method for returning a new handler object.
     * This handler object will be passed into your {@link ArrayContainer#handleListObject()} for each Object that is added.
     *
     * Usually, it should be just a new instance of whatever H is.
     *
     * @see ArrayContainer#handleListObject()
     *
     * @return The handler object to use.
     */
    protected abstract H getStoreListHandler();

    /**
     * Implementation method for returning an existing handler object on the data source.
     * This retrieved handler object will be passed into the {@link ArrayContainer#handleRetrieveList(Object, Class)} for recomposing the Objects.
     *
     * @see ArrayContainer#handleRetrieveList(Object, Class)
     *
     * @param path The path to retrieve the handler instance.
     * @return The existing handler object that exists at the data source path.
     */
    protected abstract H getRetrieveListHandler(String path);

    /**
     * Handles storing a List object into the data source.
     *
     * When a user attempts to store a List, this method will be called for all Objects within that list.
     * {@link StorageDecomposer}s will have their path and object within the provided list.
     * If the object isn't a StorageDecomposer, the Key in the {@link Pair} will be null.
     *
     * The Handler that is passed will be the same handler, and the final handler will be stored
     * using {@link StorageContainer#store(String, Object)}, passing the handler as the object.
     *
     * @see ArrayContainer#getRetrieveListHandler(String)
     *
     * @return The {@link BiConsumer} that writes to your handler from the provided data.
     */
    protected abstract BiConsumer<H, List<Pair<String, Object>>> handleListObject();

    /**
     * Handles retrieving a List object from the data source.
     *
     * When a user attempts to retrieve a list from the container, this method will be called
     * for handling your provided handler, and making sure objects are recomposed properly.
     *
     * This will generally involve parsing whatever handler your used, and converting all objects back into their original states
     * You can use the {@link RecomposedObject} instance provided for retrieving these objects again.
     *
     * If the type T is not a {@link StorageDecomposer}, the passed {@link RecomposedObject} will be null.
     *
     * @see ArrayContainer#getRetrieveListHandler(String)
     * @see RecomposedObject#completeObject(String, Object)
     * @see RecomposedObject#complete()
     *
     * @param handler The retrieved Handler from your data source.
     * @param type The {@link Class} type that the list containers.
     *             You will need to retrieve a {@link RecomposedObject} instance for all elements that will be used for recomposing your objects.
     * @param <T> The type of Object that the user wants the List to contain.
     * @return A recomposed list of the requested objects from the handler.
     */
    protected abstract <T> List<T> handleRetrieveList(H handler, Class<T> type);

    /**
     * Adds objects to the list on the path.
     * If a list does not exist at the specified path, it will be created.
     *
     * @param type The type of objects to add
     * @param path The path to the array list.
     * @param objects The objects to add
     * @param <T> The type of objects
     *
     * @return A {@link CompletableFuture} that is completed once the data has been set. The Future will have the updated list.
     */
    public <T> CompletableFuture<List<T>> addToList(Class<T> type, String path, T... objects) {
        if(objects.length == 0) {
            throw new IllegalArgumentException("You need to provide objects to save!");
        }

        if(!this.isListSupported(objects[0].getClass())) {
            throw new IllegalArgumentException(objects[0].getClass() + " is not a supported storage class for a list within this container. Only primitives and strings can be stored!");
        }

        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture<List<T>> retrieved = this.retrieveListOrElseAsync(type, path, new ArrayList<>());

        retrieved.thenAccept(list -> {
            list.addAll(Arrays.asList(objects));
            this.set(path, list);
            future.complete(list);
        });
        return future;
    }

    /**
     * Removes objects from the list on the path.
     * A list must already exist at the specified path to remove objects from it.
     *
     * @param type The type of objects to remove
     * @param path The path to the array list.
     * @param objects The objects to remove
     * @param <T> The type of objects
     *
     * @return A {@link CompletableFuture} that is completed once the data has been removed. The Future will have the updated list.
     */
    public <T> CompletableFuture<List<T>> removeFromList(Class<T> type, String path , T... objects) {
        if(objects.length == 0) {
            throw new IllegalArgumentException("You need to provide objects to remove!");
        }

        if(!this.isListSupported(objects[0].getClass())) {
            throw new IllegalArgumentException(objects[0].getClass() + " is not a supported storage class for a list within this container!");
        }

        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture<List<T>> retrieved = this.retrieveListAsync(type, path);

        retrieved.thenAccept(list -> {
            if(list == null) {
                future.complete(null);
                return;
            }

            list.removeAll(Arrays.asList(objects));
            if(list.isEmpty()) {
                this.remove(path);
            } else {
                this.set(path, list);
            }
            future.complete(list);
        });

        return future;
    }

    /**
     * Checks if an object exists within the array list.
     *
     * @param type The type of the object to check for
     * @param path The path to the array list.
     * @param object The object to check the list for.
     * @param <T> The type of the object
     * @throws ClassCastException If the array list cannot support type T.
     *
     * @return A future that when completed will verify if the object exists within the provided array.
     */
    public <T> CompletableFuture<Boolean> listContains(Class<T> type, String path, T object) {
        CompletableFuture<List<T>> listFuture = this.retrieveListAsync(type, path);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        listFuture.thenAccept(list -> {
            if(list == null) return;
            future.complete(list.contains(object));
        });

        return future;
    }

    /**
     * Retrieves a List directly from the data source.
     *
     * Please note that this method is Thread blocking.
     *
     * @see ArrayContainer#retrieveListAsync(Class, String)
     * @see ArrayContainer#retrieveListOrElse(Class, String, List)
     *
     * @param type The class type of Objects in the List.
     * @param path The path to the array list.
     * @param <T> The type ob the Objects in the list.
     * @return The List of retrieved Objects.
     */
    public <T> List<T> retrieveList(Class<T> type, String path) {
        Objects.requireNonNull(type, "Class type cannot be null!");
        Objects.requireNonNull(path, "Path cannot be null!");

        if(!isListSupported(type)) {
            throw new IllegalArgumentException(type.getName() + " is not a supported storage class for a list within this container!");
        }

        if(this.isCached(path)) {
            return this.get(path);
        }

        List<T> list;
        H handler;
        try {
            handler = this.getRetrieveListHandler(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        list = this.handleRetrieveList(handler, StorageContainer.getDecomposer(type) != null ? type : null);

        this.cacheRetrieve(path, list);
        return list;
    }

    /**
     * Retrieves a List directly from the data source asynchronously.
     *
     * @see ArrayContainer#retrieveList(Class, String)
     * @see ArrayContainer#retrieveListOrElseAsync(Class, String, List)
     *
     * @param type The class type of Objects in the List.
     * @param path The path to the array list.
     * @param <T> The type ob the Objects in the list.
     * @return A future, that when completed will contain the List of retrieved Objects.
     */
    public <T> CompletableFuture<List<T>> retrieveListAsync(Class<T> type, String path) {
        return CompletableFuture.supplyAsync(() -> this.retrieveList(type, path));
    }

    /**
     * Retrieves a List directly from the data source, or a provided list if the path doesn't exist or returns null.
     *
     * Please note that this method is Thread blocking.
     *
     * @see ArrayContainer#retrieveListOrElseAsync(Class, String, List)
     *
     * @param type The class type of Objects in the List.
     * @param path The path to the array list.
     * @param orElse The list to return if no list was found at the provided path.
     * @param <T> The type ob the Objects in the list.
     * @return The List of retrieved Objects.
     */
    public <T> List<T> retrieveListOrElse(Class<T> type, String path, List<T> orElse) {
        List<T> list = this.retrieveList(type, path);
        return (list == null) ? orElse : list;
    }

    /**
     * Retrieves a List directly from the data source asynchronously, or a provided list if the path doesn't exist or returns null.
     *
     * @see ArrayContainer#retrieveListOrElse(Class, String, List)
     *
     * @param type The class type of Objects in the List.
     * @param path The path to the array list.
     * @param orElse The list to return if no list was found at the provided path.
     * @param <T> The type ob the Objects in the list.
     * @return A future, that when completed will contain the List of retrieved Objects.
     */
    public <T> CompletableFuture<List<T>> retrieveListOrElseAsync(Class<T> type, String path, List<T> orElse) {
        CompletableFuture<List<T>> future = this.retrieveListAsync(type, path);
        future.thenAccept(list -> {
            future.complete(list == null ? orElse : list);
        });
        return future;
    }

    /**
     * Helper method for retrieving Lists with nested DecomposedObjects.
     * Will return the RecomposedObject from a Handler and decomposer type.
     *
     * @param handler The Handler to get the nested recomposed object from
     * @param decomposerType The type of the recomposed object
     * @return The RecomposedObject that was retrieved.
     */
    protected Object handleNestedObject(H handler, Class<?> decomposerType) {
        StorageDecomposer<?> decomposer = StorageContainer.getDecomposer(decomposerType);

        if(decomposer == null) return null;
        return handleRetrieveList(handler, decomposerType).get(0);
    }

    /**
     * Determines if a class can be used within a Collection or Array to be stored.
     * This supports Strings and Primitives by default, but can be overridden to support other types.
     *
     * @param clazz The type to check.
     * @return If the specified class is able to be stored in a list.
     */
    public boolean isListSupported(Class<?> clazz) {
        return clazz != null && clazz.isPrimitive()
                || clazz == String.class
                || StorageAPI.getWrappers().containsValue(clazz);
    }

    /**
     * Returns if the object provided is a {@link Collection} or an Array.
     * @param obj The object to check.
     * @return If the object is a Collection or Array.
     */
    public boolean isList(Object obj) {
        return obj != null && obj.getClass().isArray() || obj instanceof Collection<?>;
    }

    /**
     * Returns if the provided list is type of Primitive or is a String.
     *
     * If the provided object is not a list, false will be returned.
     *
     * All values must be of type primitive or String, if a single element fails,
     * then this method will return false.
     *
     * @param list The list to check
     * @return If the list contains primitive types, primitive wrappers or Strings.
     */
    public boolean isPrimitiveList(Object list) {
        if(!isList(list)) return false;

        Predicate<Object> predicate = (obj) -> obj.getClass() == String.class || obj.getClass().isPrimitive() || StorageAPI.getWrappers().containsValue(obj.getClass());

        if(list instanceof Collection<?> collection) {
            return collection.stream().allMatch(predicate);
        } else {
            return !Arrays.stream((Object[]) list).allMatch(predicate);
        }
    }

    /**
     * Returns a {@link Stream} of objects from an Array or {@link Collection} from a provided arbitrary object.
     * Returns an empty stream if the provided Object is null, or a Stream of the passed Object if it is not a List.
     *
     * @param list The Object to attempt to get the Stream for.
     * @return The Stream of the Array/Collection.
     */
    @SuppressWarnings("unchecked")
    protected Stream<Object> getListStream(Object list) {
        if(list == null) return Stream.empty();
        if(!isList(list)) return Stream.of(list);
        if(list instanceof Collection<?> c) return (Stream<Object>) c.stream();
        return Arrays.stream((Object[]) list);
    }

    /**
     * Handles the storing of a List into the container.
     * The list must not contain any classes or objects that cannot be stored.
     * e.g. They must be primitives or a have a {@link me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer}.
     *
     * All objects will have {@link ArrayContainer#handleListObject()} called on them with the handle.
     *
     * @see ArrayContainer#getRetrieveListHandler(String)
     * @see ArrayContainer#handleListObject()
     *
     * @param data The List to store, can be either an Array or a Collection.
     * @return The handler that can be stored natively into the container.
     */
    protected H handleList(Object data) {
        //Verify the list can be saved.
        this.getListStream(data).forEach(obj -> {
            if(obj == null) {
                throw new IllegalArgumentException("List cannot be stored with null elements!");
            } else if (!this.isListSupported(obj.getClass()) && !isList(data)) {
                throw new IllegalArgumentException(obj.getClass() + " is not a supported storage class for a list within this container!");
            }
        });

        //Get the Object handler for the container.
        H handler = this.getStoreListHandler();

        //Function to recursively adding StorageDecomposers to the handlerObject list.
        Recursive<Function<Triple<String, Object, StorageDecomposer<?>>, List<Pair<String, Object>>>> recursive = new Recursive<>();
        recursive.function = (triple) -> {
            List<Pair<String, Object>> temp = new ArrayList<>();
            StringBuilder currentPath = new StringBuilder(triple.getLeft());
            StorageDecomposer<?> composer = triple.getRight();

            for (String decomposedPath : composer.applyDecompose(triple.getMiddle()).getDecomposedMap().keySet()) {
                Object storedObject = composer.applyDecompose(triple.getMiddle()).getDecomposedMap().get(decomposedPath);
                currentPath.append(decomposedPath);

                if (storedObject != null && StorageContainer.getDecomposer(storedObject.getClass()) != null) {
                    StorageDecomposer<?> nestedDecomposer = StorageContainer.getDecomposer(storedObject.getClass());
                    currentPath.append(".");

                    //Check for recursive storage
                    if(composer == nestedDecomposer) {
                        throw new IllegalStateException("StorageDecomposer class " + storedObject.getClass().getName() + " cannot store itself, as this will cause infinite recursive storage.");
                    }

                    temp.addAll(recursive.function.apply(new Triple<>(currentPath.toString(), storedObject, nestedDecomposer)));
                } else if(storedObject != null && this.isList(storedObject)) {
                    temp.add(new Pair<>(currentPath.toString(), handleList(storedObject)));
                } else {
                    temp.add(new Pair<>(currentPath.toString(), storedObject));
                }
                currentPath = new StringBuilder(triple.getLeft());
            }
            return temp;
        };

        //For each object in the list, attempt to add the decomposed object or the normal object to the handleObject list.
        this.getListStream(data).forEach(obj -> {
            List<Pair<String, Object>> storage = new ArrayList<>();
            StorageDecomposer<?> decomp = StorageContainer.getDecomposer(obj.getClass());

            if (decomp == null || this.isPrimitiveList(obj)) {
                storage.add(new Pair<>(null, obj));
            } else if (isSupported(obj.getClass())) {
                storage.addAll(recursive.function.apply(new Triple<>("", obj, decomp)));
            } else {
                throw new IllegalArgumentException(obj.getClass().getName() + " was found in List and is not supported!");
            }

            this.handleListObject().accept(handler, storage);
        });

        return handler;
    }

    @Override
    public boolean isSupported(Class<?> clazz) {
        return super.isSupported(clazz) || clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }

    /**
     * Inner class used for recursively calling a {@link java.util.function.Function}.
     *
     * @param <T> The Type, generally should be a Function.
     */
    private static class Recursive<T> {
        public T function;
    }
}
