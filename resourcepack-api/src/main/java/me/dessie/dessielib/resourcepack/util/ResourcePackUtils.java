package me.dessie.dessielib.resourcepack.util;

import me.dessie.dessielib.core.utils.Colors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Utility class for creating ResourcePacks, should only be used to gather information for developing resource packs.
 */
public class ResourcePackUtils {

    /**
     * Development method used for printing all the valid states a Block can have.
     * Useful if you don't want to go hunting through the vanilla resource packs to find valid Block States to replace.
     *
     * @param block The NMS Block type. {@see net.minecraft.world.level.block.Blocks}
     */
    public static void printValidStates(Block block) {
        BlockState state = block.defaultBlockState();
        Bukkit.getLogger().log(Level.INFO, Colors.color("&6---- Properties for " + block.getName().getString() + "----"));
        for(Property<?> property : state.getProperties()) {
            Bukkit.getLogger().log(Level.INFO, Colors.color("&aProperty Key: " + property.getName()));
            Bukkit.getLogger().log(Level.INFO, Colors.color("&aValid values: " + property.getAllValues().map(value -> value.toString().split("=")[1]).collect(Collectors.toList())));
            Bukkit.getLogger().log(Level.INFO, "\n");
        }

        Bukkit.getLogger().log(Level.INFO, Colors.color("&6---------------------------------------------------"));
    }

}
