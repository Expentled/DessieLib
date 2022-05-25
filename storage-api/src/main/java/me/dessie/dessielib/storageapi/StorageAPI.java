package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;
import me.dessie.dessielib.annotations.storageapi.StoredList;
import me.dessie.dessielib.core.utils.ClassUtil;
import me.dessie.dessielib.storageapi.storage.container.ArrayContainer;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Main class for registering StorageAPI.
 */
public class StorageAPI {

    private static JavaPlugin plugin;
    private static boolean registered;

    //Used for registering annotated
    private static final Map<Class<?>, Class<?>> wrappers = new HashMap<>() {{
        put(int.class, Integer.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(boolean.class, Boolean.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, void.class);
    }};

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        Objects.requireNonNull(yourPlugin, "Plugin cannot be null!");

        plugin = yourPlugin;
        registered = true;

        //Register the SectionSerializable class so it can be properly de-serialized.
        ConfigurationSerialization.registerClass(SectionSerializable.class);

        registerAnnotatedDecomposers();
    }

    /**
     * @return If StorageAPI has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return The plugin that registered the StorageAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    @SuppressWarnings("unchecked")
    private static void registerAnnotatedDecomposers() {
        List<Class<Object>> classes = ClassUtil.getClasses(Object.class, StorageAPI.getPlugin(), null)
                .stream().filter(clazz -> StorageContainer.getDecomposer(clazz) == null).toList();

        for(Class<Object> clazz : classes) {
            //Grab all the fields that have either Stored or StoredList annotations.
            List<Field> decomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Stored.class) || f.isAnnotationPresent(StoredList.class)).toList();
            List<Field> recomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> (f.isAnnotationPresent(Stored.class) && f.getAnnotation(Stored.class).recompose()) || (f.isAnnotationPresent(StoredList.class) && f.getAnnotation(StoredList.class).recompose())).toList();
            Constructor<Object> constructor = (Constructor<Object>) Arrays.stream(clazz.getDeclaredConstructors()).filter(c -> c.isAnnotationPresent(RecomposeConstructor.class)).findFirst().orElse(null);

            //If there's no storage fields, and no recompose constructor, skip the class.
            if(decomposeFields.isEmpty() && constructor == null) continue;

            if(constructor == null) {
                StorageContainer.addStorageDecomposer(new StorageDecomposer<>(clazz, getGenericDecompose(decomposeFields)));
            } else {
                constructor.setAccessible(true);

                StorageContainer.addStorageDecomposer(new StorageDecomposer<>(clazz, getGenericDecompose(decomposeFields), (container, recompose) -> {
                    for (Field f : recomposeFields) {
                        String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                                : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                        Class<?> type = f.isAnnotationPresent(Stored.class) ? f.getType() : f.getAnnotation(StoredList.class).type();

                        if(f.isAnnotationPresent(Stored.class)) {
                            recompose.addRecomposeKey(path, type, container::retrieveAsync);
                        } else if(container instanceof ArrayContainer<?> arrayContainer) {
                            recompose.addRecomposeKey(path, type, (p) -> {
                                return (CompletableFuture<Object>) (CompletableFuture<?>) arrayContainer.retrieveListAsync(type, p);
                            });
                        }
                    }

                    return recompose.onComplete(completed -> {
                        List<Object> args = new ArrayList<>();

                        for (Field f : recomposeFields) {
                            String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                                    : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                            args.add(completed.getCompletedObject(path));
                        }

                        RecomposeConstructor annotation = constructor.getAnnotation(RecomposeConstructor.class);
                        if (args.size() != constructor.getParameterCount()) {
                            if(annotation.throwError()) {
                                throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz.getSimpleName() + ". Constructor param count and recompose fields are not the same size.");
                            } else return null;

                        } else {
                            //Check if the args provided and the params needed are the same types.
                            Class<?>[] argsArray = args.stream().map(obj -> obj == null ? null : obj.getClass()).toList().toArray(new Class<?>[0]);
                            Class<?>[] paramArray = Arrays.stream(constructor.getParameters()).map((param -> param.getType().isPrimitive() ? wrappers.get(param.getType()) : param.getType())).toList().toArray(new Class<?>[0]);

                            for(int i = 0; i < argsArray.length; i++) {
                                if((!annotation.allowNull() && argsArray[i] == null)) {
                                    return null;
                                } else if(argsArray[i] != null && argsArray[i] != paramArray[i] && !paramArray[i].isAssignableFrom(argsArray[i])) {
                                    if(annotation.throwError()) {
                                        throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz.getSimpleName() + ". Constructor and provided arguments do not match. Expected " + Arrays.toString(paramArray) + " but got " + Arrays.toString(argsArray));
                                    } else return null;
                                }
                            }
                        }

                        try {
                            return constructor.newInstance(args.toArray());
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
                }));
            }
        }
    }

    /**
     * Returns the map of primitive classes to their respected Wrapped classes
     *
     * E.g. int -> Integer
     *
     * @return The map for primitives to wrappers.
     */
    public static Map<Class<?>, Class<?>> getWrappers() {
        return wrappers;
    }

    private static Function<Object, DecomposedObject> getGenericDecompose(List<Field> decomposeFields) {
        return (obj) -> {
            DecomposedObject object = new DecomposedObject();
            for(Field f : decomposeFields) {
                try {
                    f.setAccessible(true);
                    String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                            : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                    object.addDecomposedKey(path, f.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return object;
        };
    }

}
