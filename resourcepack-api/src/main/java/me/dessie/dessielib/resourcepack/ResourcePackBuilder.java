package me.dessie.dessielib.resourcepack;

import me.dessie.dessielib.core.utils.zipper.Zipper;
import me.dessie.dessielib.resourcepack.assets.Asset;
import me.dessie.dessielib.resourcepack.assets.LanguageAsset;
import me.dessie.dessielib.resourcepack.assets.MetaAsset;
import me.dessie.dessielib.resourcepack.hash.HashUpdater;
import me.dessie.dessielib.resourcepack.webhost.ResourcePackServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourcePackBuilder {

    private String description;

    private final List<Asset> assets = new ArrayList<>();

    private ResourcePackServer resourcePackServer;
    private boolean autoUpdateHash = true;

    //The SHA-1 hash for the current ResourcePack.
    private String hash;
    private byte[] hashBytes;

    private File icon;

    public ResourcePackBuilder() {
        if(!ResourcePack.isRegistered()) {
            throw new IllegalStateException("You need to register ResourcePackAPI to start creating Builders!");
        }

        this.setDescription("Default description");
    }

    /**
     * @return All Assets contained within this ResourcePack
     */
    public List<Asset> getAssets() {
        return assets;
    }

    /**
     * @param type The Type of Asset to get
     * @return All Asset instances of a specific type that are contained within this ResourcePack.
     */
    public <T extends Asset> List<T> getAssetsOf(Class<T> type) {
        return this.getAssets().stream()
                .filter(asset -> asset.getClass().equals(type))
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public String getDescription() { return description; }
    public String getNamespace() { return ResourcePack.getPlugin().getName().toLowerCase(); }
    public File getIcon() { return icon; }
    public ResourcePackServer getResourcePackServer() {return resourcePackServer;}
    public boolean isAutoUpdateHash() { return autoUpdateHash; }
    public String getHash() { return hash; }
    public byte[] getHashBytes() {return hashBytes;}

    /**
     * Sets whether the Resource Pack hash should automatically change if a change in the Resource Pack is detected.
     * This means that the client will re-download the resource pack, instead of using the cached version.
     *
     * It is highly recommended that this is kept enabled, which it is by default.
     *
     * @param autoUpdateHash Whether to update the hash.
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder setAutoUpdateHash(boolean autoUpdateHash) {
        this.autoUpdateHash = autoUpdateHash;
        return this;
    }

    /**
     * Sets the pack's in-game icon
     * @param icon The icon png
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder setIcon(File icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Sets the Resource Pack's Display name.
     * If this is not set, Minecraft uses the default "World Specific Resources".
     * @param displayName The name to set as.
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder setDisplayName(String displayName) {
        displayName = displayName.replaceAll("&([a-fA-F0-9mnolr])", "§$1");
        //Add the Language Asset to change the Display name.
        this.addAsset(new LanguageAsset("en_us", "resourcePack.server.name", displayName));
        return this;
    }

    /**
     * Sets the pack's in-game description
     * @param description The description
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder setDescription(String description) {
        description = description.replaceAll("&([a-fA-F0-9mnolr])", "§$1");
        this.description = description;
        return this;
    }

    /**
     * Adds an asset to this builder to generate files within the Resource Pack.
     * @param asset The Asset to add
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder addAsset(Asset asset) {
        this.assets.add(asset);
        return this;
    }

    /**
     * Adds many assets to this builder to generate files within the Resource Pack.
     * @param assets The Assets to add
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder addAssets(Asset... assets) {
        for(Asset asset : assets) {
            addAsset(asset);
        }
        return this;
    }

    /**
     * Builds the {@link ResourcePack}.
     * This will generate the .zip for players to immediately equip and start using.
     *
     * @return The ResourcePack object
     */
    public ResourcePack build() {
        //Create the Resource Pack folder.
        File resourcePackFolder = new File(ResourcePack.getPlugin().getDataFolder() + "/" + this.getNamespace());
        if(resourcePackFolder.exists()) {
            try {
                FileUtils.deleteDirectory(resourcePackFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(this.getAssetsOf(MetaAsset.class).isEmpty()) {
            //Create the mcmeta asset, since it's a required asset.
            this.addAsset(new MetaAsset("mcmeta", this.getDescription(), this.getIcon()));
        }

        List<Class<? extends Asset>> generated = new ArrayList<>();
        for(Asset asset : this.getAssets()) {
            if(generated.contains(asset.getClass())) continue;
            if(asset.getGenerator() == null) {
                Bukkit.getLogger().severe(asset.getClass().getSimpleName() + " does not have a generator. It will be not generated.");
                continue;
            }

            //Get the assets that are the same class as the one we're about to generate.
            List<Asset> assets = this.getAssets().stream()
                    .filter(asset1 -> asset1.getClass() == asset.getClass())
                    .toList();

            //Initialize and Generate the assets.
            try {
                asset.getGenerator().init(this, assets);
                asset.getGenerator().generate(this, assets);
            } catch (IOException e) {
                e.printStackTrace();
            }

            generated.add(asset.getClass());
        }

        try {
            //Delete the Old file if it exists
            File old = new File(resourcePackFolder + "(old).zip");
            if(old.exists()) {
                old.delete();
            }

            //Rename the old file.
            File zipped = new File(resourcePackFolder + ".zip");
            if(zipped.exists()) {
                zipped.renameTo(old);
            }

            //Get the lastModified FileTime. This could exist from an old .zip.
            //We want to make sure that this FileTime is given to the Zipper, so that the generated
            //zip will have the EXACT same file time as the old one. Otherwise, our SHA1 hashes will be different.
            FileTime lastModified;
            if(old.exists()) {
                ZipFile file = new ZipFile(old);
                Enumeration<? extends ZipEntry> entries = file.entries();
                if(entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    lastModified = FileTime.fromMillis(entry.getLastModifiedTime().toMillis());
                } else lastModified = FileTime.from(Instant.now());
            } else lastModified = FileTime.from(Instant.now());

            //Zip the new file and delete the directory.
            new Zipper(resourcePackFolder, zipped, lastModified);
            FileUtils.deleteDirectory(resourcePackFolder);

            //Get the Resource hash
            this.hash = HashUpdater.getHashAsHex(zipped);
            this.hashBytes = HashUpdater.getHashAsBytes(zipped);

            //Attempt to update the hash if enabled.
            if(this.isAutoUpdateHash() && old.exists()) {
                HashUpdater update = new HashUpdater(zipped, old);
                if(update.getHashHex() != null) {
                    this.hash = update.getHashHex();
                    this.hashBytes = update.getHashBytes();
                }
            }

            ResourcePack pack = new ResourcePack(zipped, new NamespacedKey(ResourcePack.getPlugin(), this.getNamespace()), this);
            if(this.getResourcePackServer() != null) {
                this.getResourcePackServer().setResourcePack(pack);
            }

            return pack;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Hosts the ResourcePack on a local web server for this machine.
     * If this is created, this resource pack will be sent to all Players as a Server Resource pack.
     * @param address The address for the web server.
     * @param port The port for the web server.
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder createWebhost(String address, int port) {
        return this.createWebhost(address, port, false);
    }

    /**
     * Hosts the ResourcePack on a local web server for this machine.
     * If this is created, this resource pack will be sent to all Players as a Server Resource pack.
     * @param address The address for the web server.
     * @param port The port for the web server.
     * @param required If the ResourcePack is required to be applied.
     *                 If this is true, and a Player declines the ResourcePack, they will be kicked.
     * @return The ResourcePackBuilder instance
     */
    public ResourcePackBuilder createWebhost(String address, int port, boolean required) {
        this.resourcePackServer = new ResourcePackServer(address, port, required);
        return this;
    }
}
