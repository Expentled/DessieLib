package me.dessie.dessielib.core.events.slot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SlotUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private int slot;
    private ItemStack newItem;
    private ItemStack oldItem;
    private UpdateType updateType;
    private Inventory inventory;

    //Used for InventoryClickEvents.
    //InventoryClickEvent always triggers a tick later, so sometimes items are returning null unexpectedly.
    //These can be used to access these "null" items.
    private Inventory newInventory;
    private Inventory oldInventory;

    public SlotUpdateEvent(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType, Inventory newInv, Inventory oldInv) {
        super(who);

        this.inventory = inventory;
        this.slot = slot;
        this.newItem = newItem;
        this.oldItem = oldItem;
        this.updateType = updateType;
        this.newInventory = newInv;
        this.oldInventory = oldInv;
    }

    public SlotUpdateEvent(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType) {
        this(who, inventory, slot, newItem, oldItem, updateType, null, null);
    }

    /**
     * Fires a SlotUpdateEvent
     *
     * @param who The Player
     * @param inventory The Inventory that was updated
     * @param slot The slot that was updated
     * @param newItem The new ItemStack in that slot
     * @param oldItem The old ItemStack that was in the slot
     * @param updateType The update type of this event
     * @param newInv The updated Inventory when the UpdateType is {@link UpdateType#INVENTORY_INTERACT}
     * @param oldInv The old Inventory when the UpdateType is {@link UpdateType#INVENTORY_INTERACT}
     * @return The SlotUpdateEvent instance
     */
    public static SlotUpdateEvent attemptFire(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType, Inventory newInv, Inventory oldInv) {
        SlotUpdateEvent event = new SlotUpdateEvent(who, inventory, slot, newItem, oldItem, updateType, newInv, oldInv);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Fires a SlotUpdateEvent
     *
     * @param who The Player
     * @param inventory The Inventory that was updated
     * @param slot The slot that was updated
     * @param newItem The new ItemStack in that slot
     * @param oldItem The old ItemStack that was in the slot
     * @param updateType The update type of this event
     * @return The SlotUpdateEvent instance
     */
    public static SlotUpdateEvent attemptFire(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType) {
        SlotUpdateEvent event = new SlotUpdateEvent(who, inventory, slot, newItem, oldItem, updateType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * @return The slot that was updated.
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return The new ItemStack that is in the slot.
     */
    public ItemStack getNewItem() { return newItem; }


    /**
     * @return The old ItemStack that was in the slot.
     */
    public ItemStack getOldItem() { return oldItem; }

    /**
     * @return The {@link UpdateType} of this event.
     */
    public UpdateType getUpdateType() { return updateType; }

    /**
     * @return The Inventory that was updated.
     */
    public Inventory getInventory() { return inventory; }

    /**
     * Will return null if UpdateType is not INVENTORY_INTERACT
     * @return The new Inventory with the new contents
     */
    public Inventory getNewInventory() { return newInventory; }

    /**
     * Will return null if UpdateType is not INVENTORY_INTERACT
     * @return The old Inventory with the old contents
     */
    public Inventory getOldInventory() { return oldInventory; }

    /**
     * @return If the Inventory that was updated belongs to a player.
     */
    public boolean isPlayerInventory() { return this.getInventory().getType() == InventoryType.PLAYER; }

    /**
     * @return If the slot that was updated is the offhand slot.
     */
    public boolean isOffhand() { return isPlayerInventory() && this.getSlot() == 40; }

    /**
     * @return If the slot that was updated is the boots armor slot.
     */
    public boolean isBoots() { return isPlayerInventory() && this.getSlot() == 36; }

    /**
     * @return If the slot that was updated is the leggings armor slot.
     */
    public boolean isLeggings() { return isPlayerInventory() && this.getSlot() == 37; }

    /**
     * @return If the slot that was updated is the chestplate armor slot.
     */
    public boolean isChestplate() { return isPlayerInventory() && this.getSlot() == 38; }

    /**
     * @return If the slot that was updated is the boots helmet slot.
     */
    public boolean isHelmet() { return isPlayerInventory() && this.getSlot() == 39; }

    /**
     * @return If the slot that was updated was an armor slot at all
     */
    public boolean isArmor() { return isPlayerInventory() && (isBoots() || isLeggings() || isChestplate() || isHelmet()); }

    /**
     * @return If the slot that was updated is currently the Player's main hand.
     */
    public boolean isInMainHand() { return isPlayerInventory() && getPlayer().getInventory().getHeldItemSlot() == this.getSlot(); }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
