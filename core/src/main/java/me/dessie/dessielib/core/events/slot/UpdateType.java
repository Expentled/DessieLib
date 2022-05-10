package me.dessie.dessielib.core.events.slot;

/**
 * Used by {@link SlotUpdateEvent} to determine how a specific slot was changed.
 */
public enum UpdateType {
    /**
     * The update happened in an unknown way.
     */
    UNKNOWN,

    /**
     * Used when a player equips an item in an armor slot.
     */
    ARMOR_EQUIP,

    /**
     * Used when a player's item breaks in their hand
     */
    ITEM_BREAK,

    /**
     * Used when a player swaps items to their offhand
     */
    SWAP_HAND,

    /**
     * Used when a player drops an item
     */
    DROP,

    /**
     * Used when a player picks up an item
     */
    PICKUP,

    /**
     * Used when a player interacts with an inventory, such as clicking
     */
    INVENTORY_INTERACT,

    /**
     * Used when an item is placed in a result slot
     */
    RESULT_GENERATE,

    /**
     * Used when an item is crafted, and that item is in the result slot of the crafting grid.
     */
    CRAFT
}
