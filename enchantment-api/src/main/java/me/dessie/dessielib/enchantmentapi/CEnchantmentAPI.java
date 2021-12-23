package me.dessie.dessielib.enchantmentapi;

import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.enchantmentapi.listener.CEnchantmentListener;
import me.dessie.dessielib.enchantmentapi.properties.PropertyListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CEnchantmentAPI {
    private static JavaPlugin plugin;
    private static boolean registered;

    /**
     * Registers the API so that CEnchantments can be created.
     *
     * This method **must** be called before using the API.
     * @param yourPlugin Your {@link JavaPlugin} instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register EnchantmentAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        SlotEventHelper.register(plugin);

        plugin.getServer().getPluginManager().registerEvents(new CEnchantmentListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PropertyListener(), plugin);

        registered = true;
    }

    /**
     * @return The {@link JavaPlugin} that has registered the API.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return If the API has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }
}
