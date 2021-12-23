package me.dessie.dessielib.enchantmentapi.activator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * An Enchantment will trigger its abilities (for example {@link me.dessie.dessielib.enchantmentapi.CEnchantment#onBlockBreak(BiConsumer)})
 *  only when an activator is also satisfied.
 *
 *  For example, if you have a pickaxe with an enchantment that instantly destroys a block when you right click, but only in the offhand of the player
 *  Then your EnchantmentActivator should only have {@link Activator#OFFHAND}.
 */
public class EnchantmentActivator {

    private List<Activator> activators = new ArrayList<>();

    /**
     * @param activators The {@link Activator}s that will allow the Enchantment to trigger.
     */
    public EnchantmentActivator(Activator... activators) {
        this.addActivators(activators);
    }

    /**
     * @return The {@link Activator}s that must be satisfied to trigger the Enchantment.
     */
    public List<Activator> getActivators() {
        return activators;
    }

    /**
     * Adds additional {@link Activator}s to the current list.
     *
     * @param activators The Activators to add.
     */
    public void addActivators(Activator... activators) {
        this.activators.addAll(Arrays.asList(activators));
    }

    /**
     * @param activator The {@link Activator} to check
     * @return If a provided Activator satisfies this EnchantmentActivator.
     */
    public boolean hasActivator(Activator activator) {
        if(activator == Activator.MAINHAND) {
            return this.getActivators().contains(Activator.MAINHAND)
                    || this.getActivators().contains(Activator.HAND)
                    || this.getActivators().contains(Activator.ALL);
        } else if(activator == Activator.OFFHAND) {
            return this.getActivators().contains(Activator.OFFHAND)
                    || this.getActivators().contains(Activator.HAND)
                    || this.getActivators().contains(Activator.ALL);
        }

        return this.getActivators().contains(activator) || this.getActivators().contains(Activator.ALL);
    }

    /**
     * @param entity The Entity to retrieve the ItemStacks for.
     * @return A list of ItemStacks that satisfy an {@link Activator} for this EnchantmentActivator.
     */
    public List<ItemStack> getItems(LivingEntity entity) {
        List<ItemStack> items = new ArrayList<>();
        if(entity.getEquipment() == null) return items;

        EntityEquipment equipment = entity.getEquipment();

        //If it has Inventory activator, this item will be given twice, so we only give the mainhand
        //if they aren't going to request inventory.
        if((hasActivator(Activator.MAINHAND) || hasActivator(Activator.HAND)) && !hasActivator(Activator.INVENTORY)) {
            items.add(equipment.getItemInMainHand());
        }

        if(hasActivator(Activator.OFFHAND) || hasActivator(Activator.HAND)) {
            items.add(equipment.getItemInOffHand());
        }

        if(hasActivator(Activator.ARMOR)) {
            items.addAll(Arrays.asList(equipment.getArmorContents()));
        }

        if(hasActivator(Activator.INVENTORY)) {
            if(entity instanceof Player player) {
                PlayerInventory inv = player.getInventory();
                for(int i = 0; i < 36; i++) {
                    items.add(inv.getItem(i));
                }
            }
        }

        return items;
    }


}
