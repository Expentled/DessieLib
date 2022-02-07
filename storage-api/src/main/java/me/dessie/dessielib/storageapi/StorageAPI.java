package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.core.utils.ClassUtil;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.decomposition.annotations.RecomposeConstructor;
import me.dessie.dessielib.storageapi.storage.decomposition.annotations.Stored;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

        registerAnnotatedDecomposers();
    }

    /**
     * @return If InventoryAPI has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return The plugin that registered the InventoryAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }


    @SuppressWarnings("unchecked")
    private static void registerAnnotatedDecomposers() {
        for(Class<Object> clazz : ClassUtil.getClasses(Object.class, StorageAPI.getPlugin(), null)) {
            List<Field> decomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Stored.class)).toList();
            List<Field> recomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Stored.class) && f.getAnnotation(Stored.class).recompose()).toList();
            Constructor<Object> constructor = (Constructor<Object>) Arrays.stream(clazz.getDeclaredConstructors()).filter(c -> c.isAnnotationPresent(RecomposeConstructor.class)).findFirst().orElse(null);

            if(constructor == null) {
                StorageContainer.addStorageDecomposer(new StorageDecomposer<>(clazz, (obj) -> {
                    DecomposedObject object = new DecomposedObject();
                    for(Field f : decomposeFields) {
                        try {
                            f.setAccessible(true);
                            object.addDecomposedKey(f.getAnnotation(Stored.class).storeAs().equals("") ? f.getName() : f.getAnnotation(Stored.class).storeAs(), f.get(obj));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    return object;
                }));
            } else {
                constructor.setAccessible(true);

                StorageContainer.addStorageDecomposer(new StorageDecomposer<>(clazz, (obj) -> {
                    DecomposedObject object = new DecomposedObject();
                    for (Field f : decomposeFields) {
                        try {
                            f.setAccessible(true);
                            object.addDecomposedKey(f.getAnnotation(Stored.class).storeAs().equals("") ? f.getName() : f.getAnnotation(Stored.class).storeAs(), f.get(obj));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    return object;
                }, (container, recompose) -> {
                    for (Field f : recomposeFields) {
                        recompose.addRecomposeKey(f.getAnnotation(Stored.class).storeAs().equals("") ? f.getName() : f.getAnnotation(Stored.class).storeAs(), container::retrieveAsync);
                    }

                    return recompose.onComplete(completed -> {
                        List<Object> args = new ArrayList<>();

                        for (Field f : recomposeFields) {
                            args.add(completed.getCompletedObject(f.getAnnotation(Stored.class).storeAs().equals("") ? f.getName() : f.getAnnotation(Stored.class).storeAs()));
                        }

                        if(args.size() != constructor.getParameterCount()) {
                            throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz + ". Constructor param count and recompose fields are not the same size.");
                        } else if(!Arrays.equals(args.stream().map(Object::getClass).toArray(), Arrays.stream(constructor.getParameters()).map((param -> param.getType().isPrimitive() ? wrappers.get(param.getType()) : param.getType())).toArray())) {
                            throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz + ". Constructor and provided arguments do not match. (Make sure your fields are ordered in the same way as your constructor.)");
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
}
