package me.dessie.dessielib.storageapi;

import org.bukkit.plugin.java.JavaPlugin;

public class StorageAPI {

    private static JavaPlugin plugin;
    private static boolean registered;

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        plugin = yourPlugin;
        registered = true;
    }

    /**
     * @return If InventoryAPI has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return The plugin that registered the InventoryAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

}
