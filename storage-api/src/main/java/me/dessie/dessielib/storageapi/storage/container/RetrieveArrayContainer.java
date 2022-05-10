package me.dessie.dessielib.storageapi.storage.container;

import me.dessie.dessielib.storageapi.storage.decomposition.RecomposedObject;
import me.dessie.dessielib.storageapi.storage.format.flatfile.JSONContainer;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * An {@link ArrayContainer} that will, by default, handle retrieval of array lists.
 * This class will require some extra functionality methods to help make the {@link RetrieveArrayContainer#handleRetrieveList(Object, Class)}
 * method work generically.
 *
 * If you want to implement this method yourself, (which is not really recommended), use {@link ArrayContainer}
 *
 * @param <H> The Handler type, for storing and retrieving lists from the structure.
 *  *         Generally, this will be some natively supported array implementation such as {@link com.google.gson.JsonArray} or {@link ArrayList}.
 * @param <N> The Nested object type, which will be utilized for detecting when a StorageDecomposer
 *            was found within an Array.
 */
public abstract class RetrieveArrayContainer<H, N> extends ArrayContainer<H> {
    /**
     * Returns the function for how to add a nested object into the handler list.
     *
     * @return The BiConsumer for adding the object into the list.
     */
    protected abstract BiConsumer<H, N> add();

    /**
     * Returns the {@link Stream} of objects for the provided Handler.
     *
     * @param handler The Handler to get the Stream for.
     * @return The Object Stream of the Handler.
     */
    protected abstract Stream<Object> getHandlerStream(H handler);

    /**
     * Returns the {@link Stream} of keys for the nested object within an array.
     * These keys are equivalent to the {@link me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer}'s keys.
     *
     * @param nested The Nested Object to get the Stream keys for.
     * @return The String Stream for all the keys.
     */
    protected abstract Stream<String> getNestedKeys(N nested);

    /**
     * Returns if a provided object is instance of the Handler.
     * This generally should be
     *
     * <pre>
     *     return object instanceof H;
     * </pre>
     *
     * @param object The Object to check the instance for.
     * @return If the provided Object is a type of Handler.
     */
    protected abstract boolean isHandler(Object object);

    /**
     * Returns if a provided object is instance of the Nested Object.
     * This generally should be
     *
     * <pre>
     *     return object instanceof N;
     * </pre>
     *
     * @param object The Object to check the instance for.
     * @return If the provided Object is a type of Nested.
     */
    protected abstract boolean isNested(Object object);

    /**
     * Gets an Object from the Nested Object from the provided key.
     *
     * @param nested The Nested object to get from.
     * @param key The key to retrieve from.
     * @return The object at the provided key in the nested object.
     */
    protected abstract Object getObjectFromNested(N nested, String key);

    /**
     * "Casts" the provided object to a primitive one.
     * This generally will be the exact same return value, and you can return object if
     * no other logic is necessary.
     *
     * For example though, in {@link JSONContainer}, the object needs to be parsed using the Gson instance.
     *
     * @param object The object to get as a primitive.
     * @return The correct primitive object.
     */
    protected abstract Object getPrimitive(Object object);

    /**
     * Creates a RetrieveArrayContainer with a default {@link StorageSettings}.
     *
     * @see StorageSettings#StorageSettings()
     */
    public RetrieveArrayContainer() {
        super();
    }

    /**
     * Creates an RetrieveArrayContainer with the specified {@link StorageSettings}.
     *
     * @param settings The StorageSettings for the container.
     */
    public RetrieveArrayContainer(StorageSettings settings) {
        super(settings);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> handleRetrieveList(H handler, Class<T> type) {
        List<T> list = new ArrayList<>();

        this.getHandlerStream(handler).filter(this::isNested).map(obj -> (N) obj).forEach(obj -> {
            RecomposedObject<T> recomposedObject = RecomposedObject.filled(this, type);

            //Handle nested decomposers
            this.getNestedKeys(obj).forEach(key -> {
                Object fromKey = this.getObjectFromNested(obj, key);

                try {
                    if(isNested(fromKey)) {
                        H newHandler = this.getStoreListHandler();
                        this.add().accept(newHandler, (N) fromKey);
                        recomposedObject.completeObject(key, this.handleNestedObject(newHandler, recomposedObject.getType(key)));
                    } else if (isHandler(fromKey)) {
                        Class<?> nestedClass = recomposedObject.getType(key);
                        recomposedObject.completeObject(key, this.handleRetrieveList((H) fromKey, StorageContainer.getDecomposer(nestedClass) == null ? null : nestedClass));
                    } else {
                        recomposedObject.completeObject(key, fromKey);
                    }
                } catch (IllegalArgumentException ignored) {
                    //TODO Probably should remove un-used paths from the file if they're detected.
                }
            });

            list.add(recomposedObject.complete());
        });

        //Add all the non-decomposer objects (The ones that aren't nested objects or arrays).
        this.getHandlerStream(handler).filter(obj -> !this.isNested(obj)).forEach(obj -> {
            list.add((T) this.getPrimitive(obj));
        });

        return list;
    }

}
