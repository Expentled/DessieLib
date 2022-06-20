package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import net.minecraft.nbt.*;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * An abstract {@link StorageContainer} that stores using the {@link PersistentDataContainer} from Spigot.
 *
 * Currently, Chunks, Entities, ItemStacks and TileStates are implemented.
 *
 * @see ChunkContainer
 * @see EntityContainer
 * @see ItemStackContainer
 * @see TileStateContainer
 */
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

    /**
     * Creates a PDContainer that can be stored to and retrieved from.
     * This will use the default settings in {@link StorageSettings}.
     */
    protected PDContainer() {super(new StorageSettings());}

    /**
     * Creates a PDContainer that can be stored to and retrieved from.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param settings The StorageSettings for this Container.
     */
    protected PDContainer(StorageSettings settings) {
        super(settings);
    }

    /**
     * The {@link PersistentDataHolder} that this Container supports.
     *
     * @see ChunkContainer for storing data within {@link org.bukkit.Chunk}s
     * @see EntityContainer for storing data within {@link org.bukkit.entity.Entity}s
     * @see ItemStackContainer for storing data within {@link org.bukkit.inventory.ItemStack}s
     * @see TileStateContainer for storing data within {@link org.bukkit.block.TileState}s
     *
     * @return The PersistentDataHolder that will be used to read/write data to.
     */
    public abstract PersistentDataHolder getHolder();

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
                if(tag == null) return null;

                return registry.extract(tagTypes.get(tag.getClass()), tag);
            }

            return null;
        });
    }

    @Override
    public Set<String> getKeys(String path) {
        return new HashSet<>();
    }

    @Override
    public boolean isSupported(Class<?> clazz) {
        if(tagTypes.containsValue(clazz)) return true;
        if(getDecomposer(clazz) != null) return true;

        return false;
    }
}
