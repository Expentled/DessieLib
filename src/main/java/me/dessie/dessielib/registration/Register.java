package me.dessie.dessielib.registration;

import me.dessie.dessielib.commandapi.CommandAPI;
import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.enchantmentapi.CEnchantmentAPI;
import me.dessie.dessielib.inventoryapi.InventoryAPI;
import me.dessie.dessielib.packeteer.Packeteer;
import me.dessie.dessielib.particleapi.ParticleAPI;
import me.dessie.dessielib.resourcepack.ResourcePack;
import me.dessie.dessielib.scoreboardapi.ScoreboardAPI;
import me.dessie.dessielib.storageapi.StorageAPI;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles registering of the modules in DessieLib when using it externally.
 */
public class Register {

    /**
     * Registers specific parts of DessieLib contained within a single method.
     *
     * This should only be used if you're depending on DessieLib externally, and not shading individual modules.
     *
     * @see RegistrationType
     *
     * @param plugin The registering plugin
     * @param types The types of API to register
     */
    public static void register(JavaPlugin plugin, RegistrationType... types) {
        for(RegistrationType type : types) {
            switch (type) {
                case PACKETEER -> Packeteer.register(plugin);
                case PARTICLE_API -> ParticleAPI.register(plugin);
                case INVENTORY_API -> InventoryAPI.register(plugin);
                case SCOREBOARD_API -> ScoreboardAPI.register(plugin);
                case ENCHANTMENT_API -> CEnchantmentAPI.register(plugin);
                case RESOURCE_PACK_API -> ResourcePack.register(plugin);
                case SLOT_UPDATE_EVENT -> SlotEventHelper.register(plugin);
                case COMMAND_API -> CommandAPI.register(plugin);
                case STORAGE_API -> StorageAPI.register(plugin);
            }
        }
    }
}
