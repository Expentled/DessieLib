package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link ItemStack}'s {@link ItemMeta}
 */
public class ItemStackContainer extends PDContainer {

    private final ItemMeta meta;

    /**
     * Creates a container that stores data using an {@link ItemStack}'s {@link ItemMeta}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param item The ItemStack to store/retrieve data from.
     */
    public ItemStackContainer(ItemStack item) {
        this(item, new StorageSettings());
    }

    /**
     * Creates a container that stores data using an {@link ItemStack}'s {@link ItemMeta}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param item The ItemStack to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public ItemStackContainer(ItemStack item, StorageSettings settings) {
        super(settings);
        Objects.requireNonNull(item, "Item cannot be null!");
        if(item.getItemMeta() == null) throw new IllegalArgumentException("ItemMeta cannot be null!");

        this.meta = item.getItemMeta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.meta;
    }
}
