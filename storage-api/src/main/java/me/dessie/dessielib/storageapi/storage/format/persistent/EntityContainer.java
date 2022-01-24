package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;

public class EntityContainer extends PDContainer {

    private final Entity entity;

    public EntityContainer(Entity entity) {
        this(entity, new StorageSettings());
    }

    public EntityContainer(Entity entity, StorageSettings settings) {
        super(settings);
        this.entity = entity;
    }

    @Override
    public PersistentDataHolder getHolder() {
        return this.entity;
    }
}
