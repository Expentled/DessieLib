package me.dessie.dessielib.registration;

import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.enchantmentapi.CEnchantmentAPI;
import me.dessie.dessielib.inventoryapi.InventoryAPI;
import me.dessie.dessielib.packeteer.Packeteer;
import me.dessie.dessielib.particleapi.ParticleAPI;
import me.dessie.dessielib.resourcepack.ResourcePack;
import me.dessie.dessielib.scoreboardapi.ScoreboardAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class Register {

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
            }
        }
    }
}
