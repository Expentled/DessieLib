package me.dessie.dessielib.enchantmentapi.activator;

public enum Activator {
    /**
     * Activates if any of the Activators are met.
     */
    ALL,

    /**
     * Activates if the Enchantment is being held in either mainhand or offhand.
     */
    HAND,

    /**
     * Activates if the Enchantment is being held in the mainhand.
     */
    MAINHAND,

    /**
     * Activates if the Enchantment is being held in the offhand.
     */
    OFFHAND,

    /**
     * Activates if the Enchantment is worn on an armor piece and is equipped.
     */
    ARMOR,

    /**
     * Activates if the Enchantment is in the inventory of the entity.
     */
    INVENTORY
}
