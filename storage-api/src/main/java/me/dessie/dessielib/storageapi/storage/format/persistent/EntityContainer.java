package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link Entity}
 */
public class EntityContainer extends PDContainer {

    private final Entity entity;

    /**
     * Creates a container that stores data using a {@link Entity}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param entity The Entity to store/retrieve data from.
     */
    public EntityContainer(Entity entity) {
        this(entity, new StorageSettings());
    }

    /**
     * Creates a container that stores data using a {@link Entity}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param entity The Entity to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public EntityContainer(Entity entity, StorageSettings settings) {
        super(settings);
        Objects.requireNonNull(entity, "Entity cannot be null!");

        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.entity;
    }
}
