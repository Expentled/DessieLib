package me.dessie.dessielib.resourcepack;

import me.dessie.dessielib.packeteer.PacketListener;
import me.dessie.dessielib.packeteer.Packeteer;
import me.dessie.dessielib.resourcepack.assets.BlockStateAsset;
import me.dessie.dessielib.resourcepack.assets.SoundAsset;
import me.dessie.dessielib.resourcepack.listeners.BlockListener;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResourcePack implements PacketListener {

    private static JavaPlugin plugin;
    private static boolean registered = false;

    private static final List<ResourcePack> resourcePacks = new ArrayList<>();

    private final File zipped;
    private final ResourcePackBuilder builder;
    private final NamespacedKey key;

    /**
     * @param zipped The zipped file of the resource pack.
     * @param key The {@link NamespacedKey} to identify the resource pack.
     * @param builder The {@link ResourcePackBuilder} that created this ResourcePack.
     */
    public ResourcePack(File zipped, NamespacedKey key, ResourcePackBuilder builder) {
        this.zipped = zipped;
        this.builder = builder;
        this.key = key;

        //Don't allow duplicate keys, the new one should overwrite.
        resourcePacks.removeIf(pack -> pack.getKey() == key);
        resourcePacks.add(this);

        //Register the Pack Listener, if needed.
        if(this.getBuilder().getAssetsOf(BlockStateAsset.class).size() > 0) {
            Packeteer packeteer = Packeteer.register(getPlugin());
            BlockListener listener = new BlockListener(getPlugin(), this);

            getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());
            packeteer.addListener(listener);
        }
    }

    /**
     * Plays a SoundAsset to a Player.
     * @param player The Player to play the sound to
     * @param asset The SoundAsset to play
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public void playSound(Player player, SoundAsset asset, float volume, float pitch) {
        player.playSound(player.getLocation(), this.getBuilder().getNamespace() + ":" + asset.getPath(), asset.getCategory(), volume, pitch);
    }

    /**
     * Stops a Sound from playing.
     * @param player The player
     * @param asset The SoundAsset to stop playing
     */
    public void stopSound(Player player, SoundAsset asset) {
        player.stopSound(this.getBuilder().getNamespace() + ":" + asset.getPath());
    }

    /**
     * Checks if a Player accepted or declined the resource pack.
     * This is a shortcut for {@link me.dessie.dessielib.resourcepack.webhost.ResourcePackServer#isLoadedBy(Player)}
     *
     * Note: This method may return incorrect values if a /reload is ran!
     *
     * @param player The Player to check
     * @return If the provided Player accepted or declined the resource pack.
     */
    public boolean isLoadedBy(Player player) {
        return this.getBuilder().getResourcePackServer().isLoadedBy(player);
    }

    /**
     * @param name The path and name of the sound.
     *             Ex:
     *             entity.enderman.scream
     *             block.sand.fall
     * @return The SoundAsset if found
     */
    public SoundAsset getSoundAssetByName(String name) {
        return this.getBuilder().getAssetsOf(SoundAsset.class).stream().filter(asset -> asset.getPath().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    /**
     * Retrieves a ResourcePack from a {@link NamespacedKey}.
     *
     * @param key The NamespacedKey to find the Pack for.
     * @return The ResourcePack with the provided NamespacedKey.
     */
    public static ResourcePack getResourcePack(NamespacedKey key) {
        return resourcePacks.stream().filter(resourcePack -> resourcePack.getKey().equals(key)).findAny().orElse(null);
    }

    /**
     * @return The zip file for the Resource Pack.
     */
    public File getResourcePack() {
        return zipped;
    }

    /**
     * @return The {@link ResourcePackBuilder} for the ResourcePack.
     */
    public ResourcePackBuilder getBuilder() {return builder;}

    /**
     * @return The {@link NamespacedKey} that identifies this pack.
     */
    public NamespacedKey getKey() {return key;}

    /**
     * @return All registered Resource Packs
     */
    public static List<ResourcePack> getResourcePacks() {return resourcePacks;}

    /**
     * @return The plugin that registered the ResourcePack API.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("ResourcePack already registered to " + getPlugin().getName());
        }
        plugin = yourPlugin;
        registered = true;
    }

    /**
     * @return If ResourcePack API has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }
}
