package me.dessie.dessielib.enchantmentapi.utils;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Internal class used for handling custom Anvil logic.
 */
public class AnvilGenerator {

    public static void removeConflictingVanilla(ItemStack target, ItemStack sacrifice, ItemStack result) {
        //Gets the conflicting vanilla enchantments
        Set<Enchantment> badVanillaEnchantments = getConflictingVanilla(target, sacrifice);

        //Remove the conflicting vanilla enchantments
        for(Enchantment enchantment : badVanillaEnchantments) {
            result.removeEnchantment(enchantment);
        }
    }

    public static int doAnvilEnchant(Player player, Set<CEnchantment> enchantments, ItemStack target, ItemStack sacrifice, ItemStack result) {
        int cost = 0;
        for(CEnchantment enchantment : enchantments) {
            int level = getNewLevel(enchantment, target, sacrifice);
            try {
                cost += enchantment.getEnchantProperties().getAnvilMultiplier() * level;
                enchantment.enchant(result, level, player.getGameMode() == GameMode.CREATIVE, !CEnchantment.hasEnchantment(target, enchantment));
            } catch (IllegalArgumentException ignored) {}
        }
        return cost;
    }

    public static Set<Enchantment> getConflictingVanilla(ItemStack target, ItemStack sacrifice) {
        Set<Enchantment> enchantments = new HashSet<>();

        for(CEnchantment enchantment : CEnchantment.getEnchantments(target)) {
            for(Enchantment vanillaEnchantment : sacrifice.getEnchantments().keySet()) {
                if(enchantment.conflictsWith(vanillaEnchantment)) {
                    enchantments.add(vanillaEnchantment);
                }
            }
        }

        return enchantments;
    }

    public static Set<CEnchantment> getEnchantmentsToAdd(Player player, ItemStack target, ItemStack sacrifice) {
        Set<CEnchantment> enchantments = new HashSet<>();

        enchantments.addAll(CEnchantment.getEnchantments(target));

        //Make sure to only add sacrifice enchantments if they don't conflict with the target enchantments.
        enchantments.addAll(CEnchantment.getEnchantments(sacrifice).stream().filter(ench ->
                !ench.conflictsWith(target) && !CEnchantment.getEnchantments(target).stream().anyMatch(targetEnch -> targetEnch.conflictsWith(sacrifice)))
                .collect(Collectors.toSet()));

        //Remove any enchantments that can't be added to the target.
        if(player.getGameMode() != GameMode.CREATIVE) {
            enchantments.removeIf(enchantment -> !enchantment.canEnchantItem(target));
        }

        return enchantments;
    }

    public static int getNewLevel(CEnchantment enchantment, ItemStack target, ItemStack sacrifice) {
        int level = CEnchantment.getLevel(target, enchantment);
        int sacrificeLevel = CEnchantment.getLevel(sacrifice, enchantment);

        if (level == sacrificeLevel && level < enchantment.getMaxLevel()) {
            level++;
        } else if (level < sacrificeLevel) {
            level = sacrificeLevel;
        }

        return level;
    }

}
