package me.dessie.dessielib.core.events.slot;

/**
 * Used by {@link SlotUpdateEvent} to determine how a specific slot was changed.
 */
public enum UpdateType {
    UNKNOWN, ARMOR_EQUIP, ITEM_BREAK, SWAP_HAND, DROP, PICKUP, INVENTORY_INTERACT, RESULT_GENERATE, CRAFT
}
