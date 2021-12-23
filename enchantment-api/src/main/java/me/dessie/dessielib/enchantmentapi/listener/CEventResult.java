package me.dessie.dessielib.enchantmentapi.listener;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Used to get specific information about an {@link ItemStack}, {@link CEnchantment}, and the current level when
 * using consumer events.
 */
public class CEventResult {

    private CEnchantment enchantment;
    private ItemStack item;
    private int level;

    /**
     * @param enchantment The {@link CEnchantment} that fired the consumer event.
     * @param item The {@link ItemStack} that this event was fired for.
     * @param level The level of the {@link CEnchantment} on the ItemStack.
     */
    public CEventResult(CEnchantment enchantment, ItemStack item, int level) {
        this.enchantment = enchantment;
        this.item = item;
        this.level = level;
    }

    /**
     * @return The enchantment that fired the event
     */
    public CEnchantment getEnchantment() {
        return enchantment;
    }

    /**
     * @return The ItemStack that the enchantment is attached to
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * @return The level of the enchantment
     */
    public int getLevel() {
        return level;
    }
}
