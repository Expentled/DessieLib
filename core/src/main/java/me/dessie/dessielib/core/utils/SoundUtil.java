package me.dessie.dessielib.core.utils;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;

public class SoundUtil {

    /**
     * @param block The {@link Block} to obtain the {@link Sound} for.
     * @return The break {@link Sound} for the provided Block.
     */
    public static Sound getBreakSound(Block block) {
        return Sound.valueOf(((CraftBlock) block).getNMS().getSoundType().getBreakSound().getLocation().getPath().toUpperCase().replace(".", "_"));
    }

    /**
     * @param block The {@link Block} to obtain the {@link Sound} for.
     * @return The placing {@link Sound} for the provided Block.
     */
    public static Sound getPlaceSound(Block block) {
        return Sound.valueOf(((CraftBlock) block).getNMS().getSoundType().getPlaceSound().getLocation().getPath());
    }
}