package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import net.minecraft.nbt.*;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_18_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class PDContainer extends StorageContainer {

    //The maps for the TagType -> Class. For some reason this isn't already stored in the TagType itself?? why??
    private final static Map<Class<? extends Tag>, Class<?>> tagTypes = new HashMap<>() {{
        put(ByteTag.class, Byte.class);
        put(ShortTag.class, Short.class);
        put(IntTag.class, Integer.class);
        put(LongTag.class, Long.class);
        put(FloatTag.class, Float.class);
        put(DoubleTag.class, Double.class);
        put(StringTag.class, String.class);
        put(ByteArrayTag.class, byte[].class);
        put(IntArrayTag.class, int[].class);
        put(LongArrayTag.class, long[].class);
        put(ListTag.class, PersistentDataContainer[].class);
        put(CompoundTag.class, PersistentDataContainer.class);
    }};

    protected PDContainer() {
        super(new StorageSettings());
    }
    protected PDContainer(StorageSettings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            NamespacedKey key = new NamespacedKey(StorageAPI.getPlugin(), path);

            PersistentDataContainer container = this.getHolder().getPersistentDataContainer();

            //Find the correct DataType.
            Arrays.stream(PersistentDataType.class.getDeclaredFields()).forEach(field -> {
                Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> clazz = (Class<?>) type;

                if(!clazz.isAssignableFrom(data.getClass())) return;

                //Set the data.
                try {
                    container.set(key, (PersistentDataType) field.get(null), clazz.cast(data));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook(path -> {
            NamespacedKey key = new NamespacedKey(StorageAPI.getPlugin(), path);
            org.bukkit.persistence.PersistentDataContainer container = this.getHolder().getPersistentDataContainer();
            container.remove(key);
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> {
            NamespacedKey key = new NamespacedKey(StorageAPI.getPlugin(), path);
            PersistentDataContainer container = this.getHolder().getPersistentDataContainer();

            if(container instanceof CraftPersistentDataContainer craftContainer) {
                CraftPersistentDataTypeRegistry registry = null;
                try {
                    Field registryField = craftContainer.getClass().getDeclaredField("registry");
                    registryField.setAccessible(true);
                    registry = (CraftPersistentDataTypeRegistry) registryField.get(craftContainer);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if(registry == null) return null;

                Tag tag = craftContainer.getRaw().get(key.toString());
                return registry.extract(tagTypes.get(tag.getClass()), tag);
            }

            return null;
        });
    }

    public abstract PersistentDataHolder getHolder();
}
