package me.dessie.dessielib.particleapi;

import org.bukkit.plugin.java.JavaPlugin;

public class ParticleAPI {
    private static JavaPlugin plugin;
    private static boolean registered;

    /**
     * Registers the API so that it can be used.
     *
     * This method **must** be called before using the API.
     * @param yourPlugin Your {@link JavaPlugin} instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register ParticleAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
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
