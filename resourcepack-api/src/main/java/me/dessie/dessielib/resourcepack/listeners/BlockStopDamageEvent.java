package me.dessie.dessielib.resourcepack.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Fires when a Player stops breaking a block prematurely.
 *
 * This event will not fire unless {@link me.dessie.dessielib.resourcepack.ResourcePack#register(JavaPlugin)} has been called.
 */
public class BlockStopDamageEvent extends BlockEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private boolean cancelled = false;

    /**
     * @param player The Player that stopped breaking a block
     * @param theBlock The block that the player was breaking.
     */
    public BlockStopDamageEvent(Player player, Block theBlock) {
        super(theBlock);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
