package me.dessie.dessielib.registration;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Defines all modules in DessieLib for registration
 *
 * @see Register#register(JavaPlugin, RegistrationType...)
 */
public enum RegistrationType {

    /**
     * Registers {@link me.dessie.dessielib.inventoryapi.InventoryAPI}
     */
    INVENTORY_API,

    /**
     * Registers {@link me.dessie.dessielib.scoreboardapi.ScoreboardAPI}
     */
    SCOREBOARD_API,

    /**
     * Registers {@link me.dessie.dessielib.enchantmentapi.CEnchantmentAPI}
     */
    ENCHANTMENT_API,

    /**
     * Registers {@link me.dessie.dessielib.core.events.slot.SlotUpdateEvent} using {@link me.dessie.dessielib.core.events.slot.SlotEventHelper}
     */
    SLOT_UPDATE_EVENT,

    /**
     * Registers {@link me.dessie.dessielib.particleapi.ParticleAPI}
     */
    PARTICLE_API,

    /**
     * Registers {@link me.dessie.dessielib.resourcepack.ResourcePack}
     */
    RESOURCE_PACK_API,

    /**
     * Registers {@link me.dessie.dessielib.packeteer.Packeteer}
     */
    PACKETEER,

    /**
     * Registers {@link me.dessie.dessielib.commandapi.CommandAPI}
     */
    COMMAND_API,

    /**
     * Registers {@link me.dessie.dessielib.storageapi.StorageAPI}
     */
    STORAGE_API

}
