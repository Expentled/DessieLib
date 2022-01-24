package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;

public class ItemStackContainer extends PDContainer {

    private final ItemMeta meta;

    public ItemStackContainer(ItemMeta meta) {
        this(meta, new StorageSettings());
    }

    public ItemStackContainer(ItemStack item) {
        this(item, new StorageSettings());
    }

    public ItemStackContainer(ItemMeta meta, StorageSettings settings) {
        super(settings);
        this.meta = meta;
    }

    public ItemStackContainer(ItemStack item, StorageSettings settings) {
        super(settings);
        this.meta = item.getItemMeta();
    }

    @Override
    public PersistentDataHolder getHolder() {
        return this.meta;
    }
}
