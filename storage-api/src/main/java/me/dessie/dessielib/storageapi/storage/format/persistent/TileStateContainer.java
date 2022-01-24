package me.dessie.dessielib.storageapi.storage.format.persistent;

import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataHolder;

public class TileStateContainer extends PDContainer {

    private final TileState state;

    public TileStateContainer(TileState state) {
        this(state, new StorageSettings());
    }

    public TileStateContainer(Block block) {
        this(block, new StorageSettings());
    }

    public TileStateContainer(TileState state, StorageSettings settings) {
        super(settings);
        this.state = state;
    }

    public TileStateContainer(Block block, StorageSettings settings) {
        super(settings);

        if(block.getState() instanceof TileState state) {
            this.state = state;
        } else throw new IllegalArgumentException("BlockState is not a TileState!");
    }

    @Override
    public PersistentDataHolder getHolder() {
        return this.state;
    }
}
