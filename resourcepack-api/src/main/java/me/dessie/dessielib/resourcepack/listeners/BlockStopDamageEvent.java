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
     *
     * @deprecated Since Spigot 1.18.1 {@link org.bukkit.event.block.BlockDamageAbortEvent} has been added.
     */
    @Deprecated
    public BlockStopDamageEvent(Player player, Block theBlock) {
        super(theBlock);
        this.player = player;
    }

    /**
     * Returns the {@link Player} who stopped breaking the block
     * @return The Player for this event.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Required Spigot Event method.
     * @return The HANDLERs list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
