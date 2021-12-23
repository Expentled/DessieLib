package me.dessie.dessielib.enchantmentapi.utils;

import org.bukkit.enchantments.Enchantment;

import java.util.Random;

/**
 * Internal class used for handling custom Villager Trader logic.
 */
public class VillagerGenerator {

    public static Enchantment getRandomEnchantment() {
        return Enchantment.values()[new Random().nextInt(Enchantment.values().length)];
    }

    public static int getRandomLevel(Enchantment enchantment) {
        return new Random().nextInt(enchantment.getMaxLevel()) + 1;
    }

    public static int getRandomCost(int level, Enchantment enchantment) {
        int min = 8;
        int max = 32;

        for(int i = 1; i < level; i++) {
            min += 3;
            max += 13;
        }

        int cost = new Random().nextInt(max-min) + min;
        if(enchantment.isTreasure()) {
            cost *= 2;
        }

        return Math.min(cost, 64);
    }
}
