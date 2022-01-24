package me.dessie.dessielib.storageapi.storage.format;

public enum StorageFormat {

    /**
     * Saves/Retrieves data in a MySQL database.
     */
    MYSQL,

    /**
     * Saves/Retrieves data in a YAML file.
     */
    YAML,

    /**
     * Saves/Retrieves data in a JSON file.
     */
    JSON,

    /**
     * Saves/Retrieves data in a {@link org.bukkit.block.TileState} using {@link org.bukkit.persistence.PersistentDataHolder}.
     *
     * @see org.bukkit.persistence.PersistentDataHolder for more information on how this method works.
     * @see org.bukkit.persistence.PersistentDataContainer
     */
    TILE_STATE_PERSISTENCE,

    /**
     * Saves/Retrieves data in a {@link org.bukkit.Chunk} using {@link org.bukkit.persistence.PersistentDataHolder}.
     *
     * @see org.bukkit.persistence.PersistentDataHolder for more information on how this method works.
     * @see org.bukkit.persistence.PersistentDataContainer
     */
    CHUNK_PERSISTENCE,

    /**
     * Saves/Retrieves data in a {@link org.bukkit.entity.Entity} using {@link org.bukkit.persistence.PersistentDataHolder}.
     *
     * @see org.bukkit.persistence.PersistentDataHolder for more information on how this method works.
     * @see org.bukkit.persistence.PersistentDataContainer
     */
    ENTITY_PERSISTENCE,

    /**
     * Saves/Retrieves data in a {@link org.bukkit.inventory.meta.ItemMeta} using {@link org.bukkit.persistence.PersistentDataHolder}.
     *
     * @see org.bukkit.persistence.PersistentDataHolder for more information on how this method works.
     * @see org.bukkit.persistence.PersistentDataContainer
     */
    ITEM_PERSISTENCE

}
