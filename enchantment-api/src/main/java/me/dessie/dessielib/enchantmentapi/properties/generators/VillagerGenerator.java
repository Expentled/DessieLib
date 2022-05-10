package me.dessie.dessielib.enchantmentapi.properties.generators;

import org.bukkit.enchantments.Enchantment;

import java.util.Random;

/**
 * Internal class used for handling custom Villager Trader logic.
 */
public class VillagerGenerator {

    static Enchantment getRandomEnchantment() {
        return Enchantment.values()[new Random().nextInt(Enchantment.values().length)];
    }

    static int getRandomLevel(Enchantment enchantment) {
        return new Random().nextInt(enchantment.getMaxLevel()) + 1;
    }

    static int getRandomCost(int level, Enchantment enchantment) {
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
