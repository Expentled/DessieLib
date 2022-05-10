package me.dessie.dessielib.enchantmentapi.properties.generators;

import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.enchantmentapi.CEnchantment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Internal class used for handling custom Grindstone logic.
 */
public class GrindstoneGenerator {
    private static final Random random = new Random();

    static void doResultUpdate(ItemStack result, ItemStack target) {
        if (SlotEventHelper.isNullOrAir(result)) return;

        for (CEnchantment enchantment : CEnchantment.getEnchantments(target)) {
            //We're always going to remove the enchantment, we just MIGHT add it back.
            enchantment.removeEnchantment(result, enchantment.getEnchantProperties().canRemoveWithGrindstone());

            if (!enchantment.getEnchantProperties().canRemoveWithGrindstone()) {
                enchantment.enchant(result, CEnchantment.getLevel(target, enchantment), true, false);
            }
        }
    }

    static int getDropExp(ItemStack target, ItemStack other, ItemStack result) {
        int baseExpRandom = 0;
        int baseExp = 0;

        //If the result item is missing an enchantment that a slot item has, we drop experience..
        if(!SlotEventHelper.isNullOrAir(target)) {
            for(CEnchantment enchantment : CEnchantment.getEnchantments(target)) {
                if(!CEnchantment.hasEnchantment(result, enchantment)) {
                    if(enchantment.getEnchantProperties().isDoGrindstoneXpRandom()) {
                        baseExpRandom += enchantment.getEnchantProperties().getGrindstoneXp(CEnchantment.getLevel(target, enchantment));
                    } else {
                        baseExp += enchantment.getEnchantProperties().getGrindstoneXp(CEnchantment.getLevel(target, enchantment));
                    }
                }
            }
        }

        if (!SlotEventHelper.isNullOrAir(other)) {
            for (CEnchantment enchantment : CEnchantment.getEnchantments(other)) {
                if(enchantment.getEnchantProperties().canRemoveWithGrindstone()) {
                    if(enchantment.getEnchantProperties().isDoGrindstoneXpRandom()) {
                        baseExpRandom += enchantment.getEnchantProperties().getGrindstoneXp(CEnchantment.getLevel(other, enchantment));
                    } else {
                        baseExp += enchantment.getEnchantProperties().getGrindstoneXp(CEnchantment.getLevel(other, enchantment));
                    }
                }
            }
        }

        int totalXp = baseExp;
        //Taken right from net.minecraft.server.v1_16_R3.ContainerGrindstone
        if(baseExpRandom > 0) {
            int k = (int) Math.ceil((double) baseExpRandom / 2.0D);
            totalXp += k + random.nextInt(k);
        }

        return totalXp;
    }

    static void dropExp(int amount, Location location) {
        ServerLevel world = (((CraftWorld) location.getWorld()).getHandle());
        while(amount > 0) {
            int k = ExperienceOrb.getExperienceValue(amount);
            amount -= k;

            //Spawn the orb
            world.addFreshEntity(new ExperienceOrb(world, location.getX(), location.getY(), location.getZ(), k), CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }

}
