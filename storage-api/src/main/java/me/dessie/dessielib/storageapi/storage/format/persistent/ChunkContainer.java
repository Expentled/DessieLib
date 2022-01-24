package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataHolder;

public class ChunkContainer extends PDContainer {

    private final Chunk chunk;

    public ChunkContainer(Chunk chunk) {
        this(chunk, new StorageSettings());
    }
    public ChunkContainer(Block block) {
        this(block, new StorageSettings());
    }

    public ChunkContainer(Chunk chunk, StorageSettings settings) {
        super(settings);
        this.chunk = chunk;
    }

    public ChunkContainer(Block block, StorageSettings settings) {
        super(settings);
        this.chunk = block.getChunk();
    }

    @Override
    public PersistentDataHolder getHolder() {
        return this.chunk;
    }
}
