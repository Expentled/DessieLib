package me.dessie.dessielib.enchantmentapi.properties.generators;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Internal class used for handling custom Enchanting Table logic.
 */
public class EnchantmentGenerator {

    private static Random random = new Random();

    /**
     * Does the random math for generating an Enchantment level.
     *
     * @param itemstack The {@link ItemStack} to enchant.
     * @param base The enchanting cost of the enchantment. (1 - 30)
     * @return The modified power for this enchant
     */
    static int getModifiedPower(ItemStack itemstack, int base) {
        int enchantability = getEnchantability(itemstack);
        int range = random.nextInt((115 - 85) + 1) + 85; //Get a number between 85 and 115.
        double modifier = range/100.0; //Map to 0.85 and 1.15

        return (int) (modifier * (base + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1) + 1));
    }

    static Map<Enchantment, Integer> getPossibleEnchantments(ItemStack item, Enchantment clickedEnchantment, int modifiedPower) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        //Add all the possible Vanilla enchantments.
        for(Enchantment enchantment : Enchantment.values()) {
            //Enchantment is already granted, because that was the offer.
            if(enchantment == clickedEnchantment) continue;
            if(CEnchantment.getByKey(enchantment.getKey()) != null) continue;
            if(enchantment.isTreasure()) continue;
            if(!enchantment.canEnchantItem(item)) continue;

            for(int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                int minPower = getVanillaMinModified(enchantment, level);
                int maxPower = getVanillaMaxModified(enchantment, level);
                if(minPower == 0 || maxPower == 0) continue;

                if (modifiedPower >= minPower && modifiedPower <= maxPower) {
                    enchantments.put(enchantment, level);
                }
            }
        }

        //Add all the possible CEnchantments.
        for(CEnchantment enchantment : CEnchantment.getEnchantments()) {
            if(enchantment.isTreasure()) continue;
            if(!enchantment.getEnchantProperties().canEnchantWithTable()) continue;
            if(item.getType() == Material.BOOK) {
                if(!enchantment.getEnchantProperties().canBeOnBook()) continue;
            } else {
                if(!enchantment.canEnchantItem(item)) continue;
            }

            CEnchantProperties properties = enchantment.getEnchantProperties();
            for(int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                int minPower = properties.getMinModifiedPower(level);
                int maxPower = properties.getMaxModifiedPower(level);
                if(minPower == 0 || maxPower == 0) continue;

                if (modifiedPower >= minPower && modifiedPower <= maxPower) {
                    enchantments.put(enchantment, level);
                }
            }
        }
        return enchantments;
    }

    static Enchantment chooseEnchantment(List<Enchantment> possibleEnchantments) {
        int totalWeight = 0;

        for(Enchantment possible : possibleEnchantments) {
            if(possible instanceof CEnchantment) {
                CEnchantment cEnchantment = (CEnchantment) possible;

                totalWeight += cEnchantment.getEnchantProperties().getEnchantmentWeight();
            } else {
                totalWeight += getVanillaWeight(possible);
            }
        }

        int w = random.nextInt(totalWeight);
        for(Enchantment possible : possibleEnchantments) {
            int enchantmentWeight;
            if(possible instanceof CEnchantment) {
                enchantmentWeight = ((CEnchantment) possible).getEnchantProperties().getEnchantmentWeight();
            } else {
                enchantmentWeight = getVanillaWeight(possible);
            }

            if((w -= enchantmentWeight) <= 0) {
                return possible;
            }
        }

        return null;
    }

    static List<Enchantment> chooseEnchantments(List<Enchantment> possible, Enchantment clickedEnchantment, int modifiedPower) {
        List<Enchantment> chosen = new ArrayList<>(Arrays.asList(clickedEnchantment));

        possible.removeIf(possibleEnch -> chosen.stream().anyMatch(chosenEnch -> CEnchantment.conflictsWith(possibleEnch, chosenEnch)));

        double probability = random.nextDouble();
        while(probability < (double) (modifiedPower + 1) / 50 && !possible.isEmpty()) {
            Collections.shuffle(possible);

            //Get the new enchantment
            Enchantment toAdd = chooseEnchantment(possible);
            if(toAdd == null) return chosen;

            //Add another
            chosen.add(toAdd);

            //Update probability
            probability = random.nextDouble();

            //Remove conflicting me.dessie.dessielib.experimental.enchantments from the possible
            possible.removeIf(possibleEnch -> chosen.stream().anyMatch(chosenEnch -> CEnchantment.conflictsWith(possibleEnch, chosenEnch)));

            //Update modified power
            modifiedPower /= 2;
        }

        return chosen;
    }

    private static int getVanillaWeight(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().getRarity().getWeight();
    }

    private static int getVanillaMinModified(Enchantment enchantment, int level) {
        net.minecraft.world.item.enchantment.Enchantment nmsEnchantment = ((CraftEnchantment) enchantment).getHandle();
        return nmsEnchantment.getMinCost(level);
    }

    private static int getVanillaMaxModified(Enchantment enchantment, int level) {
        net.minecraft.world.item.enchantment.Enchantment nmsEnchantment = ((CraftEnchantment) enchantment).getHandle();
        return nmsEnchantment.getMaxCost(level);
    }

    private static int getEnchantability(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).getItem().getEnchantmentValue();
    }

}
